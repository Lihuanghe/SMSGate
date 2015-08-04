package com.zx.sms.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;

public abstract class AbstractTestMessageCodec<T> {
	
	private static int version = 0x30;
	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			CMPPCodecChannelInitializer codec = new CMPPCodecChannelInitializer(getVersion());
			pipeline.addLast("serverLog", new LoggingHandler(LogLevel.INFO));
			pipeline.addLast(codec.pipeName(), codec);

		}

	});
	
	protected int getVersion(){
		return this.version;
	}

	protected ByteBuf encode(T msg){
		ch.writeOutbound(msg);
		ByteBuf buf = ch.readOutbound();
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
