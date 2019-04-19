package com.zx.sms.connect.manager.sgip;

import java.util.List;

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
			SgipDeliverRequestMessage deli = (SgipDeliverRequestMessage)msg;
			SgipDeliverResponseMessage resp = new SgipDeliverResponseMessage(deli.getHeader());
			resp.setResult((short)0);
			resp.setTimestamp(deli.getTimestamp());
			
			List<SgipDeliverRequestMessage> deliarr = deli.getFragments();
			if(deliarr!=null) {
				for(SgipDeliverRequestMessage item:deliarr) {
					SgipDeliverResponseMessage item_resp = new SgipDeliverResponseMessage(item.getHeader());
					item_resp.setResult((short)0);
					ctx.writeAndFlush(item_resp);
				}
			}
			
			return ctx.writeAndFlush(resp);
		}else if(msg instanceof SgipSubmitRequestMessage) {
			SgipSubmitRequestMessage submit = (SgipSubmitRequestMessage)msg;
			SgipSubmitResponseMessage resp = new SgipSubmitResponseMessage(submit.getHeader());
			resp.setResult((short)0);
			
			List<SgipSubmitRequestMessage> deliarr = submit.getFragments();
			if(deliarr!=null) {
				for(SgipSubmitRequestMessage item:deliarr) {
					SgipSubmitResponseMessage item_resp = new SgipSubmitResponseMessage(item.getHeader());
					item_resp.setResult((short)0);
					ctx.writeAndFlush(item_resp);
				}
			}
			
			return ctx.writeAndFlush(resp);
		}
		return null;
	}

}
