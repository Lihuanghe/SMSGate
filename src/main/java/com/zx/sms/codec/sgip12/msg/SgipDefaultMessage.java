package com.zx.sms.codec.sgip12.msg;

import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.util.SequenceNumber;

public abstract class SgipDefaultMessage extends DefaultMessage {

	public SgipDefaultMessage(PacketType packetType, Header header) {
		super(packetType,header);
	}
	public SgipDefaultMessage(PacketType packetType) {
		super(packetType);
	}
	public SequenceNumber getSequenceNumber() {
		return new SequenceNumber(getTimestamp(),getHeader().getNodeId(),getSequenceNo()) ;
	}
}
