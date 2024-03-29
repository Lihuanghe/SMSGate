package com.zx.sms.connect.manager.cmpp;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class CMPPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof BaseMessage) {
			BaseMessage basemsg = (BaseMessage) msg;
			if (basemsg.isRequest()) {
				return ctx.newSucceededFuture();
			} else if (basemsg.isResponse()) {
				BaseMessage req = basemsg.getRequest();
				if (req instanceof LongSMSMessage) {

					logger.debug("send    : {}", 
							((LongSMSMessage) req).getUniqueLongMsgId());
				}
			}
		}
		return null;

	}

}
