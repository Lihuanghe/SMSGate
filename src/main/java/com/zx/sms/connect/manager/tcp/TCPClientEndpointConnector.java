package com.zx.sms.connect.manager.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;

public class TCPClientEndpointConnector extends AbstractEndpointConnector  {
	private static final Logger logger = LoggerFactory.getLogger(TCPClientEndpointConnector.class);
	private Bootstrap bootstrap = new Bootstrap();
	

	public TCPClientEndpointConnector(TCPClientEndpointEntity e) {
		super(e);
		bootstrap.group(EventLoopGroupFactory.INS.getWorker()).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.SO_RCVBUF, 2048).option(ChannelOption.SO_SNDBUF, 2048)
		.handler(initPipeLine());
	}

	@Override
	public ChannelFuture open() throws Exception {

		ChannelFuture future = bootstrap.connect(getEndpointEntity().getHost(), getEndpointEntity().getPort());

		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					addChannel(f.channel());
				}
			}
		});
		
		try {
			future.sync();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		return future;
	}



	public ChannelInitializer<SocketChannel> initPipeLine() {

		return new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				 pipeline.addLast("clientLog", new LoggingHandler(LogLevel.DEBUG));
				 pipeline.addLast("Echo",  new TCPServerEchoHandler());
			}
		};
	}

	@Override
	protected SslContext createSslCtx() {
		try{
			return SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
		}catch(Exception ex){
			return null;
		}
	}
	
	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		ChannelPipeline pipeline = ch.pipeline();
		if(entity.isUseSSL()){
			if(entity instanceof ServerEndpoint){
				pipeline.addLast(getSslCtx().newHandler(ch.alloc()));
			}
		}
	}
}
