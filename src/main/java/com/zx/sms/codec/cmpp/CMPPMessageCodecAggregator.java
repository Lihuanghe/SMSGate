package com.zx.sms.codec.cmpp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

@Sharable
public class CMPPMessageCodecAggregator extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(CMPPMessageCodecAggregator.class);

	private static class CMPPMessageCodecAggregatorHolder {
		private final static CMPPMessageCodecAggregator instance = new CMPPMessageCodecAggregator();
	}

	private ConcurrentHashMap<Integer, MessageToMessageCodec> codecMap = new ConcurrentHashMap<Integer, MessageToMessageCodec>();

	private CMPPMessageCodecAggregator() {
		for (PacketType packetType : CmppPacketType.values()) {
			codecMap.put(packetType.getCommandId(), packetType.getCodec());
		}
	}

	public static CMPPMessageCodecAggregator getInstance() {
		return CMPPMessageCodecAggregatorHolder.instance;
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
			int commandId = ((Message) msg).getHeader().getCommandId();
			MessageToMessageCodec codec = codecMap.get(commandId);
			codec.write(ctx, msg, promise);
		} catch (Exception ex) {
			promise.tryFailure(ex);
		}
	}
}
