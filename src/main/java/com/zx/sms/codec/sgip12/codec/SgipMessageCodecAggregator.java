package com.zx.sms.codec.sgip12.codec;

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
import com.zx.sms.codec.sgip12.packet.SgipPacketType;

@Sharable
public class SgipMessageCodecAggregator extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(SgipMessageCodecAggregator.class);
	private static class SgipMessageCodecAggregatorHolder{
		private final static  SgipMessageCodecAggregator instance = new SgipMessageCodecAggregator();
	}
	
	private ConcurrentHashMap<Integer, MessageToMessageCodec> codecMap = new ConcurrentHashMap<Integer, MessageToMessageCodec>();

	private SgipMessageCodecAggregator() {
		for (PacketType packetType : SgipPacketType.values()) {
			codecMap.put(packetType.getCommandId(), packetType.getCodec());
		}
	}
	
	public static SgipMessageCodecAggregator getInstance(){
		return SgipMessageCodecAggregatorHolder.instance;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		int commandId =  ((Message) msg).getHeader().getCommandId();
		MessageToMessageCodec codec = codecMap.get(commandId);
		codec.channelRead(ctx, msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		try {
			int commandId =  ((Message) msg).getHeader().getCommandId();
			MessageToMessageCodec codec = codecMap.get(commandId);
			codec.write(ctx, msg, promise);
		}catch(Exception ex) {
			promise.tryFailure(ex);
		}
	}
}
