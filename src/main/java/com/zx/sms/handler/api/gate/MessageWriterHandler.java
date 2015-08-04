package com.zx.sms.handler.api.gate;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.handler.api.AbstractBusinessHandler;

/**
 * 模拟网关消息发送
 */

public class MessageWriterHandler extends AbstractBusinessHandler {

	@Override
	public String name() {
		
		return "TestWriter";
	}

	private static final Logger logger = LoggerFactory.getLogger(MessageWriterHandler.class);


	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof CmppDeliverRequestMessage) {
			
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setMsgId(e.getMsgId());
			responseMessage.setResult(0);
			ctx.writeAndFlush(responseMessage);
			logger.info("receiver DeliverMsg {}" ,msg);

		} else if (msg instanceof CmppSubmitRequestMessage) {
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setMsgId(e.getMsgid());
			resp.setResult(0);
			ctx.writeAndFlush(resp);
			logger.info("receiver SubmitMsg {}" ,msg);
		} else {
			ctx.fireChannelRead(msg);
		}
	}



}

