/**
 * 
 */
package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import com.google.common.primitives.Bytes;
import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.CmppQueryRequest;
import com.zx.sms.codec.cmpp.packet.CmppQueryResponse;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppQueryResponseMessageCodec extends MessageToMessageCodec<Message, CmppQueryResponseMessage> {
	private PacketType packetType;

	public CmppQueryResponseMessageCodec() {
		this(CmppPacketType.CMPPQUERYRESPONSE);
	}

	public CmppQueryResponseMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId) {
			// 不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppQueryResponseMessage responseMessage = new CmppQueryResponseMessage(msg.getHeader());

		ByteBuf bodyBuffer = msg.getBodyBuffer();

		responseMessage.setTime(bodyBuffer.readBytes(CmppQueryResponse.TIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		responseMessage.setQueryType(bodyBuffer.readUnsignedByte());
		responseMessage.setQueryCode(bodyBuffer.readBytes(CmppQueryResponse.QUERYCODE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		responseMessage.setMtTLMsg(bodyBuffer.readUnsignedInt());
		responseMessage.setMtTLUsr(bodyBuffer.readUnsignedInt());
		responseMessage.setMtScs(bodyBuffer.readUnsignedInt());
		responseMessage.setMtWT(bodyBuffer.readUnsignedInt());
		responseMessage.setMtFL(bodyBuffer.readUnsignedInt());
		responseMessage.setMoScs(bodyBuffer.readUnsignedInt());
		responseMessage.setMoWT(bodyBuffer.readUnsignedInt());
		responseMessage.setMoFL(bodyBuffer.readUnsignedInt());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppQueryResponseMessage responseMessage, List<Object> out) throws Exception {

		ByteBuf bodyBuffer = ctx.alloc().buffer(CmppQueryResponse.MOFL.getBodyLength());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(responseMessage.getTime().getBytes(GlobalConstance.defaultTransportCharset),
				CmppQueryResponse.TIME.getLength(), 0));

		bodyBuffer.writeByte(responseMessage.getQueryType());
		bodyBuffer.writeBytes(Bytes.ensureCapacity(responseMessage.getQueryCode().getBytes(GlobalConstance.defaultTransportCharset),
				CmppQueryResponse.QUERYCODE.getLength(), 0));
		bodyBuffer.writeInt((int) responseMessage.getMtTLMsg());
		bodyBuffer.writeInt((int) responseMessage.getMtTLUsr());
		bodyBuffer.writeInt((int) responseMessage.getMtScs());
		bodyBuffer.writeInt((int) responseMessage.getMtWT());
		bodyBuffer.writeInt((int) responseMessage.getMtFL());
		bodyBuffer.writeInt((int) responseMessage.getMoScs());
		bodyBuffer.writeInt((int) responseMessage.getMoWT());
		bodyBuffer.writeInt((int) responseMessage.getMoFL());

		responseMessage.setBodyBuffer(bodyBuffer);

		out.add(responseMessage);

	}

}
