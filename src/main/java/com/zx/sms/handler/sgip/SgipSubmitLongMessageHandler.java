package com.zx.sms.handler.sgip;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.connect.manager.EndpointEntity;
public class SgipSubmitLongMessageHandler extends AbstractLongMessageHandler<SgipSubmitRequestMessage> {

	public SgipSubmitLongMessageHandler(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected void resetMessageContent(SgipSubmitRequestMessage t, SmsMessage content) {
		t.setMsgContent(content);
		
	}

}
