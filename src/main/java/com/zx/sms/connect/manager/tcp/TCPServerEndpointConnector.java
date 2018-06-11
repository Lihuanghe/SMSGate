package com.zx.sms.connect.manager.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.session.AbstractSessionStateManager;

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


	private class ChannelCollector extends ChannelDuplexHandler {
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

	private TCPServerEndpointConnector getConnector() {
		return this;
	}

	@Override
	protected SslContext createSslCtx() {
		try{
			if(getEndpointEntity().isUseSSL()){
				 SelfSignedCertificate ssc = new SelfSignedCertificate();
					return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			}else{
				return null;
			}
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

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		pipeline.addLast("channelcollector", new ChannelCollector());
		pipeline.addLast("Echo", new TCPServerEchoHandler());
	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		// TODO Auto-generated method stub
		return null;
	}
}
