package com.zx.sms.codec.cmpp.msg;

import java.io.Serializable;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class DefaultMessage implements Message ,Cloneable {
	private static final long serialVersionUID = -4245789758843785127L;
	private PacketType packetType;
	private long timestamp = CachedMillisecondClock.INS.now();
	//消息的生命周期，单位秒, 0表示永不过期
	private long lifeTime=0;
	
//	private Message response;
	private Message request;
	private Header header;
	private byte[] buffer;

	/**
	 * CMPP的消息字段太少,增加一个附加字段,方便业务处理,
	 * 比如给attach设置一个Map
	 **/
	private Serializable attachment;
	
	public DefaultMessage() {
	};

	public DefaultMessage(PacketType packetType, Header header) {
		setPacketType(packetType);
		if (header == null) {
			header = new DefaultHeader();

			header.setSequenceId(DefaultSequenceNumberUtil.getSequenceNo());
		}
		header.setCommandId(packetType.getCommandId());
		setHeader(header);
	};

	public DefaultMessage(PacketType packetType) {
		this(packetType, DefaultSequenceNumberUtil.getSequenceNo());
	}

	public DefaultMessage(PacketType packetType, int sequenceId) {
		setPacketType(packetType);
		Header header = new DefaultHeader();
		header.setSequenceId(sequenceId);
		header.setCommandId(packetType.getCommandId());
		setHeader(header);
	}

	@Override
	public void setPacketType(PacketType packetType) {
		this.packetType = packetType;

	}

	@Override
	public PacketType getPacketType() {
		return packetType;
	}


	@Override
	public void setHeader(Header header) {
		this.header = header;
	}

	@Override
	public Header getHeader() {
		return header;
	}

	@Override
	public void setBodyBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	@Override
	public byte[] getBodyBuffer() {
		return buffer;
	}

	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}

	@Override
	public String toString() {
		return "DefaultMessage [packetType=" + packetType + ", header=" + header + ", getClass()=" + getClass() + "]";
	}

	@Override
	public boolean isTerminated() {
		return lifeTime !=0 && (( timestamp + lifeTime*1000 ) - CachedMillisecondClock.INS.now() < 0L);
	}

	public Serializable getAttachment() {
		return attachment;
	}

	public void setAttachment(Serializable attachment) {
		this.attachment = attachment;
	}

	protected DefaultMessage clone() throws CloneNotSupportedException {
		DefaultMessage msg =  (DefaultMessage) super.clone();
		
		DefaultHeader newHeader = new DefaultHeader();
		newHeader.setNodeId(header.getNodeId());
		newHeader.setSequenceId(header.getSequenceId());
		newHeader.setCommandId(packetType.getCommandId());
		msg.setHeader(newHeader);
		return msg;
	}

	@Override
	public boolean isRequest() {
		long commandId = getHeader().getCommandId();
		return (commandId & 0x80000000L) == 0L;
	}
	@Override
	public boolean isResponse() {
		long commandId = getHeader().getCommandId();
		return (commandId & 0x80000000L) == 0x80000000L;
	}

	@Override
	public void setRequest(BaseMessage message) {
		this.request = (Message)message;
	}

	@Override
	public BaseMessage getRequest() {
		return this.request;
	}

	@Override
	public int getSequenceNo() {
		return getHeader().getSequenceId();
	}

	@Override
	public void setSequenceNo(int seq) {
		getHeader().setSequenceId(seq);
	}
}
