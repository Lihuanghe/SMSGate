package com.zx.sms.codec.sgip12.packet;

import com.zx.sms.codec.cmpp.packet.DataType;
import com.zx.sms.codec.cmpp.packet.PacketStructure;
/**
 * 
 * @author 
 *
 */
public  enum SgipTraceResponse implements PacketStructure {
	COUNT(SgipDataType.UNSIGNEDINT, true, 1),
	RESULT(SgipDataType.UNSIGNEDINT, true, 1),
	NODEID(SgipDataType.OCTERSTRING, true, 6),
	RECEIVETIME(SgipDataType.OCTERSTRING, true, 16),
	SENDTIME(SgipDataType.OCTERSTRING, true, 16),
	RESERVE(SgipDataType.OCTERSTRING, true, 8);
	
	private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private SgipTraceResponse(DataType dataType, boolean isFixFiledLength, int length) {
    	this.length = length;
    	this.dataType = dataType;
    	this.isFixFiledLength = isFixFiledLength;
    }
	@Override
	public DataType getDataType() {
		return dataType;
	}

	@Override
	public boolean isFixFiledLength() {
		return isFixFiledLength;
	}

	@Override
	public boolean isFixPacketLength() {
		return true;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getBodyLength() {
		int bodyLength = 0;
		for(SgipTraceResponse r : SgipTraceResponse.values()) {
			bodyLength += r.getLength();
		}
		return bodyLength;
	}	

}
