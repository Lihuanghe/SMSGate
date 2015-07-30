package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.zx.sms.codec.cmpp.msg.CmppConnectRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppConnectResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 */
@Deprecated
public class CmppConnectRequestMessageHandler extends SimpleChannelInboundHandler<CmppConnectRequestMessage> {
	private PacketType packetType;

	/**
     * 
     */
	public CmppConnectRequestMessageHandler() {
		this(CmppPacketType.CMPPCONNECTREQUEST);
	}

	public CmppConnectRequestMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, CmppConnectRequestMessage message) throws Exception {
		long commandId = ((Long) message.getHeader().getCommandId()).longValue();

		CmppConnectRequestMessage connectRequestMessage = (CmppConnectRequestMessage) message;
		CmppConnectResponseMessage connectResponseMessage = new CmppConnectResponseMessage(message.getHeader().getSequenceId());

		byte[] timestampBytes = String.format("%010d", connectRequestMessage.getTimestamp()).getBytes(GlobalConstance.defaultTransportCharset);
		byte[] authBytes = DigestUtils.md5(Bytes.concat(new byte[0], new byte[9], new byte[0], timestampBytes));

		if (!Arrays.equals(authBytes, connectRequestMessage.getAuthenticatorSource())) {
			ctx.channel().write(connectResponseMessage);
			ctx.channel().close();
			return;
		}
		connectResponseMessage.setStatus(0L);

		connectResponseMessage.setAuthenticatorISMG(DigestUtils.md5(Bytes.concat(Ints.toByteArray((int) connectResponseMessage.getStatus()),
				connectRequestMessage.getAuthenticatorSource(), "password".getBytes(GlobalConstance.defaultTransportCharset))));

		ctx.channel().write(connectResponseMessage);

	}

}
