package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppConnectResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 */
@Deprecated
public class CmppConnectResponseMessageHandler extends SimpleChannelInboundHandler<CmppConnectResponseMessage> {
	private final static Logger logger = LoggerFactory.getLogger(CmppConnectResponseMessageHandler.class);
	private PacketType packetType;

	/**
     * 
     */
	public CmppConnectResponseMessageHandler() {
		this(CmppPacketType.CMPPCONNECTRESPONSE);
	}

	public CmppConnectResponseMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	public void channelRead0(ChannelHandlerContext ctx, CmppConnectResponseMessage message) throws Exception {

		logger.info(message.toString());

	}

}
