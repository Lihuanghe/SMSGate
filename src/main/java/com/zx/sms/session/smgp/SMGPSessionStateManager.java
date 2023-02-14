package com.zx.sms.session.smgp;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smgp.msg.SMGPActiveTestMessage;
import com.zx.sms.codec.smgp.msg.SMGPBaseMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

public class SMGPSessionStateManager extends AbstractSessionStateManager<Integer, SMGPBaseMessage> {
	private static final Logger logger = LoggerFactory.getLogger(SMGPSessionStateManager.class);
	public SMGPSessionStateManager(EndpointEntity entity, ConcurrentMap<String, VersionObject<SMGPBaseMessage>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Integer getSequenceId(SMGPBaseMessage msg) {
		
		return msg.getSequenceNo();
	}

	@Override
	protected boolean needSendAgainByResponse(SMGPBaseMessage req, SMGPBaseMessage res) {
		int result = 0;
		if(res instanceof SMGPSubmitRespMessage){
			SMGPSubmitRespMessage submitresp = (SMGPSubmitRespMessage)res;
			result = submitresp.getStatus();
		}else if(res instanceof  SMGPDeliverRespMessage) {
			SMGPDeliverRespMessage deliRes = (SMGPDeliverRespMessage)res;
			result = deliRes.getStatus();
		}
		
		//TODO 电信的超速错误码现在不知道
		if (result != 0 ) {
			logger.error("Entity {} Receive Err Response result: {} . Req: {} ,Resp:{}",getEntity().getId(),result, req, res);
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
