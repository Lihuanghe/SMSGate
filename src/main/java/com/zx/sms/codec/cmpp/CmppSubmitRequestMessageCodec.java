/**
 * 
 */
package com.zx.sms.codec.cmpp;

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
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.CmppSubmitRequest;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 *@author Lihuanghe(18852780@qq.com)
 */
public class CmppSubmitRequestMessageCodec extends MessageToMessageCodec<Message, CmppSubmitRequestMessage> {
	private PacketType packetType;

	/**
	 * 
	 */
	public CmppSubmitRequestMessageCodec() {
		this(CmppPacketType.CMPPSUBMITREQUEST);
	}

	public CmppSubmitRequestMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId)
		{
			//不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppSubmitRequestMessage requestMessage = new CmppSubmitRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = msg.getBodyBuffer();

		requestMessage.setMsgid(DefaultMsgIdUtil.bytes2MsgId(bodyBuffer.readBytes(CmppSubmitRequest.MSGID.getLength()).array()));
		requestMessage.setPktotal(bodyBuffer.readUnsignedByte());
		requestMessage.setPktotal(bodyBuffer.readUnsignedByte());
		requestMessage.setRegisteredDelivery(bodyBuffer.readUnsignedByte());
		requestMessage.setMsglevel(bodyBuffer.readUnsignedByte());
		requestMessage.setServiceId(bodyBuffer.readBytes(CmppSubmitRequest.SERVICEID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
		requestMessage.setFeeUserType(bodyBuffer.readUnsignedByte());

		requestMessage.setFeeterminalId(bodyBuffer.readBytes(CmppSubmitRequest.FEETERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset)
				.trim());

		requestMessage.setFeeterminaltype(bodyBuffer.readUnsignedByte());
		requestMessage.setTppId(bodyBuffer.readUnsignedByte());
		requestMessage.setTpudhi(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgFmt(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgsrc(bodyBuffer.readBytes(CmppSubmitRequest.MSGSRC.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setFeeType(bodyBuffer.readBytes(CmppSubmitRequest.FEETYPE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setFeeCode(bodyBuffer.readBytes(CmppSubmitRequest.FEECODE.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setValIdTime(bodyBuffer.readBytes(CmppSubmitRequest.VALIDTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setAtTime(bodyBuffer.readBytes(CmppSubmitRequest.ATTIME.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setSrcId(bodyBuffer.readBytes(CmppSubmitRequest.SRCID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setDestUsrtl(bodyBuffer.readUnsignedByte());

		requestMessage.setDestterminalId(bodyBuffer.readBytes(CmppSubmitRequest.DESTTERMINALID.getLength()).toString(GlobalConstance.defaultTransportCharset)
				.trim());

		requestMessage.setDestterminaltype(bodyBuffer.readUnsignedByte());
		requestMessage.setMsgLength(bodyBuffer.readUnsignedByte());
		//取短信编码
		Charset charset = CMPPCommonUtil.switchCharset(requestMessage.getMsgFmt());
		requestMessage.setMsgContent(bodyBuffer.readBytes(requestMessage.getMsgLength()).toString(charset).trim());

		requestMessage.setLinkID(bodyBuffer.readBytes(CmppSubmitRequest.LINKID.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());
//		requestMessage.setReserve(bodyBuffer.readBytes(CmppSubmitRequest.RESERVE.getLength()).toString(GlobalConstance.defaultTransportCharset)
//				.trim()); /** CMPP3.0 无该字段 不解码*/
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppSubmitRequestMessage requestMessage, List<Object> out) throws Exception {
		ByteBuf bodyBuffer = ctx.alloc().buffer(CmppSubmitRequest.ATTIME.getBodyLength() + requestMessage.getMsgLength());

		bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(requestMessage.getMsgid()));
		bodyBuffer.writeByte(requestMessage.getPknumber());
		bodyBuffer.writeByte(requestMessage.getPktotal());
		bodyBuffer.writeByte(requestMessage.getRegisteredDelivery());
		bodyBuffer.writeByte(requestMessage.getMsglevel());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getServiceId().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.SERVICEID.getLength(), 0));

		bodyBuffer.writeByte(requestMessage.getFeeUserType());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getFeeterminalId().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.FEETERMINALID.getLength(), 0));
		bodyBuffer.writeByte(requestMessage.getFeeterminaltype());
		bodyBuffer.writeByte(requestMessage.getTppId());
		bodyBuffer.writeByte(requestMessage.getTpudhi());
		bodyBuffer.writeByte(requestMessage.getMsgFmt());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getMsgsrc().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.MSGSRC.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getFeeType().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.FEETYPE.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getFeeCode().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.FEECODE.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getValIdTime().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.VALIDTIME.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getAtTime().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.ATTIME.getLength(), 0));

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getSrcId().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.SRCID.getLength(), 0));

		bodyBuffer.writeByte(requestMessage.getDestUsrtl());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getDestterminalId().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.DESTTERMINALID.getLength(), 0));

		bodyBuffer.writeByte(requestMessage.getDestterminaltype());
		bodyBuffer.writeByte(requestMessage.getMsgLength());

		bodyBuffer.writeBytes(requestMessage.getMsgContentBytes());

		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getLinkID().getBytes(GlobalConstance.defaultTransportCharset),
				CmppSubmitRequest.LINKID.getLength(), 0));
		
//		bodyBuffer.writeBytes(Bytes.ensureCapacity(requestMessage.getReserve().getBytes(GlobalConstance.defaultTransportCharset),
//				CmppSubmitRequest.RESERVE.getLength(), 0));/**cmpp3.0 无该字段，不进行编解码 */

		requestMessage.setBodyBuffer(bodyBuffer);

		out.add(requestMessage);
	}

}
