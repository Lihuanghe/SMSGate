/**
 * 
 */
package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.session.cmpp.SessionState;

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
	public void messageReceived(ChannelHandlerContext ctx, CmppDeliverResponseMessage e) throws Exception {

	}

}
