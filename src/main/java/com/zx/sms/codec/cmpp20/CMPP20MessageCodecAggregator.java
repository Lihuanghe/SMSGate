package com.zx.sms.codec.cmpp20;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;

@Sharable
public class CMPP20MessageCodecAggregator extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(CMPP20MessageCodecAggregator.class);

	private static class CMPP20MessageCodecAggregatorHolder {
		private final static CMPP20MessageCodecAggregator instance = new CMPP20MessageCodecAggregator();
	}

	private ConcurrentHashMap<Integer, MessageToMessageCodec> codecMap = new ConcurrentHashMap<Integer, MessageToMessageCodec>();

	private CMPP20MessageCodecAggregator() {
		for (PacketType packetType : Cmpp20PacketType.values()) {
			codecMap.put(packetType.getCommandId(), packetType.getCodec());
		}
	}

	public static CMPP20MessageCodecAggregator getInstance() {
		return CMPP20MessageCodecAggregatorHolder.instance;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		int commandId = ((Message) msg).getHeader().getCommandId();
		MessageToMessageCodec codec = codecMap.get(commandId);
		codec.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		try {
			int commandId =  ((Message) msg).getHeader().getCommandId();
			MessageToMessageCodec codec = codecMap.get(commandId);
			codec.write(ctx, msg, promise);
		} catch (Exception ex) {
			promise.tryFailure(ex);
		}
	}
}
