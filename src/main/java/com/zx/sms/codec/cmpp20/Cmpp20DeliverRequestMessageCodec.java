/**
 * 
 */
package com.zx.sms.codec.cmpp20;

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

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
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
	private final Logger logger = LoggerFactory.getLogger(Cmpp20DeliverRequestMessageCodec.class);
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

		int commandId =  msg.getHeader().getCommandId();
		if (packetType.getCommandId() != commandId) {
			// 不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppDeliverRequestMessage requestMessage = new CmppDeliverRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		requestMessage.setMsgId(DefaultMsgIdUtil.bytes2MsgId(toArray(bodyBuffer,Cmpp20DeliverRequest.MSGID.getLength())));
		requestMessage.setDestId(bodyBuffer.readCharSequence(Cmpp20DeliverRequest.DESTID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setServiceid(bodyBuffer.readCharSequence(Cmpp20DeliverRequest.SERVICEID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setTppid(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgfmt(new SmsDcs((byte)bodyBuffer.readUnsignedByte()));

		requestMessage.setSrcterminalId(bodyBuffer.readCharSequence(Cmpp20DeliverRequest.SRCTERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		// requestMessage.setSrcterminalType(bodyBuffer.readUnsignedByte());//CMPP2.0
		// SrcterminalType不进行编解码
		short registeredDelivery =bodyBuffer.readUnsignedByte();
		short frameLength = (short)(bodyBuffer.readUnsignedByte() & 0xffff);

		if (registeredDelivery == 0) {
			byte[] contentbytes = new byte[frameLength];
			bodyBuffer.readBytes(contentbytes);
			requestMessage.setMsgContentBytes(contentbytes);
			requestMessage.setMsgLength((short)frameLength);
		} else {
			if(frameLength != Cmpp20ReportRequest.DESTTERMINALID.getBodyLength()){
				logger.warn("CmppDeliverRequestMessage20 - MsgContent length is {}. should be {}.",frameLength ,Cmpp20ReportRequest.DESTTERMINALID.getBodyLength());
			};
			requestMessage.setReportRequestMessage(new CmppReportRequestMessage());
			requestMessage.getReportRequestMessage()
					.setMsgId(DefaultMsgIdUtil.bytes2MsgId(toArray(bodyBuffer,Cmpp20ReportRequest.MSGID.getLength())));
			requestMessage.getReportRequestMessage().setStat(
					bodyBuffer.readCharSequence(Cmpp20ReportRequest.STAT.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setSubmitTime(
					bodyBuffer.readCharSequence(Cmpp20ReportRequest.SUBMITTIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setDoneTime(
					bodyBuffer.readCharSequence(Cmpp20ReportRequest.DONETIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setDestterminalId(
					bodyBuffer.readCharSequence(Cmpp20ReportRequest.DESTTERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setSmscSequence(bodyBuffer.readUnsignedInt());
		}

		//剩下的字节全部读取
		requestMessage.setReserved(bodyBuffer.readCharSequence(Cmpp20DeliverRequest.RESERVED.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppDeliverRequestMessage requestMessage, List<Object> out) throws Exception {
		
			// bodyBuffer 会在CmppHeaderCodec.encode里释放
			ByteBuf bodyBuffer = ctx.alloc().buffer(Cmpp20DeliverRequest.DESTID.getBodyLength() + requestMessage.getMsgLength());

			bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgId()));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getDestId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20DeliverRequest.DESTID.getLength(), 0));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getServiceid().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20DeliverRequest.SERVICEID.getLength(), 0));
			bodyBuffer.writeByte(requestMessage.getTppid());
			bodyBuffer.writeByte(requestMessage.getTpudhi());
			bodyBuffer.writeByte(requestMessage.getMsgfmt().getValue());
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getSrcterminalId().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20DeliverRequest.SRCTERMINALID.getLength(), 0));

			// bodyBuffer.writeByte(requestMessage.getSrcterminalType());//CMPP2.0不编解码
			bodyBuffer.writeByte(requestMessage.isReport()?1:0);

			if (!requestMessage.isReport()) {
			
				bodyBuffer.writeByte(requestMessage.getMsgLength());
				bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());
			} else {
				bodyBuffer.writeByte(Cmpp20ReportRequest.DESTTERMINALID.getBodyLength());
				bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getReportRequestMessage().getMsgId()));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getStat().getBytes(GlobalConstance.defaultTransportCharset),
						Cmpp20ReportRequest.STAT.getLength(), 0));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getSubmitTime().getBytes(GlobalConstance.defaultTransportCharset),
						Cmpp20ReportRequest.SUBMITTIME.getLength(), 0));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getDoneTime().getBytes(GlobalConstance.defaultTransportCharset),
						Cmpp20ReportRequest.DONETIME.getLength(), 0));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getDestterminalId().getBytes(GlobalConstance.defaultTransportCharset),
						Cmpp20ReportRequest.DESTTERMINALID.getLength(), 0));

				bodyBuffer.writeInt((int) requestMessage.getReportRequestMessage().getSmscSequence());
			}

			/** CMPP2.0 LINKID字段不进行编解码 */
			// bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getLinkid()
			// .getBytes(GlobalConstance.defaultTransportCharset),
			// Cmpp20DeliverRequest.LINKID.getLength(), 0));

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserved().getBytes(GlobalConstance.defaultTransportCharset),
					Cmpp20DeliverRequest.RESERVED.getLength(), 0));


			requestMessage.setBodyBuffer(toArray(bodyBuffer,bodyBuffer.readableBytes()));
			requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
			out.add(requestMessage);
			ReferenceCountUtil.release(bodyBuffer);
	}
}
