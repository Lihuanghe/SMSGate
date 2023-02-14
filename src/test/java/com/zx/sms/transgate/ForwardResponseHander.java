package com.zx.sms.transgate;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.wap.UniqueLongMsgId;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.handler.api.AbstractBusinessHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

@Sharable
public class ForwardResponseHander extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(ForwardResponseHander.class);

	// key: UniqueLongMsgId
	private Map<String, ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>>> uidMap;

	// key: srcIdAndDestId
	// key : msgId
	private Map<String, Map<String, UniqueLongMsgId>> msgIdMap;

	private static final String uidPrefix = "_UID_";

	ForwardResponseHander(
			Map<String, ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>>> uidMap,
			Map<String, Map<String, UniqueLongMsgId>> msgIdMap) {
		this.uidMap = uidMap;
		this.msgIdMap = msgIdMap;

	}

	// 测试用例，不考虑状态报告先与response回来的情况
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		// 作为客户端收report消息
		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(0);
			responseMessage.setMsgId(e.getMsgId());
			ctx.channel().writeAndFlush(responseMessage);

			// 状态报告要改写msgId
			if (e.isReport()) {
				CmppReportRequestMessage report = e.getReportRequestMessage();
				String reportMsgId = report.getMsgId().toString();
				// 根据报告里的id获取uid
				String destsrc = e.getSrcIdAndDestId();

				Map<String, UniqueLongMsgId> destsrcuidMap = msgIdMap.get(destsrc);

				if (destsrcuidMap != null) {

					UniqueLongMsgId uid = destsrcuidMap.remove(destsrc + reportMsgId);
//					logger.info("destsrc {} reportMsgId {} ,uid {}" ,destsrc,reportMsgId,uid);

					if (uid != null) {
						// 1: response先回来 ,这种情况是正常的，可能最大
//						logger.info("response先回来 uid {}" ,uid);

						sendBackReport(uid, e);

					} else {
						// 2: report先回来 ，report是会多个连接过来，有多线程问题
						// 遍历出来以“ uidPrefix” 开头的key, 任选一个回传

						// 多个线程同时遍历，避免两个线程选择到同一个
						synchronized (destsrcuidMap) {
							// 拿到锁后，再判断一次是不是response刚好回来，就不用再遍历了
							uid = destsrcuidMap.remove(destsrc + reportMsgId);
							if (uid != null) {
								sendBackReport(uid, e);
							} else {

								// 还是没有再遍历
								ddd(destsrcuidMap, e);
							}
						}
					}

					// 状态发送后，这里为空，把map清除
					synchronized (destsrcuidMap) {
						if (destsrcuidMap.size() == 0) {
//						logger.info("remove {}" ,destsrc);
							msgIdMap.remove(destsrc);
						}
					}

				}
			}

		} else if (msg instanceof CmppSubmitRequestMessage) {
			// 作为服务端收submit消息
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setResult(0);
			ctx.channel().writeAndFlush(resp);

			// 如要记录状态报告的
			if (e.getRegisteredDelivery() == 1) {
				// 记录response的Msgid ,用于状态报告回复
				UniqueLongMsgId uid = e.getUniqueLongMsgId(); // 相同长短信分片uid.getId()相同

				Map<Integer, MsgId> l_msgid = new ConcurrentHashMap<Integer, MsgId>();
				l_msgid.put(Integer.valueOf(uid.getPknumber()), resp.getMsgId());

				// 左值用于记录已接收到状态个数，状态收全了再给下游发
				ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>> p = ImmutablePair
						.of(new AtomicInteger(), ImmutablePair.of(uid, l_msgid));
				ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>> old = uidMap
						.putIfAbsent(uid.getId(), p);
				if (old != null) {
					old.right.right.put(Integer.valueOf(uid.getPknumber()), resp.getMsgId());
				}
			}

		} else if (msg instanceof CmppSubmitResponseMessage) {
			// 作为客户端收response 消息
			CmppSubmitRequestMessage req = (CmppSubmitRequestMessage) ((CmppSubmitResponseMessage) msg).getRequest();
			if (req.getRegisteredDelivery() == 1) {
				String destsrc = req.getSrcIdAndDestId();
				String resMsgid = ((CmppSubmitResponseMessage) msg).getMsgId().toString();

				// 判断resMsgId是不是重复了
				Map<String, UniqueLongMsgId> destsrcuidMap = msgIdMap.get(destsrc);

				UniqueLongMsgId uid = req.getUniqueLongMsgId();
				// 收到response了,
				// destsrcuidMap 一定不为空，且是全局的,是有多线程同时处理一个对象的情况
				if (destsrcuidMap != null) {
					// 这里要考虑，reponse回来晚了，状态报告先回来并且已回传
//					logger.info("  receive {} ,uid:{} ,remove  {}" ,resMsgid,uid,uidPrefix+uid.getId()+uid.getPknumber());
					UniqueLongMsgId requestUid = destsrcuidMap.get(uidPrefix + uid.getId() + uid.getPknumber());
					// 通过判断这个发送时保存的requestUid是否存在，判断有没有回传report

					// 如果已回传过了
					if (requestUid != null) {
						// requestUid还在，说明状态报告没有回传
						synchronized (destsrcuidMap) {
							// 加锁保证同时只有一个线程在增，删。这里不考虑跨进程的情况
							UniqueLongMsgId oldtt = destsrcuidMap.remove(uidPrefix + uid.getId() + uid.getPknumber());
							// 获取锁后，再判断一次report有没有回传
							if (oldtt != null)
								destsrcuidMap.put(destsrc + resMsgid, uid);
						}
					} else {
						// requestUid不在了，状态报告已回传，
					}
				}
			}
		}

		ctx.fireChannelRead(msg);
	}

	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// 发送CmppSubmitRequestMessage消息时，开始记录状态报告要用的相关信息
		if (msg instanceof CmppSubmitRequestMessage) {
			CmppSubmitRequestMessage submit = (CmppSubmitRequestMessage) msg;
			if (submit.getRegisteredDelivery() == 1) {
				String srcAndDest = submit.getSrcIdAndDestId();
				// 发送前放入Map
				Map<String, UniqueLongMsgId> map = new ConcurrentHashMap<String, UniqueLongMsgId>();
				if (submit.getUniqueLongMsgId() != null) {

					UniqueLongMsgId uid = submit.getUniqueLongMsgId();

					if (uid != null ) {
						// 这里放的对象，收到response和report的时候会删除掉。如果没有收到估计会内存泄露，因此要有过期回收机制
						map.put(uidPrefix + uid.getId() + uid.getPknumber(), uid);

						//从channel里收到的消息，才会有  uidMap 的值 
						if( uid.isCreatedByRead()) {
							
							// 发送的时候就知道要收几个状态报告了
							ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>> p = uidMap
									.get(uid.getId());
							p.left.compareAndSet(0, uid.getPktotal());// 根据分片总数，知道有几个report
						}
					} else {
						// uid没有，说明不是通过channel收上来的消息，是通过new创建出来的消息
						// 这种也要设置一个标示这个消息从哪里，状态报告要送回哪里的信息(uid就是这个作用)。 这里暂不处理
						// TODO
					}
//				logger.info("srcAndDest {} ,map {}" ,srcAndDest,map);

					Map<String, UniqueLongMsgId> old = msgIdMap.putIfAbsent(srcAndDest, map);
					if (old != null && uid != null) {
						// 这个消息的uid没放进去msgIdMap，新put一次
						synchronized (old) {
							old = msgIdMap.putIfAbsent(srcAndDest, map);
							if (old != null && uid != null)
								old.putAll(map);
						}
					}
				}
			}
		}
		ctx.write(msg, promise);
	}

	private void writeToEntity(String forwardEid, Object msg) {
		EndpointConnector conn = EndpointManager.INS.getEndpointConnector(forwardEid);
		Channel ch = conn.fetch(); // 获取连接，保证必写成功
		ch.writeAndFlush(msg);
	}

	private void ddd(Map<String, UniqueLongMsgId> destsrcuidMap, CmppDeliverRequestMessage e) throws Exception {
		CmppReportRequestMessage report = e.getReportRequestMessage();
		String reportMsgId = report.getMsgId().toString();
		String MMddHHmmss = reportMsgId.substring(0, 10);
		String destsrc = e.getSrcIdAndDestId();

		UniqueLongMsgId early = null;
		UniqueLongMsgId any = null; // 当early为空时，任选一个发回
		Iterator<Entry<String, UniqueLongMsgId>> itor = destsrcuidMap.entrySet().iterator();
		while (itor.hasNext()) {
			Entry<String, UniqueLongMsgId> entry = itor.next();

			if (entry.getKey().startsWith(uidPrefix)) {
				UniqueLongMsgId requestUid = entry.getValue();
				any = requestUid;
				String uidTime = DateFormatUtils.format(requestUid.getTimestamp(), "MMddHHmmss");
				// 状态时间要大于请求发送时间
				if (MMddHHmmss.compareTo(uidTime) >= 0
						&& (early == null || early.getTimestamp() >= requestUid.getTimestamp())) {
					early = requestUid;
				}
			}
		}

		// 就回传这个了
		UniqueLongMsgId targerUid = early == null ? any : early;
//	logger.info("report  先回来  uid {}" ,targerUid);

		if (targerUid != null) {
			// 有可两个线程选择到同一个，再判断一次是否还有
			targerUid = destsrcuidMap.remove(uidPrefix + targerUid.getId() + targerUid.getPknumber());// 先删除发送request时保存的uid

			if (targerUid != null) {
				sendBackReport(targerUid, e);
			} else {
				logger.info("1 已经有锁了，不应执行到这里 {}", destsrc);
			}

		} else {

			// 前边回来的状态和respones已经把map里的 Request创建的requestId 都删除了，现在只剩下response生成的msgid-uid了

			// 因为是同一个手机号，端口号，没办法了只能再任意先一个发走
			itor = destsrcuidMap.entrySet().iterator();
			while (itor.hasNext()) {
				Entry<String, UniqueLongMsgId> entry = itor.next();
				any = entry.getValue();
				itor.remove();
				break;
			}
//			
			if (any != null)
				sendBackReport(any, e);
			else {
				
				logger.info("走到这里，没办法了，只能依赖状态报告超时自动回传了，把接收时保存的msgid构造一个状态回复。{}, {}", destsrc, reportMsgId);
			}
		}
	}

	private void sendBackReport(UniqueLongMsgId uid, CmppDeliverRequestMessage deliver) throws Exception {
		ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>> t = uidMap.get(uid.getId());
		CmppReportRequestMessage report = deliver.getReportRequestMessage();
		MsgId reportMsgId = report.getMsgId();

		if (t == null) {
			logger.info("t 是空，说明有重复的uid,这个report在这之前已经被回传了；或者这个消息不用回传Report {}", uid);
			return;
		}
		int cnt = t.left.decrementAndGet();
		// 考虑这个短信拆分后发给上游的个数，与从下游接收的个数不一样
		if (cnt <= 0) {
			// 状态收全了，这是最后一个状态了
			// 获取早先回复给下游的msgId
			UniqueLongMsgId originUid = t.right.left;

			MsgId msgId = t.right.right.remove(Integer.valueOf(uid.getPknumber()));

			if (msgId != null) {
				report.setMsgId(msgId); // 重写msgid
				// 转发给下游
				writeToEntity(originUid.getEntityId(), deliver);
			}

			// 接收的分片，比发给上游的分片多，把剩下的都回复报告

//			synchronized (t.right.right) {
			Set<Entry<Integer, MsgId>> entrySet = t.right.right.entrySet();
			Iterator<Entry<Integer, MsgId>> itor = entrySet.iterator();

			while (itor.hasNext()) {
				Entry<Integer, MsgId> entry = itor.next();
				MsgId originMsgId = entry.getValue();
				CmppDeliverRequestMessage cloned = deliver.clone();
				cloned.setMsgId(new MsgId());
				cloned.setSequenceNo(DefaultSequenceNumberUtil.getSequenceNo());

				// 创建一个新的Report对象
				CmppReportRequestMessage newReport = new CmppReportRequestMessage();
				newReport.setDestterminalId(cloned.getSrcterminalId());
				newReport.setMsgId(originMsgId);
				newReport.setSubmitTime(cloned.getReportRequestMessage().getSubmitTime());
				newReport.setDoneTime(cloned.getReportRequestMessage().getDoneTime());
				newReport.setStat("DELIVRD");
				newReport.setSmscSequence(0);
				cloned.setReportRequestMessage(newReport);

				writeToEntity(originUid.getEntityId(), cloned);
				itor.remove();
			}
//			}

			// 最近一个了，清除uidMap，以后没机会了
			uidMap.remove(uid.getId());

		} else {
			// 获取早先回复给下游的msgId
			UniqueLongMsgId originUid = t.right.left;
			MsgId msgId = t.right.right.remove(Integer.valueOf(uid.getPknumber()));
			if (msgId != null) {
				report.setMsgId(msgId); // 重写msgid
				// 转发给下游
				writeToEntity(originUid.getEntityId(), deliver);
			}
		}
	}

	@Override
	public String name() {
		return "ForwardResponseHander";
	}
}
