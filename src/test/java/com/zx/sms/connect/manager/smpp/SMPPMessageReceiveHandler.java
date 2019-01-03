package com.zx.sms.connect.manager.smpp;

import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.codec.smpp.msg.SubmitSmResp;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class SMPPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {
		
		if(msg instanceof DeliverSm){
			DeliverSmResp res = ((DeliverSm)msg).createResponse();
			res.setMessageId(String.valueOf(System.currentTimeMillis()));
			return ctx.writeAndFlush(res);
		}else if(msg instanceof SubmitSm) {
			SubmitSmResp res = ((SubmitSm)msg).createResponse();
			res.setMessageId(String.valueOf(System.currentTimeMillis()));
			return ctx.writeAndFlush(res);
		}
		return null;
	}

}
