package com.zx.sms.handler.sgip;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsMessage;

import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitResponseMessage;
@Sharable
public class SgipSubmitLongMessageHandler extends AbstractLongMessageHandler<SgipSubmitRequestMessage> {

	@Override
	protected void response(ChannelHandlerContext ctx, SgipSubmitRequestMessage msg) {
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		SgipSubmitResponseMessage responseMessage = new SgipSubmitResponseMessage(msg.getHeader());
		ctx.writeAndFlush(responseMessage);
	}

	@Override
	protected boolean needHandleLongMessage(SgipSubmitRequestMessage msg) {
		return true;
	}

	@Override
	protected String generateFrameKey(SgipSubmitRequestMessage msg) {
		return StringUtils.join(msg.getUsernumber(), "|");
	}

	@Override
	protected void resetMessageContent(SgipSubmitRequestMessage t, SmsMessage content) {
		t.setMsgContent(content);
		
	}

}
