package com.zx.sms.handler.api.smsbiz;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

@Sharable
public abstract class MessageReceiveHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveHandler.class);
	private int rate = 1;

	private AtomicLong cnt = new AtomicLong();
	private long lastNum = 0;
	private volatile boolean inited = false;

	public AtomicLong getCnt() {
		return cnt;
	}

	@Override
	public String name() {
		return "MessageReceiveHandler-smsBiz";
	}

	public synchronized void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt == SessionState.Connect && !inited) {
			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
						long nowcnt = cnt.get();
						EndpointConnector conn = getEndpointEntity().getSingletonConnector();
						
						logger.info("channels : {},Totle Receive Msg Num:{},   speed : {}/s",
								conn == null ? 0 : conn.getConnectionNum(), nowcnt, (nowcnt - lastNum) / rate);
						lastNum = nowcnt;
						return true;
				}
			}, new ExitUnlimitCirclePolicy() {
				@Override
				public boolean notOver(Future future) {
					return getEndpointEntity().getSingletonConnector().getConnectionNum()>0;
				}
			}, rate * 1000);
			inited = true;
		}
		ctx.fireUserEventTriggered(evt);
	}

	protected abstract ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg);

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		ChannelFuture future = reponse(ctx, msg);
		if (future != null)
			future.addListener(new GenericFutureListener() {
				@Override
				public void operationComplete(Future future) throws Exception {
					cnt.incrementAndGet();
				}
			});
	}

	public MessageReceiveHandler clone() throws CloneNotSupportedException {
		MessageReceiveHandler ret = (MessageReceiveHandler) super.clone();
		ret.cnt = new AtomicLong();
		ret.lastNum = 0;
		return ret;
	}

}
