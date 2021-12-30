package com.zx.sms.session.smpp;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.codec.smpp.msg.EnquireLink;
import com.zx.sms.codec.smpp.msg.Pdu;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

public class SMPPSessionStateManager extends AbstractSessionStateManager<Integer, Pdu> {
	private static final Logger logger = LoggerFactory.getLogger(SMPPSessionStateManager.class);
	public SMPPSessionStateManager(EndpointEntity entity, ConcurrentMap<Integer, VersionObject<Pdu>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Integer getSequenceId(Pdu msg) {
		
		return msg.getSequenceNumber();
	}

	@Override
	protected boolean needSendAgainByResponse(Pdu req, Pdu res) {
		int result = SmppConstants.STATUS_OK;

		if(res instanceof SubmitSmResp) {
			result = res.getCommandStatus();
		}else if(res instanceof DeliverSmResp) {
			result = res.getCommandStatus();
		}
		if (result !=  SmppConstants.STATUS_OK && result != SmppConstants.STATUS_THROTTLED) {
			logger.error("Receive Err Response result: {} . Req: {} ,Resp:{}",result, req, res);
		}
		return result ==  SmppConstants.STATUS_THROTTLED;
	}
	protected boolean closeWhenRetryFailed(Pdu req) {
		if(req instanceof EnquireLink) {
			return true;
		}
		return getEntity().isCloseWhenRetryFailed();
	};
}
