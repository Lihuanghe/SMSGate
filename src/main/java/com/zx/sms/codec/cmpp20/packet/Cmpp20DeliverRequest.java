/**
 * 
 */
package com.zx.sms.codec.cmpp20.packet;

import com.zx.sms.codec.cmpp.packet.CmppDataType;
import com.zx.sms.codec.cmpp.packet.DataType;
import com.zx.sms.codec.cmpp.packet.PacketStructure;



/**
 * shifei(shifei@asiainfo.com)
 *
 */
public enum Cmpp20DeliverRequest implements PacketStructure {
	MSGID(CmppDataType.UNSIGNEDINT, true, 8),
	DESTID(CmppDataType.OCTERSTRING, true, 21),
	SERVICEID(CmppDataType.OCTERSTRING, true, 10),
	TPPID(CmppDataType.UNSIGNEDINT, true, 1),
	TPUDHI(CmppDataType.UNSIGNEDINT, true, 1),
	MSGFMT(CmppDataType.UNSIGNEDINT, true, 1),
	SRCTERMINALID(CmppDataType.OCTERSTRING, true,21),
	REGISTEREDDELIVERY(CmppDataType.UNSIGNEDINT, true, 1),
	MSGLENGTH(CmppDataType.UNSIGNEDINT, true, 1),
	MSGCONTENT(CmppDataType.OCTERSTRING, false, 0),
	RESERVED(CmppDataType.OCTERSTRING, true, 8);
	
    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private Cmpp20DeliverRequest(DataType dataType, boolean isFixFiledLength, int length) {
        this.dataType = dataType;
        this.isFixFiledLength = isFixFiledLength;
        this.length = length;
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
      
        return 73;
	}	
}
