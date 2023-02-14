package com.zx.sms.codec.cmpp7F;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;

import com.zx.sms.codec.cmpp7F.packet.Cmpp7FPacketType;
@Sharable
public class CMPP7FMessageCodecAggregator extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(CMPP7FMessageCodecAggregator.class);
	
	private static class CMPP7FMessageCodecAggregatorHolder{
		private final static  CMPP7FMessageCodecAggregator instance = new CMPP7FMessageCodecAggregator();
	}
	
	private ConcurrentHashMap<Integer, MessageToMessageCodec> codecMap = new ConcurrentHashMap<Integer, MessageToMessageCodec>();

	private CMPP7FMessageCodecAggregator() {
		for (PacketType packetType : Cmpp7FPacketType.values()) {
			codecMap.put(packetType.getCommandId(), packetType.getCodec());
		}
	}
	
	public static CMPP7FMessageCodecAggregator getInstance(){
		return CMPP7FMessageCodecAggregatorHolder.instance;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		int commandId =((Message) msg).getHeader().getCommandId();
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
