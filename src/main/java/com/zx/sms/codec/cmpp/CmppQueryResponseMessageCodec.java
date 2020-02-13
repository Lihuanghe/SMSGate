/**
 * 
 */
package com.zx.sms.codec.cmpp;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.CmppQueryResponse;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
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
		int commandId =  msg.getHeader().getCommandId();
		if (packetType.getCommandId() != commandId) {
			// 不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppQueryResponseMessage responseMessage = new CmppQueryResponseMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		responseMessage.setTime(bodyBuffer.readCharSequence(CmppQueryResponse.TIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		responseMessage.setQueryType(bodyBuffer.readUnsignedByte());
		responseMessage.setQueryCode(bodyBuffer.readCharSequence(CmppQueryResponse.QUERYCODE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
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

		ByteBuf bodyBuffer =ctx.alloc().buffer(CmppQueryResponse.MOFL.getBodyLength());

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(responseMessage.getTime().getBytes(GlobalConstance.defaultTransportCharset),
				CmppQueryResponse.TIME.getLength(), 0));

		bodyBuffer.writeByte(responseMessage.getQueryType());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(responseMessage.getQueryCode().getBytes(GlobalConstance.defaultTransportCharset),
				CmppQueryResponse.QUERYCODE.getLength(), 0));
		bodyBuffer.writeInt((int) responseMessage.getMtTLMsg());
		bodyBuffer.writeInt((int) responseMessage.getMtTLUsr());
		bodyBuffer.writeInt((int) responseMessage.getMtScs());
		bodyBuffer.writeInt((int) responseMessage.getMtWT());
		bodyBuffer.writeInt((int) responseMessage.getMtFL());
		bodyBuffer.writeInt((int) responseMessage.getMoScs());
		bodyBuffer.writeInt((int) responseMessage.getMoWT());
		bodyBuffer.writeInt((int) responseMessage.getMoFL());

		responseMessage.setBodyBuffer(toArray(bodyBuffer,bodyBuffer.readableBytes()));
		responseMessage.getHeader().setBodyLength(responseMessage.getBodyBuffer().length);
		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);

	}

}
