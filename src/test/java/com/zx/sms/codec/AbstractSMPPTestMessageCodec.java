package com.zx.sms.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import com.zx.sms.connect.manager.smpp.SMPPCodecChannelInitializer;

public  abstract class AbstractSMPPTestMessageCodec<T> {
	
	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {
		@Override
		protected void initChannel(Channel ch) throws Exception {
			doinitChannel(ch);
		}
	});
	
	protected void doinitChannel(Channel ch){
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		ChannelPipeline pipeline = ch.pipeline();
		SMPPCodecChannelInitializer codec = new SMPPCodecChannelInitializer();
		pipeline.addLast("serverLog", new LoggingHandler(LogLevel.DEBUG));
		pipeline.addLast(codec.pipeName(), codec);
	}

	protected ByteBuf encode(T msg){
		ch.writeOutbound(msg);
		ByteBuf buf = (ByteBuf)ch.readOutbound();
		return buf;
	}
	
	protected EmbeddedChannel channel(){
		return ch;
	}
	protected T decode(ByteBuf buf){
		ch.writeInbound(buf);
		return  (T) ch.readInbound();
	}

}
