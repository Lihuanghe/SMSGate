/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppDeliverRequest implements PacketStructure {
	MSGID(CmppDataType.UNSIGNEDINT, true, 8),
	DESTID(CmppDataType.OCTERSTRING, true, 21),
	SERVICEID(CmppDataType.OCTERSTRING, true, 10),
	TPPID(CmppDataType.UNSIGNEDINT, true, 1),
	TPUDHI(CmppDataType.UNSIGNEDINT, true, 1),
	MSGFMT(CmppDataType.UNSIGNEDINT, true, 1),
	SRCTERMINALID(CmppDataType.OCTERSTRING, true, 32),
	SRCTERMINALTYPE(CmppDataType.UNSIGNEDINT, true, 1),
	REGISTEREDDELIVERY(CmppDataType.UNSIGNEDINT, true, 1),
	MSGLENGTH(CmppDataType.UNSIGNEDINT, true, 1),
	MSGCONTENT(CmppDataType.OCTERSTRING, false, 0),
	LINKID(CmppDataType.OCTERSTRING, true, 20);
	
	private final static int bodyLength = 97;
			
    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private CmppDeliverRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
       
        return bodyLength;
	}	
}
