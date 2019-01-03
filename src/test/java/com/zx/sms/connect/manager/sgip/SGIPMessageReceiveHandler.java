package com.zx.sms.connect.manager.sgip;

import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverResponseMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitResponseMessage;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class SGIPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {
		
		if(msg instanceof SgipDeliverRequestMessage){
			SgipDeliverResponseMessage resp = new SgipDeliverResponseMessage(((SgipDeliverRequestMessage)msg).getHeader());
			resp.setResult((short)0);
			return ctx.writeAndFlush(resp);
		}else if(msg instanceof SgipSubmitRequestMessage) {
			SgipSubmitResponseMessage resp = new SgipSubmitResponseMessage(((SgipSubmitRequestMessage)msg).getHeader());
			resp.setResult((short)0);
			return ctx.writeAndFlush(resp);
		}
		return null;
	}

}
