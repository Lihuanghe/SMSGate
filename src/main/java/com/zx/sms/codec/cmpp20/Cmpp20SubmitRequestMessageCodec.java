/**
 * 
 */
package com.zx.sms.codec.cmpp20;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;

import java.util.List;

import org.marre.sms.SmsDcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;
/**
 * shifei(shifei@asiainfo.com)
 */
public class Cmpp20SubmitRequestMessageCodec extends MessageToMessageCodec<Message, CmppSubmitRequestMessage> {
	private final Logger logger = LoggerFactory.getLogger(Cmpp20SubmitRequestMessageCodec.class);
	private PacketType packetType;

	/**
	 * 
	 */
	public Cmpp20SubmitRequestMessageCodec() {
		this(Cmpp20PacketType.CMPPSUBMITREQUEST);
	}

	public Cmpp20SubmitRequestMessageCodec(PacketType packetType) {
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

		CmppSubmitRequestMessage requestMessage = new CmppSubmitRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		requestMessage.setMsgid(DefaultMsgIdUtil.bytes2MsgId(toArray(bodyBuffer,Cmpp20SubmitRequest.MSGID.getLength())));

		requestMessage.setPktotal(bodyBuffer.readUnsignedByte());
		requestMessage.setPknumber(bodyBuffer.readUnsignedByte());

		requestMessage.setRegisteredDelivery(bodyBuffer.readUnsignedByte());
		requestMessage.setMsglevel(bodyBuffer.readUnsignedByte());
		requestMessage.setServiceId(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.SERVICEID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setFeeUserType(bodyBuffer.readUnsignedByte());

		requestMessage.setFeeterminalId(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.FEETERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		// requestMessage.setFeeterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		requestMessage.setTppid(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgfmt(new SmsDcs((byte)bodyBuffer.readUnsignedByte()));

		requestMessage.setMsgsrc(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.MSGSRC.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setFeeType(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.FEETYPE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setFeeCode(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.FEECODE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setValIdTime(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.VALIDTIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setAtTime(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.ATTIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setSrcId(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.SRCID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		short destUsrtl = bodyBuffer.readUnsignedByte();
		String[] destTermId = new String[destUsrtl];
		for (int i = 0; i < destUsrtl; i++) {
			destTermId[i] = bodyBuffer.readCharSequence(Cmpp20SubmitRequest.DESTTERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim();
		}
		requestMessage.setDestterminalId(destTermId);

		// requestMessage.setDestterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		short msgLength = (short)(bodyBuffer.readUnsignedByte() & 0xffff);

		byte[] contentbytes = new byte[msgLength];
		bodyBuffer.readBytes(contentbytes);
		requestMessage.setMsgContentBytes(contentbytes);
		requestMessage.setMsgLength((short)msgLength);
		requestMessage.setReserve(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.RESERVE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		ReferenceCountUtil.release(bodyBuffer);
		
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppSubmitRequestMessage requestMessage, List<Object> out) throws Exception {
		

			ByteBuf bodyBuffer = ctx.alloc().buffer(Cmpp20SubmitRequest.ATTIME.getBodyLength() + requestMessage.getMsgLength() + requestMessage.getDestUsrtl()
					* Cmpp20SubmitRequest.DESTTERMINALID.getLength());

			bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgid()));
			bodyBuffer.writeByte(requestMessage.getPktotal());
			bodyBuffer.writeByte(requestMessage.getPknumber());

			bodyBuffer.writeByte(requestMessage.getRegisteredDelivery());
			bodyBuffer.writeByte(requestMessage.getMsglevel());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getServiceId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.SERVICEID.getLength(), 0));

			bodyBuffer.writeByte(requestMessage.getFeeUserType());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getFeeterminalId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.FEETERMINALID.getLength(), 0));

			// bodyBuffer.writeByte(requestMessage.getFeeterminaltype());//CMPP2.0
			// 无该字段 不进行编解码
			bodyBuffer.writeByte(requestMessage.getTppid());
			bodyBuffer.writeByte(requestMessage.getTpudhi());
			bodyBuffer.writeByte(requestMessage.getMsgfmt().getValue());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getMsgsrc().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.MSGSRC.getLength(), 0));

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getFeeType().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.FEETYPE.getLength(), 0));

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getFeeCode().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.FEECODE.getLength(), 0));

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getValIdTime().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.VALIDTIME.getLength(), 0));

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getAtTime().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.ATTIME.getLength(), 0));

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getSrcId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.SRCID.getLength(), 0));

			bodyBuffer.writeByte(requestMessage.getDestUsrtl());

			for (int i = 0; i < requestMessage.getDestUsrtl(); i++) {
				String[] destTermId = requestMessage.getDestterminalId();
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(destTermId[i].getBytes(GlobalConstance.defaultTransportCharset),
						Cmpp20SubmitRequest.DESTTERMINALID.getLength(), 0));
			}

			// bodyBuffer.writeByte(requestMessage.getDestterminaltype());//CMPP2.0
			// 无该字段 不进行编解码

			bodyBuffer.writeByte(requestMessage.getMsgLength());

			bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());


			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.RESERVE.getLength(), 0));
			
			requestMessage.setBodyBuffer(toArray(bodyBuffer,bodyBuffer.readableBytes()));
			requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
			out.add(requestMessage);
			ReferenceCountUtil.release(bodyBuffer);
	}
}
