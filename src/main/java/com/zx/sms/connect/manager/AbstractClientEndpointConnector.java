package com.zx.sms.connect.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.SocketUtils;

import java.net.SocketAddress;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientEndpointConnector extends AbstractEndpointConnector {

	private static final Logger logger = LoggerFactory.getLogger(AbstractClientEndpointConnector.class);
	private Bootstrap bootstrap = new Bootstrap();
	
	public AbstractClientEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
		bootstrap.group(EventLoopGroupFactory.INS.getWorker())
		.channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.SO_RCVBUF, 16384)
		.option(ChannelOption.SO_SNDBUF, 8192)
		.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)   
//		.option(ChannelOption.RCVBUF_ALLOCATOR,new FixedRecvByteBufAllocator(1024))
		.handler(initPipeLine());
	}

	@Override
	public ChannelFuture open() throws Exception {
		String host = getEndpointEntity().getHost();
		String localhost = getEndpointEntity().getLocalhost();
		Integer localport = getEndpointEntity().getLocalport();
		SocketAddress localaddr = null;
		
		if(StringUtils.isNotBlank(localhost) && localport!=null){
			localaddr = SocketUtils.socketAddress(localhost, localport);
		}
		
		if(StringUtils.isBlank(host)){
			logger.error("remote host is blank");
			return null;
		}
		
		return doConnect(host.split(","),0,getEndpointEntity().getPort(),localaddr);
	}
	
	private ChannelFuture doConnect(final String[] hosts,final int idx ,final int port ,final SocketAddress localaddress){
		if(idx>=hosts.length){
			logger.error("hosts.length is {} ,but idx is {}.",hosts.length,idx);
			return null;
		}
	
		ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(hosts[idx],port),localaddress);
		
		future.addListener(new GenericFutureListener<ChannelFuture>(){

			@Override
			public void operationComplete(ChannelFuture f) throws Exception {
				if(!f.isSuccess()){
					if(idx+1 < hosts.length){
						logger.info("retry connect to next host {}:{}",hosts[idx+1],port);
						doConnect(hosts,idx+1, port,localaddress);
					}else{
						logger.error("Connect to {}:{} failed. cause by {}.",getEndpointEntity().getHost(),port,f.cause().getMessage());
					}
				}
		}});
		
		return future;
	}
	
	@Override
	protected SslContext createSslCtx() {
	
		try{
			if(getEndpointEntity().isUseSSL())
				return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			else 
				return null;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
	}
	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		ChannelPipeline pipeline = ch.pipeline();
		if(entity instanceof ClientEndpoint){
			logger.info("EndpointEntity {} Use SSL.",entity);
			pipeline.addLast(getSslCtx().newHandler(ch.alloc(), entity.getHost(), entity.getPort()));
		}
	}
}
