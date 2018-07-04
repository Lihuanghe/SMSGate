package com.zx.sms.handler.smgp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.marre.sms.SmsMessage;

import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
@Sharable
public class SMGPDeliverLongMessageHandler extends AbstractLongMessageHandler<SMGPDeliverMessage> {

	@Override
	protected void response(ChannelHandlerContext ctx, SMGPDeliverMessage msg) {
		
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		SMGPDeliverRespMessage responseMessage = new SMGPDeliverRespMessage();
		responseMessage.setSequenceNumber(msg.getSequenceNo());
		responseMessage.setStatus(0);
		responseMessage.setMsgId(msg.getMsgId());
		ctx.writeAndFlush(responseMessage);
	}

	@Override
	protected boolean needHandleLongMessage(SMGPDeliverMessage msg) {
	
		return !msg.isReport();
	}

	@Override
	protected String generateFrameKey(SMGPDeliverMessage msg) {
		return msg.getSrcTermId();
	}

	@Override
	protected SmsMessage getSmsMessage(SMGPDeliverMessage t) {
		return t.getMsg();
	}

	@Override
	protected void resetMessageContent(SMGPDeliverMessage t, SmsMessage content) {
		t.setMsgContent(content);
	}

}
