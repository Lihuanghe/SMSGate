package com.zx.sms.connect.manager.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;

public class TCPServerEndpointConnector extends AbstractEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(TCPServerEndpointConnector.class);
	private ServerBootstrap bootstrap = new ServerBootstrap();
	
	private Channel acceptorChannel = null;
	
	public TCPServerEndpointConnector(EndpointEntity e) {
		super(e);
		bootstrap.group(EventLoopGroupFactory.INS.getBoss(), EventLoopGroupFactory.INS.getWorker())
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 100)
				.option(ChannelOption.SO_RCVBUF, 2048)
				.option(ChannelOption.SO_SNDBUF, 2048)
				.childOption(ChannelOption.TCP_NODELAY, true)
//				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(initPipeLine());
	}

	@Override
	public ChannelFuture open() throws Exception {
		
		ChannelFuture future = null;

		if (getEndpointEntity().getHost() == null)
			future = bootstrap.bind(getEndpointEntity().getPort()).sync();
		else
			future = bootstrap.bind(getEndpointEntity().getHost(), getEndpointEntity().getPort()).sync();

		acceptorChannel = future.channel();
		return future;
	}



	@Override
	public void close() throws Exception {

		super.close();
		acceptorChannel.close().sync();
		acceptorChannel = null;
	}


	private class ChannelCollector extends ChannelHandlerAdapter {
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			// 把连接加入数组
			addChannel(ctx.channel());
			super.channelActive(ctx);
		}

		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			removeChannel(ctx.channel());
			ctx.fireChannelInactive();
		}
	}

	public ChannelInitializer<SocketChannel> initPipeLine() {
		final TCPServerEchoHandler h = new TCPServerEchoHandler();
		return new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
//				pipeline.addLast("serverLog", new LoggingHandler(LogLevel.INFO));
				pipeline.addLast("channelcollector", new ChannelCollector());
				pipeline.addLast("Echo", h);
			}

		};
	}


	private TCPServerEndpointConnector getConnector() {
		return this;
	}

	@Override
	protected SslContext createSslCtx() {
		try{
			 SelfSignedCertificate ssc = new SelfSignedCertificate();
			 return SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
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
