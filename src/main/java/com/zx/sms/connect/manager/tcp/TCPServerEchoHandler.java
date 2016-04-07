package com.zx.sms.connect.manager.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServerEchoHandler extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(TCPServerEchoHandler.class);


	private volatile long totalread = 0;
	private volatile long release = 0;
	private volatile long trans = 0;

	public TCPServerEchoHandler() {
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		int cnt = ((ByteBuf) msg).readableBytes();
		totalread += cnt;
		Channel ch = ctx.channel();
		ch.writeAndFlush(msg);

		logger.info("channelID :{} ,read : {}, totalread:{},trans  : {} release {}",ctx.channel(), cnt, totalread, trans, release);
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("ERROR",cause);
    }

	private static Object safeDuplicate(Object message) {
		if (message instanceof ByteBuf) {
			return ((ByteBuf) message).duplicate().retain();
		} else if (message instanceof ByteBufHolder) {
			return ((ByteBufHolder) message).duplicate().retain();
		} else {
			return ReferenceCountUtil.retain(message);
		}
	}

}
