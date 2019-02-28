package com.zx.sms.handler.sgip;

import org.apache.commons.lang3.StringUtils;
import org.marre.sms.SmsMessage;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitResponseMessage;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.channel.ChannelHandler.Sharable;
@Sharable
public class SgipSubmitLongMessageHandler extends AbstractLongMessageHandler<SgipSubmitRequestMessage> {

	public SgipSubmitLongMessageHandler(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected BaseMessage response(SgipSubmitRequestMessage msg) {
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		SgipSubmitResponseMessage responseMessage = new SgipSubmitResponseMessage(msg.getHeader());
		return responseMessage;
	}

	@Override
	protected boolean needHandleLongMessage(SgipSubmitRequestMessage msg) {
		return true;
	}

	@Override
	protected String generateFrameKey(SgipSubmitRequestMessage msg) {
		return StringUtils.join(msg.getUsernumber(), "|")+msg.getSpnumber();
	}

	@Override
	protected void resetMessageContent(SgipSubmitRequestMessage t, SmsMessage content) {
		t.setMsgContent(content);
		
	}

}
