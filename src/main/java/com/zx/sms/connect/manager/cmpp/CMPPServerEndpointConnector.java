package com.zx.sms.connect.manager.cmpp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ServerEndpoint;
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
				.childOption(ChannelOption.TCP_NODELAY, true).handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(initPipeLine());
	}

	@Override
	public ChannelFuture open() throws Exception {
		logger.debug("Open Entity {}" ,getEndpointEntity() );
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
		acceptorChannel.close();
		acceptorChannel = null;
	}

	@Override
	protected SslContext createSslCtx() {
		try{
			 SelfSignedCertificate ssc = new SelfSignedCertificate();
			return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		ChannelPipeline pipeline = ch.pipeline();
		if(entity instanceof ServerEndpoint){
			logger.info("EndpointEntity {} Use SSL.",entity);
			pipeline.addLast(getSslCtx().newHandler(ch.alloc()));
		}
	}

}
