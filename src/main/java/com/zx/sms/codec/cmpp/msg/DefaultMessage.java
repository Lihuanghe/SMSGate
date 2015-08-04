package com.zx.sms.codec.cmpp.msg;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.atomic.AtomicInteger;

import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.handler.api.AbstractBusinessHandler;

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
	private AtomicInteger requests = new AtomicInteger();
//	private Message response;
//	private Message request;
	private Header header;
	private byte[] buffer;
//	private Object attachment;

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

	public DefaultMessage(PacketType packetType, long sequenceId) {
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
	public int incrementAndGetRequests() {
		return requests.incrementAndGet();
	}

   public void resetRequests(){
	   requests = new AtomicInteger();
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
	public boolean isTerminationLife() {
		return lifeTime !=0 && (( timestamp + lifeTime*1000 ) - CachedMillisecondClock.INS.now() < 0L);
	}
	
	protected DefaultMessage clone() throws CloneNotSupportedException {
		DefaultMessage msg =  (DefaultMessage) super.clone();
		
		DefaultHeader newHeader = new DefaultHeader();
		newHeader.setSequenceId(DefaultSequenceNumberUtil.getSequenceNo());
		newHeader.setCommandId(packetType.getCommandId());
		msg.setHeader(newHeader);
		
		msg.resetRequests();
		msg.setTimestamp(CachedMillisecondClock.INS.now());
		return msg;
	}
}
