/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppCancelRequest implements PacketStructure {
	MSGID(CmppDataType.UNSIGNEDINT, true, 8);
    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    private final static int bodyLength = MSGID.length;
    private CmppCancelRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
       
        return bodyLength;
    } 
}
