package com.zx.sms.session.sgip;

import java.util.Map;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

public class SgipSessionStateManager extends AbstractSessionStateManager<Long, Message> {

	public SgipSessionStateManager(EndpointEntity entity, Map<Long, VersionObject<Message>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Long getSequenceId(Message msg) {
		
		return msg.getHeader().getSequenceId();
	}

	@Override
	protected boolean needSendAgainByResponse(Message req, Message res) {
		return false;
	}

}
