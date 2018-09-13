package com.zx.sms.codec;

import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.handler.cmpp.CMPPDeliverLongMessageHandler;
import com.zx.sms.handler.cmpp.CMPPSubmitLongMessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public abstract class AbstractTestMessageCodec<T> {
	
	private static int version = 0x20;
	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ResourceLeakDetector.setLevel(Level.ADVANCED);
			ChannelPipeline pipeline = ch.pipeline();
			CMPPCodecChannelInitializer codec = new CMPPCodecChannelInitializer(getVersion());
			pipeline.addLast("serverLog", new LoggingHandler(LogLevel.DEBUG));
			pipeline.addLast(codec.pipeName(), codec);
			pipeline.addLast( "CMPPDeliverLongMessageHandler", new CMPPDeliverLongMessageHandler(null));
			pipeline.addLast("CMPPSubmitLongMessageHandler",  new CMPPSubmitLongMessageHandler(null));
		}
	});
	
	protected int getVersion(){
		return this.version;
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
