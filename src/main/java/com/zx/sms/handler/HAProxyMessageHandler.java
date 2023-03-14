package com.zx.sms.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.haproxy.HAProxyMessage;

/**
 * 处理 proxy protocol 代理协议
 * <br /> 
 * http://www.haproxy.org/download/1.8/doc/proxy-protocol.txt
 */
public class HAProxyMessageHandler extends SimpleChannelInboundHandler<HAProxyMessage>{
	private static final Logger logger = LoggerFactory.getLogger(HAProxyMessageHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HAProxyMessage msg) throws Exception {
		if(logger.isInfoEnabled()) {
			logger.info("receive proxy protocol msg  : {}",msg.toString());
		}
		
		ctx.channel().attr(GlobalConstance.proxyProtocolKey).set(msg);
		
		ctx.pipeline().remove(this);
		//重要：这个消息在这里消费掉后，不能向后边handler传递
//		ctx.fireChannelRead(msg);
	}

}
