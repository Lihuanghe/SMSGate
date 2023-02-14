package com.zx.sms.codec.cmpp.msg;

import java.io.Serializable;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
public interface Message extends BaseMessage  {
	 void setPacketType(PacketType packetType);
	 PacketType getPacketType();
	 void setTimestamp(long milliseconds);
	 long getTimestamp();
	 void setLifeTime(long lifeTime);
	 long getLifeTime();
     void setHeader(Header head);
     Header getHeader();  
     void setBodyBuffer(byte[] buffer);
     byte[] getBodyBuffer();
     Serializable getAttachment();
     void setAttachment(Serializable attachment);
}
