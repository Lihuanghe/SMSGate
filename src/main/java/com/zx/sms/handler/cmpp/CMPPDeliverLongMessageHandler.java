package com.zx.sms.handler.cmpp;

import org.marre.sms.SmsMessage;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.channel.ChannelHandler.Sharable;
@Sharable
public class CMPPDeliverLongMessageHandler extends AbstractLongMessageHandler<CmppDeliverRequestMessage> {

	
	public CMPPDeliverLongMessageHandler(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected BaseMessage response( CmppDeliverRequestMessage msg) {
		
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(msg.getHeader());
		responseMessage.setResult(0);
		return responseMessage;
	}

	@Override
	protected boolean needHandleLongMessage(CmppDeliverRequestMessage msg) {
	
		return !msg.isReport();
	}

	@Override
	protected String generateFrameKey(CmppDeliverRequestMessage msg) {
		return msg.getSrcterminalId()+msg.getDestId();
	}



	@Override
	protected void resetMessageContent(CmppDeliverRequestMessage t, SmsMessage content) {
		t.setMsgContent(content);
	}

}
