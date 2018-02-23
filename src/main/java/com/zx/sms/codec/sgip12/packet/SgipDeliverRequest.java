/**
 * 
 */
package com.zx.sms.codec.sgip12.packet;

import com.zx.sms.codec.cmpp.packet.DataType;
import com.zx.sms.codec.cmpp.packet.PacketStructure;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum SgipDeliverRequest implements PacketStructure {
	USERNUMBER(SgipDataType.OCTERSTRING, true, 21),
	SPNUMBER(SgipDataType.OCTERSTRING, true, 21),
	TPPID(SgipDataType.UNSIGNEDINT, true, 1),
	TPUDHI(SgipDataType.UNSIGNEDINT, true, 1),
	MESSAGECODING(SgipDataType.UNSIGNEDINT, true, 1),
	MESSAGELENGTH(SgipDataType.UNSIGNEDINT, true, 4),
	MESSAGECONTENT(SgipDataType.OCTERSTRING, false, 0),
	RESERVE(SgipDataType.OCTERSTRING, true, 8);
	
	private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private SgipDeliverRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
		return false;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getBodyLength() {
		int bodyLength = 0;
		for(SgipDeliverRequest r : SgipDeliverRequest.values()) {
			bodyLength += r.getLength();
		}
		return bodyLength;
	}	
}
