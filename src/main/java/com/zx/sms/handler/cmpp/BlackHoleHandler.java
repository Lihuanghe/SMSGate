package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
@Sharable
public class BlackHoleHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	ReferenceCountUtil.safeRelease(msg);
    }
}
