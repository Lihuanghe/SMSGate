/**
 * 
 */
package com.zx.sms.codec.sgip12.codec;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.sgip12.msg.SgipBindResponseMessage;
import com.zx.sms.codec.sgip12.packet.SgipBindResponse;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class SgipBindResponseMessageCodec extends MessageToMessageCodec<Message, SgipBindResponseMessage> {
	private PacketType packetType;

	public SgipBindResponseMessageCodec() {
		this(SgipPacketType.BINDRESPONSE);
	}

	public SgipBindResponseMessageCodec(PacketType packetType) {
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

		SgipBindResponseMessage responseMessage = new SgipBindResponseMessage(msg.getHeader());
		responseMessage.setTimestamp(msg.getTimestamp());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
		responseMessage.setResult(bodyBuffer.readUnsignedByte());
		responseMessage
				.setReserve(bodyBuffer.readCharSequence(SgipBindResponse.RESERVE.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipBindResponseMessage responseMessage, List<Object> out) throws Exception {

		ByteBuf bodyBuffer = ctx.alloc().buffer(SgipBindResponse.RESERVE.getBodyLength());

		bodyBuffer.writeByte(responseMessage.getResult());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(responseMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
				SgipBindResponse.RESERVE.getLength(), 0));

		responseMessage.setBodyBuffer(toArray(bodyBuffer, bodyBuffer.readableBytes()));
		responseMessage.getHeader().setBodyLength(responseMessage.getBodyBuffer().length);
		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);
	}

}
