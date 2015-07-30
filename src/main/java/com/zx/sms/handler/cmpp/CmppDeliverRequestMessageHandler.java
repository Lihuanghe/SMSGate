/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

/**
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppDeliverRequestMessageHandler extends SimpleChannelInboundHandler<CmppDeliverRequestMessage> {
	private PacketType packetType;
	private static final Logger logger = LoggerFactory.getLogger(CmppDeliverRequestMessageHandler.class);


	public CmppDeliverRequestMessageHandler(CMPPEndpointEntity entity) {
		this(CmppPacketType.CMPPDELIVERREQUEST);
	
	}

	public CmppDeliverRequestMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	public void messageReceived(ChannelHandlerContext ctx, CmppDeliverRequestMessage e) throws Exception {

		CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
		responseMessage.setMsgId(e.getMsgId());
		responseMessage.setResult(0);
		ctx.writeAndFlush(responseMessage);
	}

}
