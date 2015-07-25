/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppReportRequest implements PacketStructure {
	MSGID(CmppDataType.UNSIGNEDINT, true, 8),
	STAT(CmppDataType.OCTERSTRING, true, 7),
	SUBMITTIME(CmppDataType.OCTERSTRING, true, 10),
	DONETIME(CmppDataType.OCTERSTRING, true, 10),
	DESTTERMINALID(CmppDataType.OCTERSTRING, true, 32),
	SMSCSEQUENCE(CmppDataType.UNSIGNEDINT, true, 4);

    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private CmppReportRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
		return true;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getBodyLength() {
        
        return 71;
	}
}
