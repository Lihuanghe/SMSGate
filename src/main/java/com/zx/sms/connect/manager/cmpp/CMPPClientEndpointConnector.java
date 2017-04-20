package com.zx.sms.connect.manager.cmpp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.GenericFutureListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;

/**
 *@author Lihuanghe(18852780@qq.com)
 */

public class CMPPClientEndpointConnector extends AbstractEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(CMPPClientEndpointConnector.class);
	private Bootstrap bootstrap = new Bootstrap();
	

	
	public CMPPClientEndpointConnector(CMPPClientEndpointEntity e)
	{
		super(e);
		bootstrap.group(EventLoopGroupFactory.INS.getWorker()).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.SO_RCVBUF, 2048).option(ChannelOption.SO_SNDBUF, 2048)
		.handler(initPipeLine());
	}
	
	@Override
	public ChannelFuture open() throws Exception {
		String host = getEndpointEntity().getHost();
		if(StringUtils.isBlank(host)){
			logger.error("host is blank");
			return null;
		}
		return doConnect(host.split(","),0,getEndpointEntity().getPort());
	}
	
	private ChannelFuture doConnect(final String[] hosts,final int idx ,final int port){
		if(idx>=hosts.length){
			logger.error("hosts.length is {} ,but idx is {}.",hosts.length,idx);
			return null;
		}
		ChannelFuture future = bootstrap.connect(hosts[idx],port);
		
		future.addListener(new GenericFutureListener<ChannelFuture>(){

			@Override
			public void operationComplete(ChannelFuture f) throws Exception {
				if(!f.isSuccess()){
					if(idx+1 < hosts.length){
						logger.info("retry next host {}",hosts[idx+1]);
						doConnect(hosts,idx+1, port);
					}else{
						logger.error("Connect to {} failed.",getEndpointEntity().getHost());
					}
				}
		}});
		return future;
	}

	
	@Override
	protected SslContext createSslCtx() {
	
		try{
			return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
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
