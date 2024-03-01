package com.zx.sms.connect.manager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.NotSupportedException;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.SocketUtils;

public abstract class AbstractClientEndpointConnector extends AbstractEndpointConnector {

	private static final Logger logger = LoggerFactory.getLogger(AbstractClientEndpointConnector.class);
	private Bootstrap bootstrap = new Bootstrap();
	private SslContext sslCtx = null;
	
	public AbstractClientEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
		if(endpoint.isUseSSL())  
			this.sslCtx = createSslCtx();
		bootstrap.group(EventLoopGroupFactory.INS.getWorker())
		.channel(EventLoopGroupFactory.selectChannelClass())
		.option(ChannelOption.TCP_NODELAY, true)
		//使用操作系统默认缓冲区大小
//		.option(ChannelOption.SO_RCVBUF, 16384)
//		.option(ChannelOption.SO_SNDBUF, 8192)
		.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)   
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,endpoint.getConnectionTimeOut())
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
						logger.info("connect {} faild .retry connect to next host {}:{}",hosts[idx],hosts[idx+1],port);
						doConnect(hosts,idx+1, port,localaddress);
					}else{
						logger.error("Connect to {}:{} failed. cause by {}.",getEndpointEntity().getHost(),port,f.cause().getMessage());
					}
				}
		}});
		
		return future;
	}
	
	protected ChannelInitializer<?> initPipeLine() {

		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				EndpointEntity entity = getEndpointEntity();
				if ( StringUtils.isNotBlank(entity.getProxy())) {
					String uriString = entity.getProxy();
					try {
						URI uri = URI.create(uriString);
						addProxyHandler(ch, uri);
					} catch (Exception ex) {
						logger.error("parse Proxy URI {} failed.", uriString, ex);
					}
				}

				if (entity.isUseSSL() && sslCtx != null) {
					logger.info("EndpointEntity {} Use SSL.",entity);
					pipeline.addLast(sslCtx.newHandler(ch.alloc(), entity.getHost(), entity.getPort()));
				}
				doinitPipeLine(pipeline);
			}
		};
	};
	
	protected abstract void doinitPipeLine(ChannelPipeline pipeline) ;
	
	protected void addProxyHandler(Channel ch, URI proxy) throws NotSupportedException {
		if (proxy == null)
			return;
		String scheme = proxy.getScheme();
		String userinfo = proxy.getUserInfo();
		String host = proxy.getHost();
		int port = proxy.getPort();
		String username = null;
		String pass = null;

		if (StringUtils.isNotBlank(userinfo)) {
			int idx = userinfo.indexOf(":");
			if (idx > 0) {
				username = userinfo.substring(0, idx);
				pass = userinfo.substring(idx + 1);
			}
		}

		ChannelPipeline pipeline = ch.pipeline();

		if ("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme) ) {
		
			if("HTTPS".equalsIgnoreCase(scheme)) {
				if(port < 0) port = 443;  // https default port
				SslContext proxySSLCtx = createSslCtx();
				pipeline.addLast(proxySSLCtx.newHandler(ch.alloc(), host, port));
			}
			if(port < 0) port = 80;  // http default port
			if (username == null) {
				pipeline.addLast(new HttpProxyHandler(new InetSocketAddress(host, port)));
			} else {
				pipeline.addLast(new HttpProxyHandler(new InetSocketAddress(host, port), username, pass));
			}
		} else if ("SOCKS5".equalsIgnoreCase(scheme)) {
			if(port < 0) port = 1080;  // socks default port
			if (username == null) {
				pipeline.addLast(new Socks5ProxyHandler(new InetSocketAddress(host, port)));
			} else {
				pipeline.addLast(new Socks5ProxyHandler(new InetSocketAddress(host, port), username, pass));
			}
		} else if ("SOCKS4".equalsIgnoreCase(scheme) || "SOCKS".equalsIgnoreCase(scheme)) {
			if(port < 0) port = 1080;  // socks default port
			if (username == null) {
				pipeline.addLast(new Socks4ProxyHandler(new InetSocketAddress(host, port)));
			} else {
				pipeline.addLast(new Socks4ProxyHandler(new InetSocketAddress(host, port), username));
			}
		} else {
			throw new NotSupportedException("not support proxy protocol " + scheme);
		}
	}
	
	protected SslContext createSslCtx() {
		try{
			return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
}
