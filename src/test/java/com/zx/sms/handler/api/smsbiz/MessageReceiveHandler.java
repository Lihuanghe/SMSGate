package com.zx.sms.handler.api.smsbiz;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


public abstract class MessageReceiveHandler extends AbstractBusinessHandler {
	protected static final Logger logger = LoggerFactory.getLogger(MessageReceiveHandler.class);
	private int rate = 1;

	private long lastNum = 0;
	private static Map<String,AtomicLong> initedMap = new ConcurrentHashMap<String,AtomicLong>();
	
	private static final Object lock = new Object();

	public AtomicLong getCnt() {
		AtomicLong count = initedMap.get(getEndpointEntity().getId());
		return count;
	}

	@Override
	public String name() {
		return "MessageReceiveHandler-smsBiz";
	}

	public synchronized void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		synchronized (lock) {
			AtomicLong count = initedMap.get(getEndpointEntity().getId());
			
			if (evt == SessionState.Connect && count == null) {
				initedMap.put(getEndpointEntity().getId(), new AtomicLong());
				
				EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						AtomicLong cnt = initedMap.get(getEndpointEntity().getId());
						
							long nowcnt = cnt.get();
							EndpointConnector conn = getEndpointEntity().getSingletonConnector();
							
							logger.info("{} channels : {},Totle Receive Msg Num:{},   speed : {}/s",getEndpointEntity().getId(),
									conn == null ? 0 : conn.getConnectionNum(), nowcnt, (nowcnt - lastNum) / rate);
							lastNum = nowcnt;
							return true;
					}
				}, new ExitUnlimitCirclePolicy() {
					@Override
					public boolean notOver(Future future) {
						boolean inited = getEndpointEntity().getSingletonConnector().getConnectionNum() > 0;
						return inited;
					}
				}, rate * 1000);
			}
		}
		ctx.fireUserEventTriggered(evt);
	}

	protected abstract ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg);

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		if(msg instanceof LongSMSMessage) {
			logger.debug("receive : {}",((LongSMSMessage)msg).getUniqueLongMsgId());
			if(((LongSMSMessage) msg).getFragments()!=null) {
				for(LongSMSMessage f :(List<LongSMSMessage>) ((LongSMSMessage) msg).getFragments()) {
					logger.debug("receive : {}",((LongSMSMessage)f).getUniqueLongMsgId());
				}
			}
		}
		
		ChannelFuture future = reponse(ctx, msg);
		if (future != null && msg instanceof BaseMessage)

			future.addListener(new GenericFutureListener() {
				@Override
				public void operationComplete(Future future) throws Exception {
					if(msg instanceof LongSMSMessage) {
						AtomicLong cnt = initedMap.get(getEndpointEntity().getId());
						cnt.incrementAndGet();
					}
						
				}
			});
	}

	public MessageReceiveHandler clone() throws CloneNotSupportedException {
		MessageReceiveHandler ret = (MessageReceiveHandler) super.clone();
		return ret;
	}

}
