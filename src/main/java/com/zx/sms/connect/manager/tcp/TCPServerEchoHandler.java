package com.zx.sms.connect.manager.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointConnector;

@Sharable
public class TCPServerEchoHandler extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TCPServerEchoHandler.class);

	private EndpointConnector conn;

	private volatile long totalread = 0;
	private volatile long release = 0;
	private volatile long trans = 0;

	public TCPServerEchoHandler(EndpointConnector conn) {
		this.conn = conn;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		int cnt = ((ByteBuf) msg).readableBytes();
		totalread += cnt;
		Channel ch = conn.fetch();
		if (!ch.id().equals(ctx.channel().id())) {
			trans += cnt;
			ch.writeAndFlush(msg);
		} else {
			release += cnt;
			ReferenceCountUtil.release(msg);
		}

		//logger.info("channelID :{} ,read : {}, totalread:{},trans  : {} release {}",ctx.channel(), cnt, totalread, trans, release);
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
