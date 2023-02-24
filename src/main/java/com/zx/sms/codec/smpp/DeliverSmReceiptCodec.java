package com.zx.sms.codec.smpp;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

public class DeliverSmReceiptCodec extends MessageToMessageCodec<DeliverSm, DeliverSmReceipt> {
	private final Logger logger = LoggerFactory.getLogger(DeliverSmReceiptCodec.class);
	private SMPPEndpointEntity  smppEntity;
	
	private static final String[] messageStateMap = new String[]{"ENROUTE","DELIVERED","EXPIRED","DELETED","UNDELIVERABLE","ACCEPTED","UNKNOWN","REJECTED"};
	
	DeliverSmReceiptCodec(SMPPEndpointEntity  smppEntity)
	{
		super();
		this.smppEntity = smppEntity;
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
		if(msg.isReport()){
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
	        if(msg.getOptionalParameters()!=null) {
	        	for(Tlv tlv:msg.getOptionalParameters()){
	        		pdu.addOptionalParameter(tlv);
	        	}
	        	
	        	if(StringUtils.isBlank(pdu.getId())) {
	        		Tlv msgId = pdu.getOptionalParameter(SmppConstants.TAG_RECEIPTED_MSG_ID);
	        		if(msgId != null)
	        			pdu.setId(msgId.getValueAsString());
	        	}
	        	
	        	if(StringUtils.isBlank(pdu.getStat())) {
	        		Tlv msgState = pdu.getOptionalParameter(SmppConstants.TAG_MSG_STATE);
	        		if(msgState != null) {
	        			short stateIdx =  msgState.getValueAsUnsignedByte();
	        			if(stateIdx > 0 && stateIdx < 9) {
	        				pdu.setStat(messageStateMap[stateIdx-1]);
	        			}else {
	        				pdu.setStat("UNDEFINE:"+String.valueOf(stateIdx));
	        			}
	        		}
	        	}
	        }
	        
	        if(smppEntity !=null && smppEntity.isUseHexReceiptedMessageId()) {
	        	String id = pdu.getId(); 
	        	//都是10进制数字
	        	if(StringUtils.isNumeric(id)) {
	        		try {
		        		Long t = Long.valueOf(id);
		        		int signedInteger = (int)(t.longValue() & 0x0ffffffffL);
		        		pdu.setId(Integer.toHexString(signedInteger));
	        		}catch(NumberFormatException ex) {
	        			logger.warn("java.lang.NumberFormatException For input id.{}",pdu);
	        		}
	        	}
	        }

	        out.add(pdu);
		}else{
			 out.add(msg);
		}
	}
}
