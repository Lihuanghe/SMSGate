package com.zx.sms.handler.smgp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

import com.zx.sms.codec.smgp.msg.SMGPExitMessage;
import com.zx.sms.codec.smgp.msg.SMGPExitRespMessage;
@Sharable
public class SMGPExitMessageHandler extends SimpleChannelInboundHandler<SMGPExitMessage>{

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, SMGPExitMessage msg) throws Exception {
		SMGPExitRespMessage resp = new SMGPExitRespMessage();
		resp.setSequenceNo(msg.getSequenceNo());
		ChannelFuture future = ctx.channel().writeAndFlush(resp);
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
		
		
	}}