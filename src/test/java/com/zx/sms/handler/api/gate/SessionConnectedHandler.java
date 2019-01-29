package com.zx.sms.handler.api.gate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
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
public abstract class SessionConnectedHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(SessionConnectedHandler.class);

	protected AtomicInteger totleCnt = new AtomicInteger(10);
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
	

	
	protected abstract BaseMessage createTestReq(String content);
	
	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		final AtomicInteger tmptotal = new AtomicInteger(totleCnt.get());
		if (evt == SessionState.Connect) {
		
			final EndpointEntity finalentity = getEndpointEntity();
			final Channel ch = ctx.channel();
			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception{
					int cnt = RandomUtils.nextInt() & 0x4ff;
					while(cnt>0 && tmptotal.get()>0) {
						if(ctx.channel().isWritable()){
							List<Promise> futures = null;
							ChannelFuture chfuture = null;
							ChannelFuture cfuture = null;
							BaseMessage msg = createTestReq("中"+UUID.randomUUID().toString());
//							chfuture = ChannelUtil.asyncWriteToEntity(getEndpointEntity().getId(), msg);
							futures = ChannelUtil.syncWriteLongMsgToEntity(getEndpointEntity().getId(), msg);
//							cfuture = ctx.writeAndFlush(msg);
							cnt--;
							tmptotal.decrementAndGet();
							if(chfuture!=null)chfuture.sync();
							if(cfuture!=null)cfuture.sync();
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
					if(future.cause()!=null)
						future.cause().printStackTrace();
					
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
