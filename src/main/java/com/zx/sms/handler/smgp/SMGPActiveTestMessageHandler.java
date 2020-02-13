package com.zx.sms.handler.smgp;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.zx.sms.codec.smgp.msg.SMGPActiveTestMessage;
import com.zx.sms.codec.smgp.msg.SMGPActiveTestRespMessage;

@Sharable
public class SMGPActiveTestMessageHandler extends SimpleChannelInboundHandler<SMGPActiveTestMessage>{
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SMGPActiveTestMessage msg) throws Exception {
		SMGPActiveTestRespMessage resp = new SMGPActiveTestRespMessage();
		resp.setSequenceNo(msg.getSequenceNo());
		ctx.writeAndFlush(resp);
	}}