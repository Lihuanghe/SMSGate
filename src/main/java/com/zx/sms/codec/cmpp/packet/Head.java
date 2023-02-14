package com.zx.sms.codec.cmpp.packet;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 */
public interface Head {
    DataType getDataType();
    int getLength();
    int getHeadLength();
}
