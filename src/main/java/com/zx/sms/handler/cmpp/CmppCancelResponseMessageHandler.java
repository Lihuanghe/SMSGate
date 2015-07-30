/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.zx.sms.codec.cmpp.msg.CmppCancelResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class CmppCancelResponseMessageHandler extends
SimpleChannelInboundHandler<CmppCancelResponseMessage> {
	private PacketType packetType;
	
	public CmppCancelResponseMessageHandler() {
		this(CmppPacketType.CMPPCANCELRESPONSE);
	}

	public CmppCancelResponseMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	public void messageReceived(ChannelHandlerContext ctx, CmppCancelResponseMessage e)
			throws Exception {

        
        CmppCancelResponseMessage responseMessage = (CmppCancelResponseMessage) e;
       
	}
	
	
}
