package com.zx.sms.handler.smpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

import com.zx.sms.codec.smpp.msg.UnbindResp;
@Sharable
public class UnbindRespMessageHandler extends SimpleChannelInboundHandler<UnbindResp>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, UnbindResp msg) throws Exception {
		ctx.channel().close();
	}}