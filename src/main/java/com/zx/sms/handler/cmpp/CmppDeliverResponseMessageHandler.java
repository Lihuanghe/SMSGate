/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
@Sharable
public class CmppDeliverResponseMessageHandler extends SimpleChannelInboundHandler<CmppDeliverResponseMessage> {
	private PacketType packetType;
	private static final Logger logger = LoggerFactory.getLogger(CmppDeliverResponseMessageHandler.class);

	public CmppDeliverResponseMessageHandler(CMPPEndpointEntity entity) {
		this(CmppPacketType.CMPPDELIVERRESPONSE);

	}

	public CmppDeliverResponseMessageHandler(PacketType packetType) {
		this.packetType = packetType;

	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, CmppDeliverResponseMessage e) throws Exception {

	}

}
