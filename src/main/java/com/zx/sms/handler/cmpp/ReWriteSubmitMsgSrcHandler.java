package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

/**
 *重新设置Msg_Src字段，有些网关系统会校验该字段 ，防止业务则填写错误
 */
public class ReWriteSubmitMsgSrcHandler extends ChannelDuplexHandler {
	private CMPPEndpointEntity entity;

	public ReWriteSubmitMsgSrcHandler(CMPPEndpointEntity entity) {
		this.entity = entity;
	}
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		
		if(msg instanceof CmppSubmitRequestMessage){
			CmppSubmitRequestMessage submitMsg = (CmppSubmitRequestMessage)msg;
			submitMsg.setMsgsrc(entity.getUserName());
		}
		ctx.write(msg, promise);
	}
}
