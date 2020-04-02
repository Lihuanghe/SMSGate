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

import org.marre.sms.SmsDcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.packet.SgipDeliverRequest;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class SgipDeliverRequestMessageCodec extends MessageToMessageCodec<Message, SgipDeliverRequestMessage> {
	private static final Logger logger = LoggerFactory.getLogger(SgipDeliverRequestMessageCodec.class);
	private PacketType packetType;

	/**
	 * 
	 */
	public SgipDeliverRequestMessageCodec() {
		this(SgipPacketType.DELIVERREQUEST);
	}

	public SgipDeliverRequestMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message message, List<Object> out) throws Exception {

		int commandId =  message.getHeader().getCommandId();
		if (packetType.getCommandId() != commandId) {
			// 不解析，交给下一个codec
			out.add(message);
			return;
		}

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(message.getBodyBuffer());
		SgipDeliverRequestMessage requestMessage = new SgipDeliverRequestMessage(message.getHeader());
		requestMessage.setTimestamp(message.getTimestamp());

		requestMessage.setUsernumber(bodyBuffer.readCharSequence(SgipDeliverRequest.USERNUMBER.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());
		requestMessage.setSpnumber(bodyBuffer.readCharSequence(SgipDeliverRequest.SPNUMBER.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());
		requestMessage.setTppid(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgfmt(new SmsDcs((byte) bodyBuffer.readUnsignedByte()));

		int frameLength = bodyBuffer.readInt();
				

		byte[] contentbytes = new byte[frameLength];
		bodyBuffer.readBytes(contentbytes);
		
		requestMessage.setMsgContentBytes(contentbytes);
		requestMessage.setMessagelength(frameLength);

		requestMessage.setReserve(bodyBuffer.readCharSequence(SgipDeliverRequest.RESERVE.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());

		out.add(requestMessage);

		ReferenceCountUtil.release(bodyBuffer);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipDeliverRequestMessage requestMessage, List<Object> out) throws Exception {

		ByteBuf bodyBuffer = ctx.alloc().buffer(SgipDeliverRequest.USERNUMBER.getBodyLength() + requestMessage.getMessagelength());

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getUsernumber().getBytes(GlobalConstance.defaultTransportCharset),
				SgipDeliverRequest.USERNUMBER.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getSpnumber().getBytes(GlobalConstance.defaultTransportCharset),
				SgipDeliverRequest.SPNUMBER.getLength(), 0));
		bodyBuffer.writeByte(requestMessage.getTppid());
		bodyBuffer.writeByte(requestMessage.getTpudhi());
		bodyBuffer.writeByte(requestMessage.getMsgfmt().getValue());
		bodyBuffer.writeInt((int) requestMessage.getMessagelength());
		bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
				SgipDeliverRequest.RESERVE.getLength(), 0));

		requestMessage.setBodyBuffer(toArray(bodyBuffer, bodyBuffer.readableBytes()));
		requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
		out.add(requestMessage);
		ReferenceCountUtil.release(bodyBuffer);

	}
}
