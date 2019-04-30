package com.zx.sms.session.sgip;

import java.util.concurrent.ConcurrentMap;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

public class SgipSessionStateManager extends AbstractSessionStateManager<Long, Message> {

	public SgipSessionStateManager(EndpointEntity entity, ConcurrentMap<Long, VersionObject<Message>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Long getSequenceId(Message msg) {
		long seq = msg.getHeader().getSequenceId();
		long node = msg.getHeader().getNodeId();
		return Long.valueOf(node << 32 | seq);
	}

	@Override
	protected boolean needSendAgainByResponse(Message req, Message res) {
		return false;
	}

	@Override
	protected boolean closeWhenRetryFailed(Message req) {
		return getEntity().isCloseWhenRetryFailed();
	}

}
