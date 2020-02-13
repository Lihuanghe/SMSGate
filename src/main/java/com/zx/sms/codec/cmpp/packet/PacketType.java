package com.zx.sms.codec.cmpp.packet;

import io.netty.handler.codec.MessageToMessageCodec;


/**
 *
 * @author huzorro(huzorro@gmail.com)
 */
public interface PacketType {    
    public int getCommandId();
    public PacketStructure[] getPacketStructures();
    public long getAllCommandId();
    public MessageToMessageCodec getCodec();
}
