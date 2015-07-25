/**
 * 
 */
package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.google.common.primitives.Bytes;
import com.zx.sms.codec.cmpp.msg.CmppQueryRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.CmppQueryRequest;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppQueryRequestMessageCodec extends MessageToMessageCodec<Message, CmppQueryRequestMessage> {
	private PacketType packetType;

	public CmppQueryRequestMessageCodec() {
		this(CmppPacketType.CMPPQUERYREQUEST);
	}

	public CmppQueryRequestMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId)
		{
			//不解析，交给下一个codec
			out.add(msg);
			return;
		}
		CmppQueryRequestMessage requestMessage = new CmppQueryRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		requestMessage.setTime(bodyBuffer.readBytes(CmppQueryRequest.TIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setQueryType(bodyBuffer.readUnsignedByte());
		requestMessage.setQueryCode(bodyBuffer.readBytes(CmppQueryRequest.QUERYCODE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setReserve(bodyBuffer.readBytes(CmppQueryRequest.RESERVE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppQueryRequestMessage msg, List<Object> out) throws Exception {
		ByteBuf bodyBuffer = ctx.alloc().buffer(CmppQueryRequest.QUERYCODE.getBodyLength());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(msg.getTime().getBytes(GlobalConstance.defaultTransportCharset), CmppQueryRequest.TIME.getLength(), 0));
		bodyBuffer.writeByte(msg.getQueryType());
		bodyBuffer.writeBytes(Bytes.ensureCapacity(msg.getQueryCode().getBytes(GlobalConstance.defaultTransportCharset),
				CmppQueryRequest.QUERYCODE.getLength(), 0));
		bodyBuffer
				.writeBytes(Bytes.ensureCapacity(msg.getReserve().getBytes(GlobalConstance.defaultTransportCharset), CmppQueryRequest.RESERVE.getLength(), 0));

		msg.setBodyBuffer(bodyBuffer);

		out.add(msg);

	}

}
