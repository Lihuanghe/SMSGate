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
public enum Cmpp20SubmitRequest implements PacketStructure {
    MSGID(CmppDataType.UNSIGNEDINT, true, 8),
    PKTOTAL(CmppDataType.UNSIGNEDINT, true, 1),
    PKNUMBER(CmppDataType.UNSIGNEDINT, true, 1),
    REGISTEREDDELIVERY(CmppDataType.UNSIGNEDINT, true, 1),
    MSGLEVEL(CmppDataType.UNSIGNEDINT, true, 1),
    SERVICEID(CmppDataType.OCTERSTRING, true, 10),
    FEEUSERTYPE(CmppDataType.UNSIGNEDINT, true, 1),
    FEETERMINALID(CmppDataType.OCTERSTRING, true, 21),
    TPPID(CmppDataType.UNSIGNEDINT, true, 1),
    TPUDHI(CmppDataType.UNSIGNEDINT, true, 1),
    MSGFMT(CmppDataType.UNSIGNEDINT, true, 1),
    MSGSRC(CmppDataType.OCTERSTRING, true, 6),
    FEETYPE(CmppDataType.OCTERSTRING, true, 2),
    FEECODE(CmppDataType.OCTERSTRING, true, 6),
    VALIDTIME(CmppDataType.OCTERSTRING, true, 17),
    ATTIME(CmppDataType.OCTERSTRING, true, 17),
    SRCID(CmppDataType.OCTERSTRING, true, 21),
    DESTUSRTL(CmppDataType.UNSIGNEDINT, true, 1),
    DESTTERMINALID(CmppDataType.OCTERSTRING, true, 21),
    MSGLENGTH(CmppDataType.UNSIGNEDINT, true, 1),
    MSGCONTENT(CmppDataType.OCTERSTRING, false, 0),
    RESERVE(CmppDataType.OCTERSTRING, true, 8);
    private CmppDataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    private Cmpp20SubmitRequest(CmppDataType dataType, boolean isFixFiledLength, int length) {
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
    	return false;
    }
    public int getLength() {
        return length;
    }
    public int getBodyLength() {
       
        return 126;
    }
}
