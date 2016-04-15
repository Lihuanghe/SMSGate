/**
 * 
 */
package com.zx.sms.handler.cmpp;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import com.zx.sms.codec.cmpp.msg.CmppTerminateRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppTerminateResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
@Sharable
public class CmppTerminateRequestMessageHandler extends SimpleChannelInboundHandler<CmppTerminateRequestMessage> {
	private PacketType packetType;

	public CmppTerminateRequestMessageHandler() {
		this(CmppPacketType.CMPPTERMINATEREQUEST);
	}

	public CmppTerminateRequestMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(
	 * org.jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(final ChannelHandlerContext ctx, CmppTerminateRequestMessage e) throws Exception {

		CmppTerminateResponseMessage responseMessage = new CmppTerminateResponseMessage(e.getHeader().getSequenceId());
		ChannelFuture future = ctx.channel().writeAndFlush(responseMessage);
		final ChannelHandlerContext finalctx = ctx;
		future.addListeners(new GenericFutureListener(){
			@Override
			public void operationComplete(Future future) throws Exception {
				ctx.executor().schedule(new Runnable(){

					@Override
					public void run() {
						finalctx.channel().close();
					}
					
					
				},500,TimeUnit.MILLISECONDS);
				
			}
		});
		
	}

}
