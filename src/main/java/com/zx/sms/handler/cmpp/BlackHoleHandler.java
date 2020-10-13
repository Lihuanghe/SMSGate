package com.zx.sms.handler.cmpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * 
 *
 */
@Sharable
public class BlackHoleHandler extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(BlackHoleHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	ReferenceCountUtil.safeRelease(msg);
    }
    
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {	
		ctx.close();
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
    	
    	logger.warn("BlackHoleHandler exceptionCaught on channel {}",ctx.channel(),cause);
    }
}
