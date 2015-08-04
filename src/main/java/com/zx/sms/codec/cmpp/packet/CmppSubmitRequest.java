/**
 * 
 */
package com.zx.sms.codec.cmpp.packet;



/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum CmppSubmitRequest implements PacketStructure {
    MSGID(CmppDataType.UNSIGNEDINT, true, 8),
    PKTOTAL(CmppDataType.UNSIGNEDINT, true, 1),
    PKNUMBER(CmppDataType.UNSIGNEDINT, true, 1),
    REGISTEREDDELIVERY(CmppDataType.UNSIGNEDINT, true, 1),
    MSGLEVEL(CmppDataType.UNSIGNEDINT, true, 1),
    SERVICEID(CmppDataType.OCTERSTRING, true, 10),
    FEEUSERTYPE(CmppDataType.UNSIGNEDINT, true, 1),
    FEETERMINALID(CmppDataType.OCTERSTRING, true, 32),
    FEETERMINALTYPE(CmppDataType.UNSIGNEDINT, true, 1),
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
    DESTTERMINALID(CmppDataType.OCTERSTRING, true, 32),
    DESTTERMINALTYPE(CmppDataType.UNSIGNEDINT, true, 1),
    MSGLENGTH(CmppDataType.UNSIGNEDINT, true, 1),
    MSGCONTENT(CmppDataType.OCTERSTRING, false, 0),
    LINKID(CmppDataType.OCTERSTRING, true, 20);
    private CmppDataType dataType;
    private boolean isFixFiledLength; 
    private int length;
    private CmppSubmitRequest(CmppDataType dataType, boolean isFixFiledLength, int length) {
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
       
        return 151;
    }
}
