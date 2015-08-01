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

import org.apache.commons.lang.StringUtils;

import com.google.common.primitives.Bytes;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppSubmitRequest;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.LongMessageFrameHolder;

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

		ByteBuf bodyBuffer = msg.getBodyBuffer();

		requestMessage.setMsgid(DefaultMsgIdUtil.bytes2MsgId(bodyBuffer.readBytes(Cmpp20SubmitRequest.MSGID.getLength()).array()));
		LongMessageFrame frame = new LongMessageFrame();

		frame.setPktotal(bodyBuffer.readUnsignedByte());
		frame.setPknumber(bodyBuffer.readUnsignedByte());

		requestMessage.setRegisteredDelivery(bodyBuffer.readUnsignedByte());
		requestMessage.setMsglevel(bodyBuffer.readUnsignedByte());
		requestMessage.setServiceId(bodyBuffer.readBytes(Cmpp20SubmitRequest.SERVICEID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setFeeUserType(bodyBuffer.readUnsignedByte());

		requestMessage.setFeeterminalId(bodyBuffer.readBytes(Cmpp20SubmitRequest.FEETERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset)
				.trim());
		// requestMessage.setFeeterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		frame.setTppid(bodyBuffer.readUnsignedByte());
		frame.setTpudhi(bodyBuffer.readUnsignedByte());
		frame.setMsgfmt(bodyBuffer.readUnsignedByte());

		requestMessage.setMsgsrc(bodyBuffer.readBytes(Cmpp20SubmitRequest.MSGSRC.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setFeeType(bodyBuffer.readBytes(Cmpp20SubmitRequest.FEETYPE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setFeeCode(bodyBuffer.readBytes(Cmpp20SubmitRequest.FEECODE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setValIdTime(bodyBuffer.readBytes(Cmpp20SubmitRequest.VALIDTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setAtTime(bodyBuffer.readBytes(Cmpp20SubmitRequest.ATTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setSrcId(bodyBuffer.readBytes(Cmpp20SubmitRequest.SRCID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setDestUsrtl(bodyBuffer.readUnsignedByte());
		String[] destTermId = new String[requestMessage.getDestUsrtl()];
		for (int i = 0; i < requestMessage.getDestUsrtl(); i++) {
			destTermId[i] = bodyBuffer.readBytes(Cmpp20SubmitRequest.DESTTERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim();
		}
		requestMessage.setDestterminalId(destTermId);

		// requestMessage.setDestterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		short msgLength = bodyBuffer.readUnsignedByte();
		byte[] contentbytes = new byte[msgLength];
		bodyBuffer.readBytes(contentbytes);
		frame.setMsgContentBytes(contentbytes);

		requestMessage.setReserve(bodyBuffer.readBytes(Cmpp20SubmitRequest.RESERVE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		ReferenceCountUtil.release(bodyBuffer);

		String content = LongMessageFrameHolder.INS.putAndget(StringUtils.join(destTermId, "|"), frame);

		if (content != null) {
			requestMessage.setMsgContent(content);
			out.add(requestMessage);
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppSubmitRequestMessage requestMessage, List<Object> out) throws Exception {

		List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(requestMessage.getMsgContent(), requestMessage.isSupportLongMsg());
		boolean first = true;
		for (LongMessageFrame frame : frameList) {

			ByteBuf bodyBuffer = ctx.alloc().buffer(
					Cmpp20SubmitRequest.ATTIME.getBodyLength() + frame.getMsgLength() + requestMessage.getDestUsrtl()
							* Cmpp20SubmitRequest.DESTTERMINALID.getLength());

			bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgid()));
			bodyBuffer.writeByte(frame.getPktotal());
			bodyBuffer.writeByte(frame.getPknumber());

			bodyBuffer.writeByte(requestMessage.getRegisteredDelivery());
			bodyBuffer.writeByte(requestMessage.getMsglevel());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getServiceId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.SERVICEID.getLength(), 0));

			bodyBuffer.writeByte(requestMessage.getFeeUserType());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getFeeterminalId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.FEETERMINALID.getLength(), 0));

			// bodyBuffer.writeByte(requestMessage.getFeeterminaltype());//CMPP2.0
			// 无该字段 不进行编解码
			bodyBuffer.writeByte(frame.getTppid());
			bodyBuffer.writeByte(frame.getTpudhi());
			bodyBuffer.writeByte(frame.getMsgfmt());

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

			bodyBuffer.writeByte(frame.getMsgLength());

			bodyBuffer.writeBytes(frame.getMsgContentBytes());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.RESERVE.getLength(), 0));

			if (first) {
				DefaultMessage defaultMsg = new DefaultMessage();
				defaultMsg.setHeader(requestMessage.getHeader());
				defaultMsg.setBodyBuffer(bodyBuffer);
				out.add(defaultMsg);
				first = false;
			} else {
				DefaultMessage defaultMsg = new DefaultMessage(requestMessage.getPacketType());
				defaultMsg.setBodyBuffer(bodyBuffer);
				out.add(defaultMsg);
			}
		}
	}

}
