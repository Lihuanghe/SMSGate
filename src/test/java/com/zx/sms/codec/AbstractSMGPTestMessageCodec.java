package com.zx.sms.codec;

import com.zx.sms.codec.cmpp.wap.LongMessageMarkerReadHandler;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.smgp.SMGPCodecChannelInitializer;
import com.zx.sms.handler.smgp.SMGPDeliverLongMessageHandler;
import com.zx.sms.handler.smgp.SMGPSubmitLongMessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public  abstract class AbstractSMGPTestMessageCodec<T> {
	
	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {
		@Override
		protected void initChannel(Channel ch) throws Exception {
			doinitChannel(ch);
		}
	});
	
	protected int getversion() {
		return 0x13;
	}
	
	protected void doinitChannel(Channel ch){
		ResourceLeakDetector.setLevel(Level.DISABLED);
		ChannelPipeline pipeline = ch.pipeline();
		SMGPCodecChannelInitializer codec = new SMGPCodecChannelInitializer(getversion());
		pipeline.addLast("serverLog", new LoggingHandler(this.getClass(),LogLevel.DEBUG));
		pipeline.addLast(codec.pipeName(), codec);
		LongMessageMarkerReadHandler h_readMarker = new LongMessageMarkerReadHandler(null);
		pipeline.addAfter(GlobalConstance.codecName, h_readMarker.name(),h_readMarker );

		//处理长短信
		pipeline.addLast("SMGPDeliverLongMessageHandler", new SMGPDeliverLongMessageHandler(null));
		pipeline.addLast("SMGPSubmitLongMessageHandler",  new SMGPSubmitLongMessageHandler(null));
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
