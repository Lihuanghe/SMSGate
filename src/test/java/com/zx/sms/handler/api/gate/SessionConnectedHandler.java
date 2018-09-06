package com.zx.sms.handler.api.gate;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

/**
 * 
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class SessionConnectedHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(SessionConnectedHandler.class);

	private AtomicInteger totleCnt = new AtomicInteger(10);
	private volatile boolean inited = false;
	private long lastNum = 0;
	public AtomicInteger getTotleCnt() {
		return totleCnt;
	}
	public void setTotleCnt(AtomicInteger totleCnt) {
		this.totleCnt = totleCnt;
	}
	
	public SessionConnectedHandler(){
	}
	
	public SessionConnectedHandler(AtomicInteger t){
		totleCnt = t;
	}
	
	public SessionConnectedHandler(int t){
		totleCnt = new AtomicInteger(t);
	}
	
	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		final AtomicInteger tmptotal = new AtomicInteger(totleCnt.get());
		if (evt == SessionState.Connect) {
		
			final EndpointEntity finalentity = (EndpointEntity) getEndpointEntity();
			final Channel ch = ctx.channel();
			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {
				private BaseMessage createTestReq(String content) {
					
					if (finalentity instanceof ServerEndpoint) {
						CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage();
						msg.setDestId("13800138000");
						msg.setLinkid("0000");
//						msg.setMsgContent(sb.toString());
						msg.setMsgContent(content);
						msg.setMsgId(new MsgId());
						msg.setRegisteredDelivery((short) 0);
						if (msg.getRegisteredDelivery() == 1) {
							msg.setReportRequestMessage(new CmppReportRequestMessage());
						}
						msg.setServiceid("10086");
						msg.setSrcterminalId(String.valueOf(System.nanoTime()));
						msg.setSrcterminalType((short) 1);
//						msg.setMsgContent(new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd",50*1024));
						
						return msg;
					} else {
						CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
						msg.setDestterminalId(String.valueOf(System.nanoTime()));
						msg.setLinkID("0000");
						msg.setMsgContent(content);
						msg.setRegisteredDelivery((short)1);
						msg.setMsgid(new MsgId());
						msg.setServiceId("10086");
						msg.setSrcId("10086");
						msg.setMsgsrc("927165");
//						msg.setMsgContent(new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd",50*1024));
						/*
						SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage();
		    			requestMessage.setTimestamp(msg.getTimestamp());
		    			requestMessage.setSpnumber("10086");
		    			requestMessage.setUsercount((short)2);
		    			for (int i = 0; i < requestMessage.getUsercount(); i++) {
		    				requestMessage.addUsernumber(String.valueOf(System.nanoTime()));
		    			}
		    			requestMessage.addUsernumber(String.valueOf(System.nanoTime()));
		    			requestMessage.setCorpid("927165");
		    			requestMessage.setReportflag((short)1);
		    		
		    			requestMessage.setMsgContent(content);*/
						return msg;
					}
				}

				@Override
				public Boolean call() throws Exception{
					int cnt = RandomUtils.nextInt() & 0x4ff;
					while(cnt>0 && tmptotal.get()>0) {
						if(ctx.channel().isWritable()){
							List<Promise> futures = ChannelUtil.syncWriteLongMsgToEntity(getEndpointEntity().getId(), createTestReq("中createT"+UUID.randomUUID().toString()));
//							ChannelFuture future = ChannelUtil.asyncWriteToEntity(getEndpointEntity().getId(), createTestReq("中msg"+UUID.randomUUID().toString()));
//							ChannelFuture future = ctx.writeAndFlush( );
							
							try{
								for(Promise  future: futures){
									future.sync();
									if(future.isSuccess()){
//										logger.info("response:{}",future.get());
									}else{
										logger.error("response:{}",future.cause());
									}
								}
									
								cnt--;
								tmptotal.decrementAndGet();
							}catch(Exception e){
								e.printStackTrace();
								cnt--;
								tmptotal.decrementAndGet();
								break;
							}
						}else{
							Thread.sleep(10);
						}
					}
					return true;
				}
			}, new ExitUnlimitCirclePolicy() {
				@Override
				public boolean notOver(Future future) {
					boolean over =   ch.isActive() && tmptotal.get() > 0;
					if(!over) {
						logger.info("========send over.============");
					
//						ch.writeAndFlush(new CmppTerminateRequestMessage());
					}
					return over;
				}
			},1);
			
			
		}
		
	
		ctx.fireUserEventTriggered(evt);
	}

	
	@Override
	public String name() {
		return "SessionConnectedHandler-Gate";
	}
	
	public SessionConnectedHandler clone() throws CloneNotSupportedException {
		SessionConnectedHandler ret = (SessionConnectedHandler) super.clone();
		return ret;
	}

}
