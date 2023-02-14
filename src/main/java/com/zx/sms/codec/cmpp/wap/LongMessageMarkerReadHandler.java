package com.zx.sms.codec.cmpp.wap;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;

import io.netty.channel.ChannelHandlerContext;

public class LongMessageMarkerReadHandler extends AbstractBusinessHandler {
	private EndpointEntity entity;

	public LongMessageMarkerReadHandler(EndpointEntity entity) {
		this.entity = entity;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof LongSMSMessage && ((LongSMSMessage) msg).needHandleLongMessage()) {
			LongSMSMessage lmsg = (LongSMSMessage) msg;
			lmsg.setUniqueLongMsgId(new UniqueLongMsgId(entity,ctx.channel(),lmsg,0,true));
		}

		ctx.fireChannelRead(msg);
	}

	@Override
	public String name() {
		return "_LongMessageMarkerReadHandler";
	}

}