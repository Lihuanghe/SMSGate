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
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
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
		ChannelFuture future = bootstrap.connect(getEndpointEntity().getHost(), getEndpointEntity().getPort());
		
		try {
			future.sync();
		} catch (InterruptedException e) {
			logger.error("open Entity {} error. ",getEndpointEntity());
		}
	}
	@Override
	public ChannelInitializer<?> initPipeLine() {
		return new ChannelInitializer<Channel>() {
			
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				
				pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, ((CMPPEndpointEntity)getEndpointEntity()).getIdleTimeSec(), TimeUnit.SECONDS));
				pipeline.addLast("CmppServerIdleStateHandler", GlobalConstance.idleHandler);
				pipeline.addLast("clientLog", new LoggingHandler(LogLevel.TRACE));
				CMPPCodecChannelInitializer codec = new CMPPCodecChannelInitializer(((CMPPEndpointEntity)getEndpointEntity()).getVersion());
				pipeline.addLast(codec.pipeName(), codec);
				pipeline.addLast("sessionLoginManager", new SessionLoginManager((CMPPEndpointEntity)getEndpointEntity()));
			}
		};
	}




}
