package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import com.google.common.primitives.Bytes;
import com.zx.sms.codec.cmpp.msg.CmppConnectRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppConnectRequest;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppConnectRequestMessageCodec extends MessageToMessageCodec<Message, CmppConnectRequestMessage> {
	private PacketType packetType;

	public CmppConnectRequestMessageCodec() {
		this(CmppPacketType.CMPPCONNECTREQUEST);
	}

	public CmppConnectRequestMessageCodec(PacketType packetType) {
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
		CmppConnectRequestMessage requestMessage = new CmppConnectRequestMessage(msg.getHeader());

		ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
		requestMessage.setSourceAddr(bodyBuffer.readBytes(CmppConnectRequest.SOURCEADDR.getLength()).toString(GlobalConstance.defaultTransportCharset).trim());

		requestMessage.setAuthenticatorSource(bodyBuffer.readBytes(CmppConnectRequest.AUTHENTICATORSOURCE.getLength()).array());

		requestMessage.setVersion(bodyBuffer.readUnsignedByte());
		requestMessage.setTimestamp(bodyBuffer.readUnsignedInt());
		
		ReferenceCountUtil.release(bodyBuffer);
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppConnectRequestMessage msg, List<Object> out) throws Exception {

		ByteBuf bodyBuffer =  Unpooled.buffer(CmppConnectRequest.AUTHENTICATORSOURCE.getBodyLength());

		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getSourceAddr().getBytes(GlobalConstance.defaultTransportCharset),
				CmppConnectRequest.SOURCEADDR.getLength(), 0));
		bodyBuffer.writeBytes(CMPPCommonUtil.ensureLength(msg.getAuthenticatorSource(),CmppConnectRequest.AUTHENTICATORSOURCE.getLength(),0));
		bodyBuffer.writeByte(msg.getVersion());
		bodyBuffer.writeInt((int) msg.getTimestamp());

		msg.setBodyBuffer(bodyBuffer.array());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(msg);
	}

}
