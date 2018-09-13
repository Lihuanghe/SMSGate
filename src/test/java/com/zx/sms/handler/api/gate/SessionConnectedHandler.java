package com.zx.sms.handler.api.gate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppQueryRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

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
		
			final CMPPEndpointEntity finalentity = (CMPPEndpointEntity)getEndpointEntity();
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
						
						msg.setServiceid("10086");
						msg.setSrcterminalId(String.valueOf(System.nanoTime()));
						msg.setSrcterminalType((short) 1);
//						msg.setMsgContent(new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd",50*1024));
						
						return msg;
					} else {
						CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
						msg.setDestterminalId(String.valueOf(System.currentTimeMillis()/1000));
						msg.setLinkID("0000");
						msg.setMsgContent(System.nanoTime()+"|||21==21==ｋ===看=1==ms21======1.是服务器内部的重定向，服务器直接访问目标地址的 url网址，把里面的东西读取出来，但是客户端并不知道，因此用forward的话，客户端浏览器的网址是不会发生变化的。NotetMsgContent(newSmsMmsNotificationMessage");
						msg.setRegisteredDelivery((short)1);
						msg.setMsgid(new MsgId());
						msg.setServiceId("10086");
						msg.setSrcId(finalentity.getSpCode());
						msg.setMsgsrc(finalentity.getUserName());
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
							List<Promise> futures = null;
							ChannelFuture chfuture = null;
//							chfuture = ChannelUtil.asyncWriteToEntity(getEndpointEntity().getId(), createTestReq("中createT"+UUID.randomUUID().toString()));
							futures = ChannelUtil.syncWriteLongMsgToEntity(getEndpointEntity().getId(), createTestReq("中msg"+UUID.randomUUID().toString()));
//							ChannelFuture future = ctx.writeAndFlush( );
							cnt--;
							tmptotal.decrementAndGet();
							if(chfuture!=null)chfuture.sync();
							
							if(futures==null) continue;
							try{
								for(Promise  future: futures){
									future.sync();
									if(future.isSuccess()){
//										logger.info("response:{}",future.get());
									}else{
//										logger.error("response:{}",future.cause());
									}
								}


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

	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
//		logger.info("Receive : {}" ,msg);
		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(0);
			responseMessage.setMsgId(e.getMsgId());
			ctx.channel().writeAndFlush(responseMessage);
			
		} else if (msg instanceof CmppDeliverResponseMessage) {
			CmppDeliverResponseMessage e = (CmppDeliverResponseMessage) msg;

		} else if (msg instanceof CmppSubmitRequestMessage) {
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			final CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
//			resp.setResult(RandomUtils.nextInt()%2 <1 ? 8 : 0);
			resp.setResult(0);
			ctx.channel().writeAndFlush(resp);
			
			//回复状态报告
			final CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
			deliver.setDestId(e.getSrcId());
			deliver.setSrcterminalId(e.getDestterminalId()[0]);
			CmppReportRequestMessage report = new CmppReportRequestMessage();
			report.setDestterminalId(deliver.getSrcterminalId());
			report.setMsgId(resp.getMsgId());
			String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHMM");
			report.setSubmitTime(t);
			report.setDoneTime(t);
			report.setStat("DELIVRD");
			report.setSmscSequence(0);
			deliver.setReportRequestMessage(report);
			
			ctx.executor().schedule(new Runnable() {
				public void run() {
					ctx.channel().writeAndFlush(deliver);
				}
			}, 5, TimeUnit.SECONDS);
			
		} else if (msg instanceof CmppSubmitResponseMessage) {
			CmppSubmitResponseMessage e = (CmppSubmitResponseMessage) msg;
		} else if (msg instanceof CmppQueryRequestMessage) {
			CmppQueryRequestMessage e = (CmppQueryRequestMessage) msg;
			CmppQueryResponseMessage res = new CmppQueryResponseMessage(e.getHeader().getSequenceId());
			ctx.channel().writeAndFlush(res);
		} else {
			ctx.fireChannelRead(msg);
		}
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
