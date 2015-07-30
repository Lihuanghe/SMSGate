package com.zx.sms.handler.cmpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
@Sharable
public class BlackHoleHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       

    }
}
