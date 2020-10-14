package com.zx.sms.connect.manager.smgp;

import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class SMGPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {
		
		if(msg instanceof SMGPDeliverMessage){
			SMGPDeliverRespMessage resp = new SMGPDeliverRespMessage();
		    resp.setSequenceNo(((SMGPDeliverMessage)msg).getSequenceNo());
		    resp.setMsgId( ((SMGPDeliverMessage)msg).getMsgId());
		    resp.setStatus(0);
		  
			return ctx.writeAndFlush(resp);
		}else if(msg instanceof SMGPSubmitMessage) {
			SMGPSubmitRespMessage resp = new SMGPSubmitRespMessage();
			resp.setSequenceNo(((SMGPSubmitMessage)msg).getSequenceNo());
		    resp.setStatus(0);
			return ctx.writeAndFlush(resp);
		}
		return null;
	}

}
