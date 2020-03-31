package com.zx.sms.codec;


import com.zx.sms.connect.manager.sgip.SgipCodecChannelInitializer;
import com.zx.sms.handler.sgip.SgipDeliverLongMessageHandler;
import com.zx.sms.handler.sgip.SgipSubmitLongMessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public  abstract class AbstractSGIPTestMessageCodec<T> {
	
	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {
		@Override
		protected void initChannel(Channel ch) throws Exception {
			doinitChannel(ch);
		}
	});
	
	protected void doinitChannel(Channel ch){
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		ChannelPipeline pipeline = ch.pipeline();
		SgipCodecChannelInitializer codec = new SgipCodecChannelInitializer();
		pipeline.addLast("serverLog", new LoggingHandler(this.getClass(),LogLevel.DEBUG));
		pipeline.addLast(codec.pipeName(), codec);
		pipeline.addLast( "SgipDeliverLongMessageHandler", new SgipDeliverLongMessageHandler(null));
		pipeline.addLast("SgipSubmitLongMessageHandler",  new SgipSubmitLongMessageHandler(null));
	
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
