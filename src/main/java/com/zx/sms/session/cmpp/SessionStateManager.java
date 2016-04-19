package com.zx.sms.session.cmpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.CmppDeliverResponseMessageCodec;
import com.zx.sms.codec.cmpp.CmppSubmitResponseMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp20.Cmpp20DeliverResponseMessageCodec;
import com.zx.sms.codec.cmpp20.Cmpp20SubmitResponseMessageCodec;
import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

/**
 * @author Lihuanghe(18852780@qq.com) 消息发送窗口拜你控制和消息重发 ，消息持久化
 */
public class SessionStateManager extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);
	// 用来记录连接上的错误消息
	private final Logger errlogger;
	
	//cmpp自定义的可写标致位，1-3已被 AbstractTrafficShapingHandler 使用。
	final int userDefinedWritabilityIndex = 4;

	/**
	 * @param entity
	 *            Session关联的端口
	 * @param storeMap
	 *            存储该端口上的持久化消息，存储不会很大，收到消息resp后就会删除持久化消息。
	 * @param preSend
	 *            预发送数据，连接建立后要发送的数据
	 */
	public SessionStateManager(CMPPEndpointEntity entity, Map<Long, Message> storeMap, Map<Long, Message> preSend) {
		this.entity = entity;
		windowSize = entity.getWindows()<0?0:entity.getWindows();
		if (windowSize == 0) {
			windows = null;
		} else {
			windows = new AtomicInteger();
		}
		errlogger = LoggerFactory.getLogger("error."+entity.getId());
		this.storeMap = storeMap;
		this.preSend = preSend;
	}

	/**
	 * 连接流量统计
	 **/
	private long msgReadCount = 0;
	private long msgWriteCount = 0;
	private CMPPEndpointEntity entity;

	/**
	 * 消息窗口，默认16
	 **/
	private final int windowSize;
	private final AtomicInteger windows ;
	/**
	 * 重发队列
	 **/
	private final ConcurrentHashMap<Long, Entry> msgRetryMap = new ConcurrentHashMap<Long, Entry>();
	/**
	 * 发送未收到resp的消息，需要使用可持久化的Map.
	 */
	private final Map<Long, Message> storeMap;

	/**
	 * 会话刚建立时要发送的数据
	 */
	private Map<Long, Message> preSend;


	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.warn("Connection closed. channel:{}", ctx.channel());
		// 取消重试队列里的任务
		for (Map.Entry<Long, Entry> entry : msgRetryMap.entrySet()) {
			Message requestmsg = entry.getValue().request;

			EndpointConnector conn = CMPPEndpointManager.INS.getEndpointConnector(entity);
			// 所有连接都已关闭
			if (conn == null)
				break;

			Channel ch = conn.fetch();

			if (ch != null && ch.isActive()) {

				if (entity.isReSendFailMsg()) {
					// 连接断连，但是未收到Resp的消息，通过其它连接再发送一次
					ch.writeAndFlush(requestmsg);

					logger.debug("current channel {} is closed.send requestMsg from other channel {} which is active.", ctx.channel(), ch);
				} else {

					errlogger.error("Channel closed . Msg may not send Success. {}", requestmsg);
				}
			}
			cancelRetry(requestmsg, ctx.channel());
		}

		//设置连接为可写
		setUserDefinedWritability(ctx.channel(), true);
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		msgReadCount++;
		if (msg instanceof Message) {
			final Message message = (Message) msg;
			// 设置消息的生命周期
			message.setLifeTime(entity.getLiftTime());

			// 如果是resp，取消息消息重发
			if (!isRequestMsg(message)) {
				// 删除发送成功的消息
				// 要先删除成功的消息，然后再开一个发送窗口。

				Message request = storeMap.remove(message.getHeader().getSequenceId());
				if (request != null) {

					cancelRetry(request, ctx.channel());
					
					// SessionStateManager
					// 是在协议解析的CodecHandler前面的，这里还无法获取消息的具体java类型。所以使用commandId做比较
					if (message.getHeader().getCommandId() == CmppPacketType.CMPPSUBMITRESPONSE.getCommandId()) {
						CmppSubmitResponseMessage submitResp = submitRespdecode(message ,this.entity.getVersion());
						if (submitResp.getResult() != 0 && submitResp.getResult() != 8) {
							errlogger.error("Send SubmitMsg ERR . Msg: {} ,Resp:{}", request, submitResp);
						}
						// 对于超速错误的消息，延迟再发
						// 8是超速错
						if (submitResp.getResult() == 8) {
							errlogger.error("Send SubmitMsg Over speed . Msg: {}", request);
							reWriteLater(ctx, request, ctx.newPromise(), 400);
						}
					} else if (message.getHeader().getCommandId() == CmppPacketType.CMPPDELIVERRESPONSE.getCommandId()) {
						CmppDeliverResponseMessage deliverResp = deliverRespdecode(message ,this.entity.getVersion());
						
						if (deliverResp.getResult() != 0) {
							errlogger.error("Send DeliverMsg ERR . Msg: {} ,Resp:{}", request, deliverResp);
						}
						// 8是超速错
						if (deliverResp.getResult() == 8) {
							reWriteLater(ctx, request, ctx.newPromise(), 400);
						}
					}
				}
				else{
					errlogger.warn("receive ResponseMessage ,but not found related Request Msg. {}",message);
				}
			}
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

		if (msg instanceof Message) {

			// 发送消息超过生命周期
			if (((Message) msg).isTerminationLife()) {
				errlogger.error("Msg Life over .{}", msg);
				promise.setFailure(new RuntimeException("Msg Life over"));
				return;
			}

			if (isRequestMsg((Message) msg)) {
				// 发送，未收到Response时，60秒后重试,
				writeWithWindow(ctx, (Message) msg, promise);
			} else {
				// 发送Response消息时直接发送
				ctx.write(msg, promise);
			}
		} else {
			// 不是Message消息时直接发送
			ctx.write(msg, promise);
		}
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt == SessionState.Connect) {
			preSendMsg(ctx);
		}
		ctx.fireUserEventTriggered(evt);
	}

    void setUserDefinedWritability(Channel ch, boolean writable) {
        ChannelOutboundBuffer cob = ch.unsafe().outboundBuffer();
        if (cob != null ) {
            cob.setUserDefinedWritability(userDefinedWritabilityIndex, writable);
        }
    }
    
	/**
	 * 获取发送窗口，并且注册重试任务
	 **/
	private boolean writeWithWindow(final ChannelHandlerContext ctx, final Message message, final ChannelPromise promise) {
		int acquired = 0;
		try {
			// 获取发送窗口
			acquired = (windows == null ? -1 : windows.get());
			// 防止一个连接死掉，把服务挂死，这里要处理窗口不够用的情况
			if (acquired < windowSize) {
				//设置channel为可写
				if(windows != null){
					//窗口占用增加1
					windows.getAndIncrement();
				}
				setUserDefinedWritability(ctx.channel(),true);
				
				safewrite(ctx, message, promise);
			} else {
				// 加入等待队列
				promise.setFailure(new RuntimeException("send window not enough"));
				//设置channel为不可写
				setUserDefinedWritability(ctx.channel(),false);
			}

		} catch (Exception e) {
			promise.setFailure(e);
			logger.error("windows acquire exception: ", e.getCause() != null ? e.getCause() : e);
		}
		return acquired< windowSize;
	}

	private void scheduleRetryMsg(final ChannelHandlerContext ctx, final Message message, final ChannelPromise promise) {

		long seq = message.getHeader().getSequenceId();

		Entry entry = msgRetryMap.get(seq);

		// 发送次数大于1时要重发
		if (entry != null && entity.getMaxRetryCnt() > 1) {

			/*
			 * TODO bugfix:不知道什么原因，会导致 下面的future任务没有cancel掉。
			 * 这里增加一个引用，当会试任务超过次数限制后，cancel掉自己。
			 * 些任务不能被中断interupted.如果storeMap.remove()被中断会破坏BDB的内部状态，使用BDB无法继续工作
			 */
			final AtomicReference<Future> ref = new AtomicReference<Future>();

			Future<?> future = EventLoopGroupFactory.INS.getMsgResend().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					try {
						int times = message.incrementAndGetRequests();
						logger.warn("retry Send Msg : {}", message);
						if (times > entity.getMaxRetryCnt()) {

							//会有future泄漏的情况发生，这里cancel掉自己，来规避泄漏
							Future future = ref.get();
							if (future != null)
								future.cancel(false);

							cancelRetry(message, ctx.channel());

							// 删除发送成功的消息
							storeMap.remove(message.getHeader().getSequenceId());
							// 发送3次都失败的消息要记录

							logger.error("retry send msg {} times。cancel retry task", times);

							errlogger.error("RetryFailed: {}", message);
							
							logger.error("retry send Message {} 3 times,the connection may die.close it",message);
							ctx.close();
							
						} else {

							msgWriteCount++;
							// 重发不用申请窗口
							ctx.writeAndFlush(message, ctx.newPromise());
						}
					} catch (Throwable e) {
						logger.error("retry Send Msg Error: {}", message);
						logger.error("retry send Msg Error.", e);
					}
				}
			}, entity.getRetryWaitTimeSec(), entity.getRetryWaitTimeSec(), TimeUnit.SECONDS);

			ref.set(future);

			entry.future = future;

			// 这里增加一次判断，是否已收到resp消息,已到resp后，msgRetryMap里的entry会被 remove掉。
			if (msgRetryMap.get(seq) == null) {
				future.cancel(false);
			}

		} else if (entry == null) {
			// 当程序执行到这里时，可能已收到resp消息，此时entry为空。
			logger.warn("receive seq {} not exists in msgRetryMap,maybe response received before create retrytask .", seq);
		}

	}

	private Entry cancelRetry(Message requestMsg, Channel channel) {
		Entry entry = msgRetryMap.remove(requestMsg.getHeader().getSequenceId());

		if (entry != null && entry.future != null) {
			entry.future.cancel(false);
		}
		if (windows != null) {
			// 如果等窗口的队列里有任务，先发送等待的消息
			if (channel != null && channel.isActive()) {

			
				if(windows.decrementAndGet()<0){
					windows.set(0);
				};
				
				//设置连接为可写状态
				setUserDefinedWritability(channel, true);
			} 
		}

		return entry;
	}

	private boolean isRequestMsg(Message msg) {
		long commandId = msg.getHeader().getCommandId();
		return (commandId & 0x80000000L) == 0L;
	}

	private void preSendMsg(ChannelHandlerContext ctx) {
		if (preSend != null && preSend.size() > 0) {
			for (Map.Entry<Long, Message> entry : preSend.entrySet()) {
				Long key = entry.getKey();
				Message msg = entry.getValue();

				if (msg ==null || msg.isTerminationLife()) {
					errlogger.error("Msg Life is Over. {}", msg);
					continue;
				}

				if (msg != null) {
					// 如果配置了失败重发
					if (entity.isReSendFailMsg()) {
						logger.debug("Send last failed msg . {}", msg);
						storeMap.remove(key);
						writeWithWindow(ctx, msg, ctx.newPromise());
					} else {
						// 删除消息不重发
						storeMap.remove(key);
						errlogger.warn("msg send may not success. {}", msg);
					}
				}

			}
			// 让GC回收内存
			preSend.clear();
			preSend = null;
		}
	}

	/**
	 * 发送msg,首先做消息持久化
	 */
	private void safewrite(final ChannelHandlerContext ctx, final Message message, final ChannelPromise promise) {
		if (ctx.channel().isActive()) {
			final Long seq = message.getHeader().getSequenceId();

			message.incrementAndGetRequests();

			// 记录已发送的请求,在发送msg前生记录到map里。防止生成retryTask前就收到resp的情况发生
			Entry tmpentry = new Entry(message);

			Entry old = msgRetryMap.putIfAbsent(seq, tmpentry);

			if (old != null) {
				// bugfix: 集群环境下可能产生相同的seq. 如果已经存在一个相同的seq.
				// 此消息延迟250ms再发
				logger.error("has repeat Sequense {}", seq);

				reWriteLater(ctx, message, promise, 250);

			} else {
				msgWriteCount++;
				// 持久化到队列
				storeMap.put(seq, message);

				ctx.write(message, promise);

				// 注册重试任务
				scheduleRetryMsg(ctx, message, promise);
				ctx.flush();
			}

		} else {
			// 如果连接已关闭，通知上层应用
			if (promise != null && (!promise.isDone())) {
				StringBuilder sb = new StringBuilder();
				sb.append("Connection ").append(ctx.channel()).append(" has closed");
				promise.setFailure(new IOException(sb.toString()));
			}
		}
	}

	private void reWriteLater(final ChannelHandlerContext ctx, final Message message, final ChannelPromise promise, final int delay) {
		EventLoopGroupFactory.INS.getMsgResend().schedule(new Runnable() {
			@Override
			public void run() {
				try {
					write(ctx, message, promise);
				} catch (Exception e) {
					logger.error("has repeat Sequense ,and write Msg err {}", message);
				}
			}

		}, delay, TimeUnit.MILLISECONDS);
	}
	
	private CmppSubmitResponseMessage submitRespdecode(Message message , short version){
		if (version == 0x30L) {
			return CmppSubmitResponseMessageCodec.decode(message);
		} else if (version == 0x20L) {
			return Cmpp20SubmitResponseMessageCodec.decode(message);
		} else  {
			return CmppSubmitResponseMessageCodec.decode(message);
		}
	} 
	private CmppDeliverResponseMessage deliverRespdecode(Message message , short version){
		if (version == 0x30L) {
			return CmppDeliverResponseMessageCodec.decode(message);
		} else if (version == 0x20L) {
			return Cmpp20DeliverResponseMessageCodec.decode(message);
		} else {
			return CmppDeliverResponseMessageCodec.decode(message);
		}
	} 
	private class Entry {
		// 保证future的可见性，
		volatile Future future;
		Message request;

		Entry(Message request) {
			this.request = request;
		}
	}

}
