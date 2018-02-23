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
import com.zx.sms.codec.sgip12.msg.SgipBindRequestMessage;
import com.zx.sms.codec.sgip12.packet.SgipBindRequest;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class SgipBindRequestMessageCodec extends MessageToMessageCodec<Message, SgipBindRequestMessage> {
	private PacketType packetType;

	public SgipBindRequestMessageCodec() {
		this(SgipPacketType.BINDREQUEST);
	}

	public SgipBindRequestMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId) {
			//不解析，交给下一个codec
			out.add(msg);
			return;
		}

		SgipBindRequestMessage requestMessage = new SgipBindRequestMessage(msg.getHeader());
		requestMessage.setTimestamp(msg.getTimestamp());
		
		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
		
		requestMessage.setLoginType(bodyBuffer.readUnsignedByte());
		requestMessage.setLoginName(bodyBuffer.readBytes(
				SgipBindRequest.LOGINNAME.getLength()).toString(
						GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setLoginPassowrd(bodyBuffer.readBytes(
				SgipBindRequest.LOGINPASSWD.getLength()).toString(
						GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setReserve(bodyBuffer.readBytes(
				SgipBindRequest.RESERVE.getLength()).toString(
						GlobalConstance.defaultTransportCharset).trim());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipBindRequestMessage requestMessage, List<Object> out) throws Exception {
		ByteBuf bodyBuffer =  Unpooled.buffer(SgipBindRequest.LOGINNAME.getBodyLength());
		bodyBuffer.writeByte(requestMessage.getLoginType());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage
				.getLoginName().getBytes(GlobalConstance.defaultTransportCharset),
				SgipBindRequest.LOGINNAME.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage
				.getLoginPassowrd()
				.getBytes(GlobalConstance.defaultTransportCharset),
				SgipBindRequest.LOGINPASSWD.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserve()
				.getBytes(GlobalConstance.defaultTransportCharset),
				SgipBindRequest.RESERVE.getLength(), 0));
		
		requestMessage.setBodyBuffer(toArray(bodyBuffer,bodyBuffer.readableBytes()));
		requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

}
