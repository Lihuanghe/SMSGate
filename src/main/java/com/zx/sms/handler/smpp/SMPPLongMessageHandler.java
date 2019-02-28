package com.zx.sms.handler.smpp;

import org.marre.sms.SmsMessage;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.connect.manager.EndpointEntity;

public class SMPPLongMessageHandler extends AbstractLongMessageHandler<BaseSm> {
	
	
	public SMPPLongMessageHandler(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected BaseMessage response(BaseSm msg) {
		return msg.createResponse();
	}

	@Override
	protected boolean needHandleLongMessage(BaseSm msg) {
		
		if(msg instanceof DeliverSmReceipt)
		{
			return false;
		}
		return true;
	}

	@Override
	protected String generateFrameKey(BaseSm msg) throws Exception{
		if(msg instanceof SubmitSm){
			return msg.getDestAddress().getAddress()+msg.getSourceAddress().getAddress();
		}else if(msg instanceof DeliverSm){
			return msg.getSourceAddress().getAddress()+msg.getDestAddress().getAddress();
		}else{
			throw new NotSupportedException("not support LongMessage Type  "+ msg.getClass());
		}
	}

	@Override
	protected void resetMessageContent(BaseSm t, SmsMessage content) {
		t.setSmsMsg(content);
	}
	
}