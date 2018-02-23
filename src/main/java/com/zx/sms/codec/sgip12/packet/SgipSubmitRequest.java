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
public enum SgipSubmitRequest implements PacketStructure {
	SPNUMBER(SgipDataType.OCTERSTRING, true, 21),
	CHARGENUMBER(SgipDataType.OCTERSTRING, true, 21),
	USERCOUNT(SgipDataType.UNSIGNEDINT, true, 1),
	USERNUMBER(SgipDataType.OCTERSTRING, true, 21),
	CORPID(SgipDataType.OCTERSTRING, true, 5),
	SERVICETYPE(SgipDataType.OCTERSTRING, true, 10),
	FEETYPE(SgipDataType.UNSIGNEDINT, true, 1),
	FEEVALUE(SgipDataType.OCTERSTRING, true, 6),
	GIVENVALUE(SgipDataType.OCTERSTRING, true, 6),
	AGENTFLAG(SgipDataType.UNSIGNEDINT, true, 1),
	MORELATETOMTFLAG(SgipDataType.UNSIGNEDINT, true, 1),
	PRIORITY(SgipDataType.UNSIGNEDINT, true, 1),
	EXPIRETIME(SgipDataType.OCTERSTRING, true, 16),
	SCHEDULETIME(SgipDataType.OCTERSTRING, true, 16),
	REPORTFLAG(SgipDataType.UNSIGNEDINT, true, 1),
	TPPID(SgipDataType.UNSIGNEDINT, true, 1),
	TPUDHI(SgipDataType.UNSIGNEDINT, true, 1),
	MESSAGECODING(SgipDataType.UNSIGNEDINT, true, 1),
	MESSAGETYPE(SgipDataType.UNSIGNEDINT, true, 1),
	MESSAGELENGTH(SgipDataType.UNSIGNEDINT, true, 4),
	MESSAGECONTENT(SgipDataType.OCTERSTRING, false, 0),
	RESERVE(SgipDataType.OCTERSTRING, true, 8);
	
	private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private SgipSubmitRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
		for(SgipSubmitRequest r : SgipSubmitRequest.values()) {
			bodyLength += r.getLength();
		}
		return bodyLength;
	}	
}
