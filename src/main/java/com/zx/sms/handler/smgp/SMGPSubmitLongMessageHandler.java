package com.zx.sms.handler.smgp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsMessage;

import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.smgp.msg.MsgId;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;

@Sharable
public class SMGPSubmitLongMessageHandler extends AbstractLongMessageHandler<SMGPSubmitMessage> {

	@Override
	protected void response(ChannelHandlerContext ctx, SMGPSubmitMessage msg) {
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		SMGPSubmitRespMessage responseMessage = new SMGPSubmitRespMessage();
		responseMessage.setSequenceNumber(msg.getSequenceNo());
		responseMessage.setStatus(0);
		responseMessage.setMsgId( new MsgId());
		ctx.writeAndFlush(responseMessage);
	}

	@Override
	protected boolean needHandleLongMessage(SMGPSubmitMessage msg) {
		return true;
	}

	@Override
	protected String generateFrameKey(SMGPSubmitMessage msg) {
		return StringUtils.join(msg.getDestTermIdArray(), "|");
	}

	@Override
	protected SmsMessage getSmsMessage(SMGPSubmitMessage t) {
		
		return t.getMsg();
	}

	@Override
	protected void resetMessageContent(SMGPSubmitMessage t, SmsMessage content) {
		t.setMsgContent(content);
	}

}
