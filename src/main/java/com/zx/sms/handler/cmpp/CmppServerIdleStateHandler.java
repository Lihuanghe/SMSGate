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
public class CmppServerIdleStateHandler extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(CmppServerIdleStateHandler.class);
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                ctx.channel().writeAndFlush(new CmppActiveTestRequestMessage());
            } 
        }else{
        	ctx.fireUserEventTriggered(evt);
        }
    }
}
