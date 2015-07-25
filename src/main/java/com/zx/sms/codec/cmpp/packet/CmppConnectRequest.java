/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppConnectRequest implements PacketStructure {
	SOURCEADDR(CmppDataType.OCTERSTRING, true, 6),
    AUTHENTICATORSOURCE(CmppDataType.OCTERSTRING, true, 16),
    VERSION(CmppDataType.UNSIGNEDINT, true, 1),
    TIMESTAMP(CmppDataType.UNSIGNEDINT, true, 4);
    private DataType dataType;
    private boolean isFixFiledLength;
    private int length;
    private final static int bodyLength = SOURCEADDR.length + AUTHENTICATORSOURCE.length + VERSION.length + TIMESTAMP.length;
    
    private CmppConnectRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
        return bodyLength;
    }
}
