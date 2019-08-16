package com.zx.sms.handler.sgip;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

import com.zx.sms.codec.sgip12.msg.SgipReportRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipReportResponseMessage;
import com.zx.sms.handler.api.AbstractBusinessHandler;
@Sharable
public class SgipReportRequestMessageHandler extends AbstractBusinessHandler{
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	if(msg instanceof SgipReportRequestMessage){
    		SgipReportResponseMessage resp = new SgipReportResponseMessage(((SgipReportRequestMessage)msg).getHeader());
    		resp.setResult((short)0);
    		resp.setTimestamp(((SgipReportRequestMessage)msg).getTimestamp());
    		ctx.channel().writeAndFlush(resp);
    	}else{
    		ctx.fireChannelRead(msg);
    	}
    	
    }

	@Override
	public String name() {
		return "SgipReportRequestMessageHandler";
	}
}
