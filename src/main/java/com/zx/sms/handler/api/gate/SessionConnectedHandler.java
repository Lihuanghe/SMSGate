package com.zx.sms.handler.api.gate;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

/**
 * 
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class SessionConnectedHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(SessionConnectedHandler.class);

	private Future future;
	private boolean over = false;
	private static AtomicInteger connCnt = new AtomicInteger();
	private final static ConcurrentHashMap<String, AtomicInteger> totleMap = new ConcurrentHashMap<String, AtomicInteger>();

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt == SessionState.Connect) {
			connCnt.incrementAndGet();
			final ChannelHandlerContext finalCtx = ctx;
			final CMPPEndpointEntity finalentity = (CMPPEndpointEntity) getEndpointEntity();

			final AtomicInteger totle = getOne(getEndpointEntity().getId());
			synchronized (totle) {
				
				if(future!=null) return ;
				
				future = EventLoopGroupFactory.INS.getBusiWork().submit(new Runnable() {
					private Message createTestReq() {
						int contentLength = RandomUtils.nextInt() & 0xff;
						StringBuilder sb = new StringBuilder();
						if(contentLength %2 ==0 ){
							while (contentLength-- >0){
								sb.append('中');
							}
						}else{
							while (contentLength-- >0){
								sb.append('a');
							}
						}

						if (finalentity instanceof ServerEndpoint) {
							CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage();
							// msg.getHeader().setSequenceId(System.nanoTime());
							msg.setDestId("13800138000");
							msg.setLinkid("0000");
							msg.setMsgContent(sb.toString());
						
							msg.setMsgId(new MsgId());
							msg.setRegisteredDelivery((short) (RandomUtils.nextBoolean()?1:0));
							if(msg.getRegisteredDelivery()  == 1){
								msg.setReportRequestMessage(new CmppReportRequestMessage());
							}
							msg.setServiceid("10086");
							msg.setSrcterminalId(String.valueOf(System.nanoTime()));
							msg.setSrcterminalType((short) 1);
//							msg.setChannelIds(finalCtx.channel().id().asLongText());
							return msg;
						} else {
							CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
							msg.setDestterminalId(new String[]{"13800138000"});
							msg.setLinkID("0000");
							msg.setMsgContent(sb.toString());
							msg.setMsgid(new MsgId());
							msg.setServiceId("10086");
							msg.setSrcId("10086");
//							msg.setChannelIds(finalCtx.channel().id().asLongText());
							return msg;
						}
					}

					@Override
					public void run() {
						// int cnt = RandomUtils.nextInt() & 0xff;
						while (!over) {
							while (totle.get() < 10000) {
								try {
									Future promise = ChannelUtil.syncWriteToEntity(getEndpointEntity(), createTestReq());
									if (promise==null  || !promise.isSuccess()) {
										logger.error("发送失败!!");
										over=true;
										break;
									} else {
										totle.incrementAndGet();
									}
								} catch (ClosedChannelException ex) {
									logger.error("连接已关闭，发送失败");
									over=true;
									break;
								} catch (Exception e) {
									logger.error("发送失败", e);
									over=true;
									break;
								}
							}
							
//								logger.info("Send 10000 over.wait next time.");
//								Thread.sleep(30000);
								totle.set(0);

						}
					}
				});
			}
			
		}
		ctx.fireUserEventTriggered(evt);

	}

	private AtomicInteger getOne(String key) {
		AtomicInteger totnum = totleMap.get(key);
		if (totnum == null) {
			synchronized (totleMap) {
				totnum = totleMap.get(key);
				if (totnum == null) {
					totnum = new AtomicInteger();
					totleMap.put(key, totnum);
					return totnum;
				}

			}
		}
		return totnum;
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		connCnt.decrementAndGet();
		if (future != null && connCnt.get() == 0) {
			logger.info("cancel future,{}", ctx.channel().id());
			future.cancel(true);
			future = null;
		}
		
		ctx.fireChannelInactive();
	}

	@Override
	public String name() {
		return "SessionConnectedHandler-Gate";
	}

}
