package com.zx.sms.handler.sgip;

import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.connect.manager.sgip.SgipEndpointEntity;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ReWriteNodeIdHandler extends ChannelDuplexHandler {
	private SgipEndpointEntity entity;
	public ReWriteNodeIdHandler(SgipEndpointEntity entity) {
		this.entity = entity;
	}
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if(msg instanceof DefaultMessage && entity instanceof SgipEndpointEntity) {
			if(((DefaultMessage)msg).isRequest() && entity.getNodeId()!=0) {
				((DefaultMessage) msg).getHeader().setNodeId(entity.getNodeId());
			}
		}
		ctx.write(msg, promise);
	}
}
