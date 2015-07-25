/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class CmppQueryResponseMessageHandler extends SimpleChannelInboundHandler<CmppQueryResponseMessage> {
	private PacketType packetType;

	public CmppQueryResponseMessageHandler() {
		this(CmppPacketType.CMPPQUERYRESPONSE);
	}

	public CmppQueryResponseMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	public void messageReceived(ChannelHandlerContext ctx, CmppQueryResponseMessage e) throws Exception {

	}

}
