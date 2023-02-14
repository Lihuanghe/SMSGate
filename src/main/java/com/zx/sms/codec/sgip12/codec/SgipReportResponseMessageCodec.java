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
import com.zx.sms.codec.sgip12.msg.SgipReportResponseMessage;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.codec.sgip12.packet.SgipReportResponse;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class SgipReportResponseMessageCodec extends MessageToMessageCodec<Message, SgipReportResponseMessage> {
	private PacketType packetType;

	public SgipReportResponseMessageCodec() {
		this(SgipPacketType.REPORTRESPONSE);
	}

	public SgipReportResponseMessageCodec(PacketType packetType) {
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

		SgipReportResponseMessage responseMessage = new SgipReportResponseMessage(msg.getHeader());
		responseMessage.setTimestamp(msg.getTimestamp());
		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
		responseMessage.setResult(bodyBuffer.readUnsignedByte());
		responseMessage.setReserve(bodyBuffer.readCharSequence(SgipReportResponse.RESERVE.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());

		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipReportResponseMessage msg, List<Object> out) throws Exception {

		ByteBuf bodyBuffer = ctx.alloc().buffer(SgipReportResponse.RESULT.getBodyLength());

		bodyBuffer.writeByte(msg.getResult());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
				SgipReportResponse.RESERVE.getLength(), 0));

		msg.setBodyBuffer(toArray(bodyBuffer, bodyBuffer.readableBytes()));
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		ReferenceCountUtil.release(bodyBuffer);
		out.add(msg);
	}

}
