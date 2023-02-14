package com.zx.sms.connect.manager.sgip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.sgip12.codec.SgipHeaderCodec;
import com.zx.sms.codec.sgip12.codec.SgipMessageCodecAggregator;
import com.zx.sms.common.GlobalConstance;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SgipCodecChannelInitializer extends ChannelInitializer<Channel> {
	private static final Logger logger = LoggerFactory.getLogger(SgipCodecChannelInitializer.class);
	
	public static String pipeName() {
		return "smppCodec";
	}
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addBefore(pipeName(), "FrameDecoder", new LengthFieldBasedFrameDecoder(4 * 1024 , 0, 4, -4, 0, true));
		pipeline.addBefore(pipeName(), "SgipHeaderCodec", new SgipHeaderCodec());
		pipeline.addBefore(pipeName(), GlobalConstance.codecName, SgipMessageCodecAggregator.getInstance());

	}
}
