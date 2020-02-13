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

import com.zx.sms.codec.cmpp.msg.CmppQueryRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.CmppQueryRequest;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
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
		int commandId =  msg.getHeader().getCommandId();
		if (packetType.getCommandId() != commandId)
		{
			//不解析，交给下一个codec
			out.add(msg);
			return;
		}
		CmppQueryRequestMessage requestMessage = new CmppQueryRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer =Unpooled.wrappedBuffer( msg.getBodyBuffer());

		requestMessage.setTime(bodyBuffer.readCharSequence(CmppQueryRequest.TIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setQueryType(bodyBuffer.readUnsignedByte());
		requestMessage.setQueryCode(bodyBuffer.readCharSequence(CmppQueryRequest.QUERYCODE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setReserve(bodyBuffer.readCharSequence(CmppQueryRequest.RESERVE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppQueryRequestMessage msg, List<Object> out) throws Exception {
		ByteBuf bodyBuffer = ctx.alloc().buffer(CmppQueryRequest.QUERYCODE.getBodyLength());

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getTime().getBytes(GlobalConstance.defaultTransportCharset), CmppQueryRequest.TIME.getLength(), 0));
		bodyBuffer.writeByte(msg.getQueryType());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getQueryCode().getBytes(GlobalConstance.defaultTransportCharset),
				CmppQueryRequest.QUERYCODE.getLength(), 0));
		bodyBuffer
				.writeBytes(CMPPCommonUtil.ensureLength(msg.getReserve().getBytes(GlobalConstance.defaultTransportCharset), CmppQueryRequest.RESERVE.getLength(), 0));

		msg.setBodyBuffer(toArray(bodyBuffer,bodyBuffer.readableBytes()));
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		ReferenceCountUtil.release(bodyBuffer);

		out.add(msg);

	}

}
