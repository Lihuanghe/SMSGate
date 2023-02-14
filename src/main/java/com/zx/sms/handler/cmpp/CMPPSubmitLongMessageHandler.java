package com.zx.sms.handler.cmpp;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.connect.manager.EndpointEntity;

public class CMPPSubmitLongMessageHandler extends AbstractLongMessageHandler<CmppSubmitRequestMessage> {

	public CMPPSubmitLongMessageHandler(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected void resetMessageContent(CmppSubmitRequestMessage t, SmsMessage content) {
		t.setMsg(content);
	}

}
