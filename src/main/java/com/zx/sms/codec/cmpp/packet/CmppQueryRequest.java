/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppQueryRequest implements PacketStructure {
    TIME(CmppDataType.OCTERSTRING, true, 8),
    QUERYTYPE(CmppDataType.UNSIGNEDINT, true, 1),
    QUERYCODE(CmppDataType.OCTERSTRING, true, 10),
    RESERVE(CmppDataType.OCTERSTRING, true, 8);
	
    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    private final static int bodyLength = TIME.length + QUERYTYPE.length +QUERYCODE.length +RESERVE.length ;
    private CmppQueryRequest(DataType dataType, boolean isFixFiledLength, int length) {
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
