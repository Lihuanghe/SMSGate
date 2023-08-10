/**
 * 
 */
package com.zx.sms.codec.sgip12.codec;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.sgip12.msg.SgipTraceRequestMessage;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.codec.sgip12.packet.SgipReportRequest;
import com.zx.sms.codec.sgip12.packet.SgipTraceRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SgipTraceRequestMessageCodec extends MessageToMessageCodec<Message, SgipTraceRequestMessage> {
	private PacketType packetType;

	public SgipTraceRequestMessageCodec() {
		this(SgipPacketType.TRACEREQUEST);
	}

	public SgipTraceRequestMessageCodec(PacketType packetType) {
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

		SgipTraceRequestMessage requestMessage = new SgipTraceRequestMessage(msg.getHeader());
		requestMessage.setTimestamp(msg.getTimestamp());
		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
		
		byte[] seqbytes = new byte[SgipTraceRequest.SUBMITSEQUENCENUMBER.getLength()];
		bodyBuffer.readBytes(seqbytes);
		
		requestMessage.setSequenceId(DefaultSequenceNumberUtil.bytes2SequenceN(seqbytes));
		requestMessage.setUsernumber(bodyBuffer.readCharSequence(SgipReportRequest.USERNUMBER.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setReserve(bodyBuffer.readCharSequence(SgipReportRequest.RESERVE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipTraceRequestMessage msg, List<Object> out) throws Exception {

		ByteBuf bodyBuffer = ctx.alloc().buffer(SgipTraceRequest.USERNUMBER.getBodyLength());
		bodyBuffer.writeBytes(DefaultSequenceNumberUtil.sequenceN2Bytes(msg.getSequenceId()));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getUsernumber().getBytes(GlobalConstance.defaultTransportCharset),
				SgipReportRequest.USERNUMBER.getLength(), 0));

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
				SgipReportRequest.RESERVE.getLength(), 0));

		msg.setBodyBuffer(toArray(bodyBuffer, bodyBuffer.readableBytes()));
		ReferenceCountUtil.release(bodyBuffer);
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		out.add(msg);
	}

}
