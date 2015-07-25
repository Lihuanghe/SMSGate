package com.zx.sms.connect.manager.cmpp;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;
import com.zx.sms.session.cmpp.SessionLoginManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public class CMPPServerEndpointConnector extends AbstractEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(CMPPServerEndpointConnector.class);
	private ServerBootstrap bootstrap = new ServerBootstrap();
	private Channel acceptorChannel = null;

	public CMPPServerEndpointConnector(CMPPServerEndpointEntity e) {
		super(e);
		bootstrap.group(EventLoopGroupFactory.INS.getBoss(), EventLoopGroupFactory.INS.getWorker()).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 100).option(ChannelOption.SO_RCVBUF, 2048).option(ChannelOption.SO_SNDBUF, 2048)
				.childOption(ChannelOption.TCP_NODELAY, true).handler(new LoggingHandler(LogLevel.INFO)).childHandler(initPipeLine());
	}

	@Override
	public void open() throws Exception {
		logger.debug("Open Entity {}" ,getEndpointEntity() );
		ChannelFuture future = null;

		if (getEndpointEntity().getHost() == null)
			future = bootstrap.bind(getEndpointEntity().getPort()).sync();
		else
			future = bootstrap.bind(getEndpointEntity().getHost(), getEndpointEntity().getPort()).sync();

		acceptorChannel = future.channel();

	}

	@Override
	public void close() throws Exception {

		super.close();
		acceptorChannel.close().sync();
		acceptorChannel = null;
	}

	@Override
	public ChannelInitializer<Channel> initPipeLine() {

		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				CMPPCodecChannelInitializer codec = new CMPPCodecChannelInitializer(0);
				pipeline.addLast("serverLog", new LoggingHandler(LogLevel.TRACE));
				pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0,30, TimeUnit.SECONDS));
				pipeline.addLast("CmppServerIdleStateHandler", GlobalConstance.idleHandler);
				pipeline.addLast(codec.pipeName(), codec);
				pipeline.addLast("sessionLoginManager", new SessionLoginManager(getEndpointEntity()));
			}
		};
	}

}
