package com.zx.sms.connect.manager.smpp;

import java.util.concurrent.atomic.AtomicInteger;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;

public class SMPPSessionConnectedHandler extends SessionConnectedHandler {
	public SMPPSessionConnectedHandler(int t) {
		totleCnt = new AtomicInteger(t);
	}

	@Override
	protected BaseMessage createTestReq(String content) {
		final EndpointEntity finalentity = getEndpointEntity();

		if (finalentity instanceof ServerEndpoint) {
			DeliverSm pdu = new DeliverSm();
	        pdu.setSourceAddress(new Address((byte)0,(byte)0,"13800138000"));
	        pdu.setDestAddress(new Address((byte)0,(byte)0,"10086"));
	        pdu.setSmsMsg(content);
			return pdu;
		} else {
			SubmitSm pdu = new SubmitSm();
	        pdu.setSourceAddress(new Address((byte)0,(byte)0,"10086"));
	        pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
	        pdu.setSmsMsg(content);
			return pdu;
		}
	}

}
