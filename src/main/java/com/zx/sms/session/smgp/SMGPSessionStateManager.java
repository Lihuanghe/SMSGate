package com.zx.sms.session.smgp;

import java.util.concurrent.ConcurrentMap;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.smgp.msg.SMGPActiveTestMessage;
import com.zx.sms.codec.smgp.msg.SMGPBaseMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

public class SMGPSessionStateManager extends AbstractSessionStateManager<Integer, SMGPBaseMessage> {

	public SMGPSessionStateManager(EndpointEntity entity, ConcurrentMap<Integer, VersionObject<SMGPBaseMessage>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Integer getSequenceId(SMGPBaseMessage msg) {
		
		return msg.getSequenceNo();
	}

	@Override
	protected boolean needSendAgainByResponse(SMGPBaseMessage req, SMGPBaseMessage res) {
		if(res instanceof SMGPSubmitRespMessage){
			SMGPSubmitRespMessage submitresp = (SMGPSubmitRespMessage)res;
			//TODO 电信的超速错误码现在不知道
			return false;
		}
		return false;
	}
	
	protected boolean closeWhenRetryFailed(SMGPBaseMessage req) {
		if(req instanceof SMGPActiveTestMessage) {
			return true;
		}
		return getEntity().isCloseWhenRetryFailed();
	};

}
