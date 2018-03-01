package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsMessage;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;

public class CMPPSubmitLongMessageHandler extends AbstractLongMessageHandler<CmppSubmitRequestMessage> {

	@Override
	protected void response(ChannelHandlerContext ctx, CmppSubmitRequestMessage msg) {
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(msg.getHeader());
		responseMessage.setResult(0);
		ctx.writeAndFlush(responseMessage);
	}

	@Override
	protected boolean needHandleLongMessage(CmppSubmitRequestMessage msg) {
		return true;
	}

	@Override
	protected String generateFrameKey(CmppSubmitRequestMessage msg) {
		return StringUtils.join(msg.getDestterminalId(), "|");
	}

	@Override
	protected SmsMessage getSmsMessage(CmppSubmitRequestMessage t) {
		
		return t.getMsg();
	}

	@Override
	protected void resetMessageContent(CmppSubmitRequestMessage t, SmsMessage content) {
		t.setMsg(content);
	}

}
