package com.zx.sms.codec.cmpp.msg;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
public interface Message extends Serializable  {
	public void setPacketType(PacketType packetType);
	public PacketType getPacketType();
	public void setTimestamp(long milliseconds);
	public long getTimestamp();
	public boolean isTerminationLife();
	public void setChannelIds(String channelIds);
	public String getChannelIds();
	public void setChildChannelIds(String childChannelIds);
	public String getChildChannelIds();
	public void setLifeTime(long lifeTime);
	public long getLifeTime();
    public int incrementAndGetRequests();
    public Message setResponse(Message message);
    public Message getResponse();
    public Message setRequest(Message message);
    public Message getRequest(); 
    public void setHeader(Header head);
    public Header getHeader();  
    public void setBodyBuffer(ByteBuf buffer);
    public ByteBuf getBodyBuffer();
    public void setAttachment(Object attachment);
    public Object getAttachment(); 
}
