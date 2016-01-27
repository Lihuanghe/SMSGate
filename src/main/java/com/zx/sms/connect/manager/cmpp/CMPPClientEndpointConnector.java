package com.zx.sms.connect.manager.cmpp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.session.cmpp.SessionLoginManager;

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
	public void open() throws Exception {
		bootstrap.connect(getEndpointEntity().getHost(), getEndpointEntity().getPort());
	}
	@Override
	protected SslContext createSslCtx() {
	
		try{
			return SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
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
