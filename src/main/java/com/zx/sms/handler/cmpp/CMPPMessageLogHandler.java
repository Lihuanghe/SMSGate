package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.connect.manager.EndpointEntity;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */

public class CMPPMessageLogHandler extends ChannelHandlerAdapter {
	private final Logger logger;
	private EndpointEntity entity;

	public CMPPMessageLogHandler(EndpointEntity entity) {
		this.entity = entity;
		logger = LoggerFactory.getLogger(entity.getId());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof Message) {
			logger.debug("Receive:{}", msg);
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof Message) {
			logger.debug("Send:{}", msg);
		}
		ctx.write(msg, promise);
	}

}
