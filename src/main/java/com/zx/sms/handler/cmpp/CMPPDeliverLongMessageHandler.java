package com.zx.sms.handler.cmpp;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.connect.manager.EndpointEntity;

public class CMPPDeliverLongMessageHandler extends AbstractLongMessageHandler<CmppDeliverRequestMessage> {

	
	public CMPPDeliverLongMessageHandler(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected void resetMessageContent(CmppDeliverRequestMessage t, SmsMessage content) {
		t.setMsgContent(content);
	}

}
