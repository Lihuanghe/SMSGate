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
	private String channelIds;
	private String childChannelIds;
	private long lifeTime = 72 * 3600L;
	private AtomicInteger requests = new AtomicInteger();
	private Message response;
	private Message request;
	private Header header;
	private transient ByteBuf buffer;
	private Object attachment;

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

	public void setTimestamp(long milliseconds) {
		this.timestamp = milliseconds;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setChannelIds(String channelIds) {
		this.channelIds = channelIds;

	}

	@Override
	public String getChannelIds() {
		return channelIds;
	}

	@Override
	public void setChildChannelIds(String childChannelIds) {
		this.childChannelIds = childChannelIds;
	}

	@Override
	public String getChildChannelIds() {
		return childChannelIds;
	}

	@Override
	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}

	@Override
	public long getLifeTime() {
		return lifeTime;
	}

	@Override
	public boolean isTerminationLife() {
		return (System.currentTimeMillis() - timestamp) > (lifeTime * 1000);
	}

	@Override
	public int incrementAndGetRequests() {
		return requests.incrementAndGet();
	}

	@Override
	public Message setResponse(Message message) {
		this.response = message;
		return this;

	}

	@Override
	public Message getResponse() {
		return response;
	}

	@Override
	public Message setRequest(Message message) {
		this.request = message;
		return this;
	}

	@Override
	public Message getRequest() {
		return request;
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
	public void setBodyBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}

	@Override
	public ByteBuf getBodyBuffer() {
		return buffer;
	}

	@Override
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	@Override
	public Object getAttachment() {
		return attachment;
	}

}
