/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.zx.sms.codec.cmpp.msg.CmppCancelRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppCancelResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class CmppCancelRequestMessageHandler extends
SimpleChannelInboundHandler<CmppCancelRequestMessage> {
	private PacketType packetType;
	
	public CmppCancelRequestMessageHandler() {
		this(CmppPacketType.CMPPCANCELREQUEST);
	}

	public CmppCancelRequestMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	public void channelRead0(ChannelHandlerContext ctx, CmppCancelRequestMessage e)
			throws Exception {
		CmppCancelResponseMessage responseMessage = new CmppCancelResponseMessage(e.getHeader().getSequenceId());
		
		responseMessage.setSuccessId(0L);
		
		ctx.channel().writeAndFlush(responseMessage);

	}
	
}
