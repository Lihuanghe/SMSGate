/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppSubmitResponse implements PacketStructure {
    MSGID(CmppDataType.UNSIGNEDINT, true, 8),
    RESULT(CmppDataType.UNSIGNEDINT, true, 4);
    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private CmppSubmitResponse(DataType dataType, boolean isFixFiledLength, int length) {
        this.dataType = dataType;
        this.isFixFiledLength = isFixFiledLength;
        this.length = length;
    }
    public DataType getDataType() {
        return dataType;
    }
    public boolean isFixFiledLength() {
        return isFixFiledLength;
    }
    public boolean isFixPacketLength() {
    	return true;
    }
    public int getLength() {
        return length;
    } 
    public int getBodyLength() {
        
        return 12;
    }
}
