/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class CmppSubmitResponseMessageHandler extends SimpleChannelInboundHandler<CmppSubmitResponseMessage> {
	private PacketType packetType;

	/**
	 * 
	 */
	public CmppSubmitResponseMessageHandler() {
		this(CmppPacketType.CMPPSUBMITRESPONSE);
	}

	public CmppSubmitResponseMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, CmppSubmitResponseMessage e) throws Exception {

	}

}
