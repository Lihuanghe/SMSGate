/**
 * 
 */
package com.zx.sms.codec.sgip12.codec;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.sgip12.msg.SgipTraceInfo;
import com.zx.sms.codec.sgip12.msg.SgipTraceResponseMessage;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.codec.sgip12.packet.SgipTraceResponse;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class SgipTraceResponseMessageCodec extends MessageToMessageCodec<Message, SgipTraceResponseMessage> {
	private PacketType packetType;

	public SgipTraceResponseMessageCodec() {
		this(SgipPacketType.TRACERESPONSE);
	}

	public SgipTraceResponseMessageCodec(PacketType packetType) {
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

		SgipTraceResponseMessage responseMessage = new SgipTraceResponseMessage(msg.getHeader());
		responseMessage.setTimestamp(msg.getTimestamp());
		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
		
		int count = bodyBuffer.readUnsignedByte();
		
		SgipTraceInfo[] infos = new SgipTraceInfo[count];
		for(int i =0 ; i< count ; i++) {
			SgipTraceInfo item = new SgipTraceInfo();
			item.setResult(bodyBuffer.readUnsignedByte());
			item.setNodeId(bodyBuffer.readCharSequence(SgipTraceResponse.NODEID.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());
			item.setReceiveTime(bodyBuffer.readCharSequence(SgipTraceResponse.RECEIVETIME.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());
			item.setSendTime(bodyBuffer.readCharSequence(SgipTraceResponse.SENDTIME.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());
			item.setReserve(bodyBuffer.readCharSequence(SgipTraceResponse.RESERVE.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());
			infos[i] = item;
		}
		if(count > 0)responseMessage.setTraceInfos(infos);

		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipTraceResponseMessage msg, List<Object> out) throws Exception {

		int count = msg.getCount();
		int bodyLength = count > 0 ? (SgipTraceResponse.RESULT.getBodyLength() + (count - 1) * (SgipTraceResponse.RESULT.getBodyLength() -1 )):1 ;
		ByteBuf bodyBuffer = ctx.alloc().buffer(bodyLength);

		bodyBuffer.writeByte(count);
		for(int i =0 ; i< count ; i++) {
			SgipTraceInfo item = msg.getTraceInfos()[i];
			bodyBuffer.writeByte(item.getResult());
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(item.getNodeId().getBytes(GlobalConstance.defaultTransportCharset),SgipTraceResponse.NODEID.getLength(), 0));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(item.getReceiveTime().getBytes(GlobalConstance.defaultTransportCharset),SgipTraceResponse.RECEIVETIME.getLength(), 0));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(item.getSendTime().getBytes(GlobalConstance.defaultTransportCharset),SgipTraceResponse.SENDTIME.getLength(), 0));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(item.getReserve().getBytes(GlobalConstance.defaultTransportCharset),SgipTraceResponse.RESERVE.getLength(), 0));
		}

		msg.setBodyBuffer(toArray(bodyBuffer, bodyBuffer.readableBytes()));
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		ReferenceCountUtil.release(bodyBuffer);
		out.add(msg);
	}

}
