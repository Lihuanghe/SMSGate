package com.zx.sms.codec.cmpp.packet;




/**
 *
 * @author huzorro(huzorro@gmail.com)
 */
public interface PacketStructure {
    DataType getDataType();
    boolean isFixFiledLength();
    boolean isFixPacketLength();
    int getLength();
    int getBodyLength();
}
