package com.zx.sms.connect.manager.smpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.SMPPMessageCodec;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SMPPCodecChannelInitializer extends ChannelInitializer<Channel> {
	private static final Logger logger = LoggerFactory.getLogger(SMPPCodecChannelInitializer.class);
	private EndpointEntity entity;
	public static String pipeName() {
		return "smppCodec";
	}
	
	public SMPPCodecChannelInitializer(EndpointEntity entity) {
		this.entity = entity;
	}
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addBefore(pipeName(), "FrameDecoder", new LengthFieldBasedFrameDecoder(4 * 1024 , 0, 4, -4, 0, true));

		pipeline.addBefore(pipeName(), GlobalConstance.codecName, new SMPPMessageCodec(entity));
	}
}
