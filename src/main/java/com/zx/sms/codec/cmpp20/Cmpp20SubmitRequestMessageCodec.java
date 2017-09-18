/**
 * 
 */
package com.zx.sms.codec.cmpp20;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;

import static com.zx.sms.common.util.NettyByteBufUtil.*;
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
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId) {
			// 不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppSubmitRequestMessage requestMessage = new CmppSubmitRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		requestMessage.setMsgid(DefaultMsgIdUtil.bytes2MsgId(toArray(bodyBuffer,Cmpp20SubmitRequest.MSGID.getLength())));
		LongMessageFrame frame = new LongMessageFrame();

		frame.setPktotal(bodyBuffer.readUnsignedByte());
		frame.setPknumber(bodyBuffer.readUnsignedByte());

		requestMessage.setRegisteredDelivery(bodyBuffer.readUnsignedByte());
		requestMessage.setMsglevel(bodyBuffer.readUnsignedByte());
		requestMessage.setServiceId(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.SERVICEID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setFeeUserType(bodyBuffer.readUnsignedByte());

		requestMessage.setFeeterminalId(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.FEETERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		// requestMessage.setFeeterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		frame.setTppid(bodyBuffer.readUnsignedByte());
		frame.setTpudhi(bodyBuffer.readUnsignedByte());
		frame.setMsgfmt(new SmsDcs((byte)bodyBuffer.readUnsignedByte()));

		requestMessage.setMsgsrc(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.MSGSRC.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setFeeType(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.FEETYPE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setFeeCode(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.FEECODE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setValIdTime(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.VALIDTIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setAtTime(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.ATTIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setSrcId(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.SRCID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setDestUsrtl(bodyBuffer.readUnsignedByte());
		String[] destTermId = new String[requestMessage.getDestUsrtl()];
		for (int i = 0; i < requestMessage.getDestUsrtl(); i++) {
			destTermId[i] = bodyBuffer.readCharSequence(Cmpp20SubmitRequest.DESTTERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim();
		}
		requestMessage.setDestterminalId(destTermId);

		// requestMessage.setDestterminaltype(bodyBuffer.readUnsignedByte());//CMPP2.0
		// 无该字段 不进行编解码

		short msgLength =(short) frame.getPayloadLength(bodyBuffer.readUnsignedByte());
		byte[] contentbytes = new byte[msgLength];
		bodyBuffer.readBytes(contentbytes);
		frame.setMsgContentBytes(contentbytes);
		frame.setMsgLength((short)msgLength);
		requestMessage.setReserve(bodyBuffer.readCharSequence(Cmpp20SubmitRequest.RESERVE.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		ReferenceCountUtil.release(bodyBuffer);
		try {
			SmsMessage content = LongMessageFrameHolder.INS.putAndget(StringUtils.join(destTermId, "|"), frame);

			if (content != null) {
				requestMessage.setMsgContent(content);
				out.add(requestMessage);
			} else {

				CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(msg.getHeader());

				responseMessage.setMsgId(requestMessage.getMsgid());
				responseMessage.setResult(0);
				ctx.channel().writeAndFlush(responseMessage);
			}
		} catch (Exception ex) {
			logger.error("",ex);
			//长短信解析失败，直接给网关回复 resp . 并丢弃这个短信
			logger.error("Decode CmppSubmitRequestMessage Error ,msg dump :{}" , ByteBufUtil.hexDump(msg.getBodyBuffer()));
			CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(msg.getHeader());
			responseMessage.setMsgId(new MsgId());
			responseMessage.setResult(0);
			ctx.channel().writeAndFlush(responseMessage);

		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppSubmitRequestMessage oldMsg, List<Object> out) throws Exception {
		if (oldMsg.getBodyBuffer() != null && oldMsg.getBodyBuffer().length == oldMsg.getHeader().getBodyLength()) {
			// bodybuffer不为空，说明该消息是已经编码过的。可能其它连接断了，消息通过这个连接再次发送，不需要再次编码
			out.add(oldMsg);
			return;
		}

		CmppSubmitRequestMessage requestMessage = oldMsg.clone();
		List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(requestMessage.getMsg(), requestMessage.isSupportLongMsg());
		boolean first = true;
		for (LongMessageFrame frame : frameList) {

			ByteBuf bodyBuffer = Unpooled.buffer(Cmpp20SubmitRequest.ATTIME.getBodyLength() + frame.getMsgLength() + requestMessage.getDestUsrtl()
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
			bodyBuffer.writeByte(frame.getMsgfmt().getValue());

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

			requestMessage.setMsgContent(frame.getContentPart());

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20SubmitRequest.RESERVE.getLength(), 0));

			if (first) {
				requestMessage.setBodyBuffer(byteBufreadarray(bodyBuffer));
				requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
				out.add(requestMessage);
				first = false;
			} else {
				CmppSubmitRequestMessage defaultMsg = requestMessage.clone();
				defaultMsg.getHeader().setSequenceId(DefaultSequenceNumberUtil.getSequenceNo());
				defaultMsg.setBodyBuffer(byteBufreadarray(bodyBuffer));
				defaultMsg.getHeader().setBodyLength(defaultMsg.getBodyBuffer().length);
				out.add(defaultMsg);
			}
			ReferenceCountUtil.release(bodyBuffer);
		}
	}
	private byte[] byteBufreadarray(ByteBuf buf){
		byte[] dst = new byte[ buf.readableBytes()];
		buf.readBytes(dst);
		return dst;
	}
}
