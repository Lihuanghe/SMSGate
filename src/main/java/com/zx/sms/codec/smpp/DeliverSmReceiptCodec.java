package com.zx.sms.codec.smpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;

public class DeliverSmReceiptCodec extends MessageToMessageCodec<DeliverSm, DeliverSmReceipt> {

	DeliverSmReceiptCodec()
	{
		super();
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, DeliverSmReceipt msg, List<Object> out) throws Exception {
		DeliverSm pdu = new DeliverSm();
        pdu.setCommandStatus(msg.getCommandStatus());
        pdu.setSequenceNumber(msg.getSequenceNumber());
        pdu.setServiceType(msg.getServiceType());
        pdu.setSourceAddress(msg.getSourceAddress());
        pdu.setDestAddress(msg.getDestAddress());
        pdu.setEsmClass(msg.getEsmClass());
        pdu.setProtocolId(msg.getProtocolId());
        pdu.setPriority(msg.getPriority());
        pdu.setScheduleDeliveryTime(msg.getScheduleDeliveryTime());
        pdu.setValidityPeriod(msg.getValidityPeriod());
        pdu.setRegisteredDelivery(msg.getRegisteredDelivery());
        pdu.setReplaceIfPresent(msg.getReplaceIfPresent());
        pdu.setDataCoding(msg.getDataCoding());
        pdu.setDefaultMsgId(msg.getDefaultMsgId());
        pdu.setShortMessage(msg.getShortMessage());
        pdu.setMsglength((short)msg.getShortMessage().length);
        if(msg.getOptionalParameters()!=null)
        	for(Tlv tlv:msg.getOptionalParameters()){
        		pdu.addOptionalParameter(tlv);
        	}
        out.add(pdu);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DeliverSm msg, List<Object> out) throws Exception {
		/*

x x 0 0 0 0 x x Default message Type (i.e. normal message)
x x 0 0 0 1 x x Short Message contains SMSC Delivery Receipt
x x 0 0 1 0 x x Short Message contains SME Delivery Acknowledgement
x x 0 0 1 1 x x reserved
x x 0 1 0 0 x x Short Message contains SME Manual/User Acknowledgment
x x 0 1 0 1 x x reserved
x x 0 1 1 0 x x Short Message contains Conversation Abort (Korean CDMA)
x x 0 1 1 1 x x reserved
x x 1 0 0 0 x x Short Message contains Intermediate Delivery Notification 
		 * */
		if((msg.getEsmClass() & 0x3c) == 0x04){
			//状态报告解析
			DeliverSmReceipt pdu = new DeliverSmReceipt();
		    pdu.setCommandLength(msg.getCommandLength());
	        pdu.setCommandStatus(msg.getCommandStatus());
	        pdu.setSequenceNumber(msg.getSequenceNumber());
	        pdu.setServiceType(msg.getServiceType());
	        pdu.setSourceAddress(msg.getSourceAddress());
	        pdu.setDestAddress(msg.getDestAddress());
	        pdu.setEsmClass(msg.getEsmClass());
	        pdu.setProtocolId(msg.getProtocolId());
	        pdu.setPriority(msg.getPriority());
	        pdu.setScheduleDeliveryTime(msg.getScheduleDeliveryTime());
	        pdu.setValidityPeriod(msg.getValidityPeriod());
	        pdu.setRegisteredDelivery(msg.getRegisteredDelivery());
	        pdu.setReplaceIfPresent(msg.getReplaceIfPresent());
	        pdu.setDataCoding(msg.getDataCoding());
	        pdu.setDefaultMsgId(msg.getDefaultMsgId());
	        pdu.setShortMessage(msg.getShortMessage());
	        if(msg.getOptionalParameters()!=null)
	        	for(Tlv tlv:msg.getOptionalParameters()){
	        		pdu.addOptionalParameter(tlv);
	        	}
	        out.add(pdu);
		}else{
			 out.add(msg);
		}
	}
}
