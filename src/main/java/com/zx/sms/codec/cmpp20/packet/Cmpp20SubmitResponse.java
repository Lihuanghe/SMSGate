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
public enum Cmpp20SubmitResponse implements PacketStructure {
    MSGID(CmppDataType.UNSIGNEDINT, true, 8),
    RESULT(CmppDataType.UNSIGNEDINT, true, 1);
    private DataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    
    private Cmpp20SubmitResponse(DataType dataType, boolean isFixFiledLength, int length) {
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
       
        return 9;
    }
}
