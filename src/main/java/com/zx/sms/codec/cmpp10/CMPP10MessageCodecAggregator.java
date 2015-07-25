package com.zx.sms.codec.cmpp10;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp10.packet.Cmpp10PacketType;

public class CMPP10MessageCodecAggregator extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(CMPP10MessageCodecAggregator.class);
	
	private static class CMPP10MessageCodecAggregatorHolder{
		private final static  CMPP10MessageCodecAggregator instance = new CMPP10MessageCodecAggregator();
	}
	
	private ConcurrentHashMap<Long, MessageToMessageCodec> codecMap = new ConcurrentHashMap<Long, MessageToMessageCodec>();

	private CMPP10MessageCodecAggregator() {
		for (PacketType packetType : Cmpp10PacketType.values()) {
			codecMap.put(packetType.getCommandId(), packetType.getCodec());
		}
	}
	
	public static CMPP10MessageCodecAggregator getInstance(){
		return CMPP10MessageCodecAggregatorHolder.instance;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		long commandId = (Long) ((Message) msg).getHeader().getCommandId();
		MessageToMessageCodec codec = codecMap.get(commandId);
		codec.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		long commandId = (Long) ((Message) msg).getHeader().getCommandId();
		MessageToMessageCodec codec = codecMap.get(commandId);
		codec.write(ctx, msg, promise);
	}
}
