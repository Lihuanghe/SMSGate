/**
 * 
 */
package com.zx.sms.codec.cmpp20;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.nio.charset.Charset;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20DeliverRequest;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20ReportRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;

/**
 * shifei(shifei@asiainfo.com)
 */
public class Cmpp20DeliverRequestMessageCodec extends MessageToMessageCodec<Message, CmppDeliverRequestMessage> {
	private PacketType packetType;

	/**
	 * 
	 */
	public Cmpp20DeliverRequestMessageCodec() {
		this(Cmpp20PacketType.CMPPDELIVERREQUEST);
	}

	public Cmpp20DeliverRequestMessageCodec(PacketType packetType) {
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

		CmppDeliverRequestMessage requestMessage = new CmppDeliverRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		requestMessage.setMsgId(DefaultMsgIdUtil.bytes2MsgId(bodyBuffer.readBytes(Cmpp20DeliverRequest.MSGID.getLength()).array()));
		requestMessage.setDestId(bodyBuffer.readBytes(Cmpp20DeliverRequest.DESTID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setServiceid(bodyBuffer.readBytes(Cmpp20DeliverRequest.SERVICEID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setTppid(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgfmt(bodyBuffer.readUnsignedByte());
		// 取短信编码
		Charset charset = CMPPCommonUtil.switchCharset(requestMessage.getMsgfmt());

		requestMessage.setSrcterminalId(bodyBuffer.readBytes(Cmpp20DeliverRequest.SRCTERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset)
				.trim());

		// requestMessage.setSrcterminalType(bodyBuffer.readUnsignedByte());//CMPP2.0
		// SrcterminalType不进行编解码
		requestMessage.setRegisteredDelivery(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgLength(bodyBuffer.readUnsignedByte());

		if (requestMessage.getRegisteredDelivery() == 0) {
			requestMessage.setMsgContent(bodyBuffer.readBytes(requestMessage.getMsgLength()).toString(charset));
		} else {
			requestMessage.setReportRequestMessage(new CmppReportRequestMessage());
			requestMessage.getReportRequestMessage()
					.setMsgId(DefaultMsgIdUtil.bytes2MsgId(bodyBuffer.readBytes(Cmpp20ReportRequest.MSGID.getLength()).array()));
			requestMessage.getReportRequestMessage().setStat(
					bodyBuffer.readBytes(Cmpp20ReportRequest.STAT.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
			requestMessage.getReportRequestMessage().setSubmitTime(
					bodyBuffer.readBytes(Cmpp20ReportRequest.SUBMITTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
			requestMessage.getReportRequestMessage().setDoneTime(
					bodyBuffer.readBytes(Cmpp20ReportRequest.DONETIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
			requestMessage.getReportRequestMessage().setDestterminalId(
					bodyBuffer.readBytes(Cmpp20ReportRequest.DESTTERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
			requestMessage.getReportRequestMessage().setSmscSequence(bodyBuffer.readUnsignedInt());
		}

		/** CMPP2.0 LINKID 不进行编解码 */
		// requestMessage.setLinkid(bodyBuffer.readBytes(
		// Cmpp20DeliverRequest.LINKID.getLength()).toString(
		// GlobalConstance.defaultTransportCharset));

		requestMessage.setReserved(bodyBuffer.readBytes(Cmpp20DeliverRequest.RESERVED.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppDeliverRequestMessage requestMessage, List<Object> out) throws Exception {

		// bodyBuffer 会在CmppHeaderCodec.encode里释放
		ByteBuf bodyBuffer = ctx.alloc().buffer(Cmpp20DeliverRequest.DESTID.getBodyLength() + requestMessage.getMsgLength());

		bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgId()));
		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getDestId().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20DeliverRequest.DESTID.getLength(), 0));
		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getServiceid().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20DeliverRequest.SERVICEID.getLength(), 0));
		bodyBuffer.writeByte(requestMessage.getTppid());
		bodyBuffer.writeByte(requestMessage.getTpudhi());
		bodyBuffer.writeByte(requestMessage.getMsgfmt());
		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getSrcterminalId().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20DeliverRequest.SRCTERMINALID.getLength(), 0));

		// bodyBuffer.writeByte(requestMessage.getSrcterminalType());//CMPP2.0不编解码
		bodyBuffer.writeByte(requestMessage.getRegisteredDelivery());
		bodyBuffer.writeByte(requestMessage.getMsgLength());

		if (!requestMessage.isReport()) {
			bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());
		} else {
			bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getReportRequestMessage().getMsgId()));
			bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getReportRequestMessage().getStat().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20ReportRequest.STAT.getLength(), 0));
			bodyBuffer.writeBytes(Bytes.ensureCapacity(
					requestMessage.getReportRequestMessage().getSubmitTime().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20ReportRequest.SUBMITTIME.getLength(), 0));
			bodyBuffer.writeBytes(Bytes.ensureCapacity(
					requestMessage.getReportRequestMessage().getDoneTime().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20ReportRequest.DONETIME.getLength(), 0));
			bodyBuffer.writeBytes(Bytes.ensureCapacity(
					requestMessage.getReportRequestMessage().getDestterminalId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20ReportRequest.DESTTERMINALID.getLength(), 0));

			bodyBuffer.writeInt((int) requestMessage.getReportRequestMessage().getSmscSequence());
		}

		/** CMPP2.0 LINKID字段不进行编解码 */
		// bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getLinkid()
		// .getBytes(GlobalConstance.defaultTransportCharset),
		// Cmpp20DeliverRequest.LINKID.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getReserved().getBytes(GlobalConstance.defaultTransportCharset),
				Cmpp20DeliverRequest.RESERVED.getLength(), 0));

		requestMessage.setBodyBuffer(bodyBuffer);
		out.add(requestMessage);
	}

}
