package com.zx.sms.codec.cmpp.msg;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.atomic.AtomicInteger;

import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class DefaultMessage implements Message {
	private static final long serialVersionUID = -4245789758843785127L;
	private PacketType packetType;
	private long timestamp = System.currentTimeMillis();

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

			header.setSequenceId((GlobalConstance.sequenceId.compareAndSet(Integer.MAX_VALUE, 0) ? GlobalConstance.sequenceId.getAndIncrement()
					: GlobalConstance.sequenceId.getAndIncrement()));
		}
		header.setCommandId(packetType.getCommandId());
		setHeader(header);
	};

	public DefaultMessage(PacketType packetType) {
		this(packetType, (GlobalConstance.sequenceId.compareAndSet(Integer.MAX_VALUE, 0) ? GlobalConstance.sequenceId.getAndIncrement()
				: GlobalConstance.sequenceId.getAndIncrement()));
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

	@Override
	public String toString() {
		return "DefaultMessage [packetType=" + packetType + ", header=" + header + ", getClass()=" + getClass() + "]";
	}
}
