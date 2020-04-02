/**
 * 
 */
package com.zx.sms.codec.sgip12.codec;

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
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.codec.sgip12.packet.SgipSubmitRequest;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import static com.zx.sms.common.util.NettyByteBufUtil.toArray;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class SgipSubmitRequestMessageCodec extends MessageToMessageCodec<Message, SgipSubmitRequestMessage> {
	private final static Logger logger = LoggerFactory.getLogger(SgipSubmitRequestMessageCodec.class);
	private PacketType packetType;

	/**
	 * 
	 */
	public SgipSubmitRequestMessageCodec() {
		this(SgipPacketType.SUBMITREQUEST);
	}

	public SgipSubmitRequestMessageCodec(PacketType packetType) {
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

		SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage(msg.getHeader());
		requestMessage.setTimestamp(msg.getTimestamp());
		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());

		requestMessage.setSpnumber(bodyBuffer.readCharSequence(SgipSubmitRequest.SPNUMBER.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());
		requestMessage.setChargenumber(bodyBuffer.readCharSequence(SgipSubmitRequest.CHARGENUMBER.getLength(), GlobalConstance.defaultTransportCharset)
				.toString().trim());

		int usercount = bodyBuffer.readUnsignedByte();
		
		String[] nums = new String[usercount];
		for (int i = 0; i < usercount; i++) {
			nums[i] = bodyBuffer.readCharSequence(SgipSubmitRequest.USERNUMBER.getLength(), GlobalConstance.defaultTransportCharset)
					.toString().trim();
		}
		requestMessage.setUsernumber(nums);

		requestMessage.setCorpid(bodyBuffer.readCharSequence(SgipSubmitRequest.CORPID.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());
		requestMessage.setServicetype(bodyBuffer.readCharSequence(SgipSubmitRequest.SERVICETYPE.getLength(), GlobalConstance.defaultTransportCharset)
				.toString().trim());
		requestMessage.setFeetype(bodyBuffer.readUnsignedByte());
		requestMessage.setFeevalue(bodyBuffer.readCharSequence(SgipSubmitRequest.FEEVALUE.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());
		requestMessage.setGivenvalue(bodyBuffer.readCharSequence(SgipSubmitRequest.GIVENVALUE.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());
		requestMessage.setAgentflag(bodyBuffer.readUnsignedByte());
		requestMessage.setMorelatetomtflag(bodyBuffer.readUnsignedByte());
		requestMessage.setPriority(bodyBuffer.readUnsignedByte());
		requestMessage.setExpiretime(bodyBuffer.readCharSequence(SgipSubmitRequest.EXPIRETIME.getLength(), GlobalConstance.defaultTransportCharset).toString()
				.trim());
		requestMessage.setScheduletime(bodyBuffer.readCharSequence(SgipSubmitRequest.SCHEDULETIME.getLength(), GlobalConstance.defaultTransportCharset)
				.toString().trim());
		requestMessage.setReportflag(bodyBuffer.readUnsignedByte());
		requestMessage.setTppid(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgfmt(new SmsDcs((byte) bodyBuffer.readUnsignedByte()));

		requestMessage.setMessagetype(bodyBuffer.readUnsignedByte());
		int msgLength = bodyBuffer.readInt() ;

		byte[] contentbytes = new byte[msgLength];
		bodyBuffer.readBytes(contentbytes);
		requestMessage.setMsgContentBytes(contentbytes);
		requestMessage.setMessagelength(msgLength);

		requestMessage
				.setReserve(bodyBuffer.readCharSequence(SgipSubmitRequest.RESERVE.getLength(), GlobalConstance.defaultTransportCharset).toString().trim());

		out.add(requestMessage);
		ReferenceCountUtil.release(bodyBuffer);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipSubmitRequestMessage requestMessage, List<Object> out) throws Exception {

		assert (requestMessage.getUsercount() > 0);

		ByteBuf bodyBuffer = ctx.alloc().buffer(SgipSubmitRequest.SPNUMBER.getBodyLength() + (requestMessage.getUsercount() - 1)
				* SgipSubmitRequest.USERNUMBER.getLength() + requestMessage.getMessagelength());

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getSpnumber().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.SPNUMBER.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getChargenumber().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.CHARGENUMBER.getLength(), 0));
		int usercount = requestMessage.getUsercount();
		bodyBuffer.writeByte(usercount);
		for (int i = 0; i < usercount; i++) {
			String[] destTermId = requestMessage.getUsernumber();
			bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(destTermId[i].getBytes(GlobalConstance.defaultTransportCharset),
					SgipSubmitRequest.USERNUMBER.getLength(), 0));
		}

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getCorpid().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.CORPID.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getServicetype().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.SERVICETYPE.getLength(), 0));
		bodyBuffer.writeByte(requestMessage.getFeetype());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getFeevalue().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.FEEVALUE.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getGivenvalue().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.GIVENVALUE.getLength(), 0));
		bodyBuffer.writeByte(requestMessage.getAgentflag());
		bodyBuffer.writeByte(requestMessage.getMorelatetomtflag());
		bodyBuffer.writeByte(requestMessage.getPriority());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getExpiretime().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.EXPIRETIME.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getScheduletime().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.SCHEDULETIME.getLength(), 0));
		bodyBuffer.writeByte(requestMessage.getReportflag());
		bodyBuffer.writeByte(requestMessage.getTppid());
		bodyBuffer.writeByte(requestMessage.getTpudhi());
		bodyBuffer.writeByte(requestMessage.getMsgfmt().getValue());
		bodyBuffer.writeByte(requestMessage.getMessagetype());
		bodyBuffer.writeInt((int) requestMessage.getMessagelength());
		bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
				SgipSubmitRequest.RESERVE.getLength(), 0));

		requestMessage.setBodyBuffer(toArray(bodyBuffer, bodyBuffer.readableBytes()));
		requestMessage.getHeader().setBodyLength(requestMessage.getBodyBuffer().length);
		out.add(requestMessage);
		ReferenceCountUtil.release(bodyBuffer);
	}
}
