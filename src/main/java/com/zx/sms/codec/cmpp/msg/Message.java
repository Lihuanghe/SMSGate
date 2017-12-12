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
	public void setPacketType(PacketType packetType);
	public PacketType getPacketType();
	public void setTimestamp(long milliseconds);
	public long getTimestamp();
//	public void setChannelIds(String channelIds);
//	public String getChannelIds();
//	public void setChildChannelIds(String childChannelIds);
//	public String getChildChannelIds();
	public void setLifeTime(long lifeTime);
	public long getLifeTime();

//    public Message setRequest(Message message);
//    public Message getRequest(); 
    public void setHeader(Header head);
    public Header getHeader();  
    public void setBodyBuffer(byte[] buffer);
    public byte[] getBodyBuffer();
    public Serializable getAttachment();
    public void setAttachment(Serializable attachment);
}
