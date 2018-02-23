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
public enum SgipReportRequest implements PacketStructure {
	SUBMITSEQUENCENUMBER(SgipDataType.UNSIGNEDINT, true, 12),
	REPORTTYPE(SgipDataType.UNSIGNEDINT, true, 1),
	USERNUMBER(SgipDataType.OCTERSTRING, true, 21),
	STATE(SgipDataType.UNSIGNEDINT, true, 1),
	ERRORCODE(SgipDataType.UNSIGNEDINT, true, 1),
	RESERVE(SgipDataType.OCTERSTRING, true, 8);
	
	private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private SgipReportRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
		for(SgipReportRequest r : SgipReportRequest.values()) {
			bodyLength += r.getLength();
		}
		return bodyLength;
	}	
}
