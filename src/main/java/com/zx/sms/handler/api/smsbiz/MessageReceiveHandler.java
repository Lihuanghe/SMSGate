package com.zx.sms.handler.api.smsbiz;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

@Component("SMSBizMessageReceiveHandler")
@Sharable
public class MessageReceiveHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveHandler.class);
	private static AtomicLong cnt = new AtomicLong();
	private static Future future = null;
	private static AtomicInteger connCnt = new AtomicInteger();

	@Override
	public String name() {
		return "MessageReceiveHandler-smsBiz";
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		connCnt.decrementAndGet();
		if (future != null && connCnt.get() == 0) {
			logger.info("cancel future,{}", ctx.channel().id());
			future.cancel(true);
			future = null;
		}
		logger.info("channelID,{},total receive {}", ctx.channel().id(), cnt.get());
		ctx.fireChannelInactive();
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt == SessionState.Connect) {
			connCnt.incrementAndGet();
			if (future == null) {
				future = EventLoopGroupFactory.INS.getBusiWork().scheduleAtFixedRate(new Runnable() {
					private long lastNum = 0;

					@Override
					public void run() {
						long nowcnt = cnt.get();
						logger.info("Totle Receive Msg Num:{},   speed : {}/s", nowcnt, nowcnt - lastNum);
						lastNum = nowcnt;
					}

				}, 0, 1, TimeUnit.SECONDS);
			}
		}
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setMsgId(e.getMsgId());
			responseMessage.setResult(0);
			ctx.writeAndFlush(responseMessage);
			cnt.incrementAndGet();

		} else if (msg instanceof CmppDeliverResponseMessage) {
			CmppDeliverResponseMessage e = (CmppDeliverResponseMessage) msg;

		} else if (msg instanceof CmppSubmitRequestMessage) {
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setMsgId(e.getMsgid());
			resp.setResult(0);
			ctx.writeAndFlush(resp);
			cnt.incrementAndGet();
		} else if (msg instanceof CmppSubmitResponseMessage) {
			CmppSubmitResponseMessage e = (CmppSubmitResponseMessage) msg;
		} else {
			ctx.fireChannelRead(msg);
		}
	}

}
