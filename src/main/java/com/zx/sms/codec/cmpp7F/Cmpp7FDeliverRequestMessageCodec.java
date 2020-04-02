/**
 * 
 */
package com.zx.sms.codec.cmpp7F;

import static com.zx.sms.common.util.NettyByteBufUtil.toArray;

import java.util.List;

import org.marre.sms.SmsDcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppDeliverRequest;
import com.zx.sms.codec.cmpp.packet.CmppReportRequest;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.cmpp7F.packet.Cmpp7FPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.FstObjectSerializeUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;
/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class Cmpp7FDeliverRequestMessageCodec extends MessageToMessageCodec<Message, CmppDeliverRequestMessage> {
	private final Logger logger = LoggerFactory.getLogger(Cmpp7FDeliverRequestMessageCodec.class);
	private PacketType packetType;

	/**
	 * 
	 */
	public Cmpp7FDeliverRequestMessageCodec() {
		this(Cmpp7FPacketType.CMPPDELIVERREQUEST);
	}

	public Cmpp7FDeliverRequestMessageCodec(PacketType packetType) {
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
		requestMessage.setMsgId(DefaultMsgIdUtil.bytes2MsgId(toArray(bodyBuffer,CmppDeliverRequest.MSGID.getLength())));
		requestMessage.setDestId(bodyBuffer.readCharSequence(CmppDeliverRequest.DESTID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setServiceid(bodyBuffer.readCharSequence(CmppDeliverRequest.SERVICEID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());

		requestMessage.setTppid(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgfmt(new SmsDcs((byte)bodyBuffer.readUnsignedByte()));

		requestMessage.setSrcterminalId(bodyBuffer.readCharSequence(CmppDeliverRequest.SRCTERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setSrcterminalType(bodyBuffer.readUnsignedByte());
		short registeredDelivery = bodyBuffer.readUnsignedByte();
		
		short frameLength = (short)(bodyBuffer.readUnsignedByte() & 0xffff);

		if (registeredDelivery == 0) {
			byte[] contentbytes = new byte[frameLength];
			bodyBuffer.readBytes(contentbytes);
			requestMessage.setMsgContentBytes(contentbytes);
			requestMessage.setMsgLength((short)frameLength);
		} else {
			requestMessage.setReportRequestMessage(new CmppReportRequestMessage());
			requestMessage.getReportRequestMessage().setMsgId(DefaultMsgIdUtil.bytes2MsgId(toArray(bodyBuffer,CmppReportRequest.MSGID.getLength())));
			requestMessage.getReportRequestMessage().setStat(
					bodyBuffer.readCharSequence(CmppReportRequest.STAT.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setSubmitTime(
					bodyBuffer.readCharSequence(CmppReportRequest.SUBMITTIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setDoneTime(
					bodyBuffer.readCharSequence(CmppReportRequest.DONETIME.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setDestterminalId(
					bodyBuffer.readCharSequence(CmppReportRequest.DESTTERMINALID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
			requestMessage.getReportRequestMessage().setSmscSequence(bodyBuffer.readUnsignedInt());
		}

		requestMessage.setLinkid(bodyBuffer.readCharSequence(CmppDeliverRequest.LINKID.getLength(),GlobalConstance.defaultTransportCharset).toString().trim());
		//在线公司自定义的字段
		int attach = bodyBuffer.readInt();
		if(attach != 0 ){
			byte[] objbytes = new byte[attach];
			bodyBuffer.readBytes(objbytes);
			try{
				requestMessage.setAttachment(FstObjectSerializeUtil.read(objbytes));
			}catch(Exception ex){
				logger.warn("Attachment decode error",ex);
			}
		}
		
		out.add(requestMessage);
		ReferenceCountUtil.release(bodyBuffer);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppDeliverRequestMessage requestMessage, List<Object> out) throws Exception {
		

			// bodyBuffer 会在CmppHeaderCodec.encode里释放
			ByteBuf bodyBuffer = ctx.alloc().buffer(CmppDeliverRequest.DESTID.getBodyLength() + requestMessage.getMsgLength());

			bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgId()));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getDestId().getBytes(GlobalConstance.defaultTransportCharset),
					CmppDeliverRequest.DESTID.getLength(), 0));
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getServiceid().getBytes(GlobalConstance.defaultTransportCharset),
					CmppDeliverRequest.SERVICEID.getLength(), 0));
			bodyBuffer.writeByte(requestMessage.getTppid());
			bodyBuffer.writeByte(requestMessage.getTpudhi());
			bodyBuffer.writeByte(requestMessage.getMsgfmt().getValue());
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getSrcterminalId().getBytes(GlobalConstance.defaultTransportCharset),
					CmppDeliverRequest.SRCTERMINALID.getLength(), 0));
			bodyBuffer.writeByte(requestMessage.getSrcterminalType());
			bodyBuffer.writeByte(requestMessage.isReport()?1:0);
			

			if (!requestMessage.isReport()) {
				bodyBuffer.writeByte(requestMessage.getMsgLength());

				bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());
				
			} else {
				bodyBuffer.writeByte(CmppReportRequest.DESTTERMINALID.getBodyLength());
				
				bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getReportRequestMessage().getMsgId()));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getStat().getBytes(GlobalConstance.defaultTransportCharset),
						CmppReportRequest.STAT.getLength(), 0));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getSubmitTime().getBytes(GlobalConstance.defaultTransportCharset),
						CmppReportRequest.SUBMITTIME.getLength(), 0));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getDoneTime().getBytes(GlobalConstance.defaultTransportCharset),
						CmppReportRequest.DONETIME.getLength(), 0));
				bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(
						requestMessage.getReportRequestMessage().getDestterminalId().getBytes(GlobalConstance.defaultTransportCharset),
						CmppReportRequest.DESTTERMINALID.getLength(), 0));

				bodyBuffer.writeInt((int) requestMessage.getReportRequestMessage().getSmscSequence());
			}

			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getLinkid().getBytes(GlobalConstance.defaultTransportCharset),
					CmppDeliverRequest.LINKID.getLength(), 0));
			//在线公司自定义的字段
			if(requestMessage.getAttachment()!=null){
				try{
					byte[] attach =FstObjectSerializeUtil.write(requestMessage.getAttachment());
					bodyBuffer.writeInt(attach.length);
					bodyBuffer.writeBytes(attach);
				}catch(Exception ex){
					logger.warn("Attachment Serializa error",ex);
					bodyBuffer.writeInt(0);
				}
			}else{
				bodyBuffer.writeInt(0);
			}

			requestMessage.setBodyBuffer(toArray(bodyBuffer,bodyBuffer.readableBytes()));
			requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
			out.add(requestMessage);
			ReferenceCountUtil.release(bodyBuffer);
	}

}
