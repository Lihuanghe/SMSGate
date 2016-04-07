/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppActiveTestResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
@Sharable
public class CmppActiveTestRequestMessageHandler extends SimpleChannelInboundHandler<CmppActiveTestRequestMessage> {
	private PacketType packetType;
	private static final Logger logger = LoggerFactory.getLogger(CmppActiveTestRequestMessageHandler.class);

	public CmppActiveTestRequestMessageHandler() {
		this(CmppPacketType.CMPPACTIVETESTREQUEST);
	}

	public CmppActiveTestRequestMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CmppActiveTestRequestMessage e) throws Exception {
		CmppActiveTestResponseMessage resp = new CmppActiveTestResponseMessage(e.getHeader().getSequenceId());
		ctx.writeAndFlush(resp);
	}

}
