/**
 * 
 */
package com.zx.sms.codec.cmpp20;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;

/**
 * shifei(shifei@asiainfo.com)
 */
public class Cmpp20SubmitRequestMessageCodec extends MessageToMessageCodec<Message, CmppSubmitRequestMessage> {
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
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId) {
			// 不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppSubmitRequestMessage requestMessage = new CmppSubmitRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer =msg.getBodyBuffer();

		requestMessage.setMsgid(DefaultMsgIdUtil.bytes2MsgId(bodyBuffer.readBytes(Cmpp20SubmitRequest.MSGID.getLength()).array()));
		requestMessage.setPktotal(bodyBuffer.readUnsignedByte());
		requestMessage.setPktotal(bodyBuffer.readUnsignedByte());
		requestMessage.setRegisteredDelivery(bodyBuffer.readUnsignedByte());
		requestMessage.setMsglevel(bodyBuffer.readUnsignedByte());
		requestMessage.setServiceId(bodyBuffer.readBytes(Cmpp20SubmitRequest.SERVICEID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setFeeUserType(bodyBuffer.readUnsignedByte());

		requestMessage.setFeeterminalId(bodyBuffer.readBytes(Cmpp20SubmitRequest.FEETERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset)
				.trim());
		// requestMessage.setFeeterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		requestMessage.setTppId(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgFmt(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgsrc(bodyBuffer.readBytes(Cmpp20SubmitRequest.MSGSRC.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setFeeType(bodyBuffer.readBytes(Cmpp20SubmitRequest.FEETYPE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setFeeCode(bodyBuffer.readBytes(Cmpp20SubmitRequest.FEECODE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setValIdTime(bodyBuffer.readBytes(Cmpp20SubmitRequest.VALIDTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setAtTime(bodyBuffer.readBytes(Cmpp20SubmitRequest.ATTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setSrcId(bodyBuffer.readBytes(Cmpp20SubmitRequest.SRCID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setDestUsrtl(bodyBuffer.readUnsignedByte());

		requestMessage.setDestterminalId(bodyBuffer.readBytes(Cmpp20SubmitRequest.DESTTERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset)
				.trim());

		// requestMessage.setDestterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		requestMessage.setMsgLength(bodyBuffer.readUnsignedByte());
		// 取短信编码
		Charset charset = CMPPCommonUtil.switchCharset(requestMessage.getMsgFmt());
		requestMessage.setMsgContent(bodyBuffer.readBytes(requestMessage.getMsgLength()).toString(charset).trim());

		requestMessage.setReserve(bodyBuffer.readBytes(Cmpp20SubmitRequest.RESERVE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppSubmitRequestMessage requestMessage, List<Object> out) throws Exception {
		ByteBuf bodyBuffer = ctx.alloc().buffer(Cmpp20SubmitRequest.ATTIME.getBodyLength() + requestMessage.getMsgLength());

		bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgid()));
		bodyBuffer.writeByte(requestMessage.getPknumber());
		bodyBuffer.writeByte(requestMessage.getPktotal());
		bodyBuffer.writeByte(requestMessage.getRegisteredDelivery());
		bodyBuffer.writeByte(requestMessage.getMsglevel());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getServiceId().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.SERVICEID.getLength(), 0));

		bodyBuffer.writeByte(requestMessage.getFeeUserType());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getFeeterminalId().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.FEETERMINALID.getLength(), 0));

		// bodyBuffer.writeByte(requestMessage.getFeeterminaltype());//CMPP2.0
		// 无该字段 不进行编解码
		bodyBuffer.writeByte(requestMessage.getTppId());
		bodyBuffer.writeByte(requestMessage.getTpudhi());
		bodyBuffer.writeByte(requestMessage.getMsgFmt());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getMsgsrc().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.MSGSRC.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getFeeType().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.FEETYPE.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getFeeCode().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.FEECODE.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getValIdTime().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.VALIDTIME.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getAtTime().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.ATTIME.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getSrcId().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.SRCID.getLength(), 0));

		bodyBuffer.writeByte(requestMessage.getDestUsrtl());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getDestterminalId().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.DESTTERMINALID.getLength(), 0));

		// bodyBuffer.writeByte(requestMessage.getDestterminaltype());//CMPP2.0
		// 无该字段 不进行编解码

		bodyBuffer.writeByte(requestMessage.getMsgLength());

		bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20SubmitRequest.RESERVE.getLength(), 0));

		requestMessage.setBodyBuffer(bodyBuffer);

		out.add(requestMessage);
	}

}
