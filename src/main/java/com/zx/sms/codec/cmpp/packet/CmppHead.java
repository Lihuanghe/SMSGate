/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;


/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppHead implements Head {
    COMMANDID(CmppDataType.UNSIGNEDINT, 4), 
    TOTALLENGTH(CmppDataType.UNSIGNEDINT, 4),
    SEQUENCEID(CmppDataType.OCTERSTRING, 4);
    private DataType dataType;
    private int length;
    
    
    private CmppHead(DataType dataType, int length){
        this.dataType = dataType;
        this.length = length;
    }
    public DataType getDataType() {
        return dataType; 
    }
    public int getLength() {
        return length;
    }
    public int getHeadLength() {
        
        return 12;
    }

}
