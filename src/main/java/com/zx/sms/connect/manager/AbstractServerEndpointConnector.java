package com.zx.sms.connect.manager;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author Lihuanghe(18852780@qq.com)
 */
public abstract class AbstractServerEndpointConnector extends AbstractEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(AbstractServerEndpointConnector.class);
	private ServerBootstrap bootstrap = new ServerBootstrap();
	private Channel acceptorChannel = null;
	private final DefaultChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	public AbstractServerEndpointConnector(EndpointEntity e) {
		super(e);
		bootstrap.group(EventLoopGroupFactory.INS.getBoss(), EventLoopGroupFactory.INS.getWorker()).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 100).childOption(ChannelOption.SO_RCVBUF, 2048).childOption(ChannelOption.SO_SNDBUF, 2048)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024)).childOption(ChannelOption.TCP_NODELAY, true)
				.handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(initPipeLine());
	}

	@Override
	public ChannelFuture open() throws Exception {
		logger.debug("Open Entity {}", getEndpointEntity());
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
		if (acceptorChannel != null)
			acceptorChannel.close();
		acceptorChannel = null;
		allChannels.close();
	}

	@Override
	protected SslContext createSslCtx() {
		try {
			if (getEndpointEntity().isUseSSL()) {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
			} else {
				return null;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		ChannelPipeline pipeline = ch.pipeline();
		if (entity instanceof ServerEndpoint) {
			logger.info("EndpointEntity {} Use SSL.", entity);
			pipeline.addLast(getSslCtx().newHandler(ch.alloc()));
		}
	}

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {

	}

	protected void doinitPipeLine(ChannelPipeline pipeline) {
		pipeline.addLast(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelActive(ChannelHandlerContext ctx) {
				allChannels.add(ctx.channel());
				ctx.fireChannelActive();
			}
		});
	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		// TODO Auto-generated method stub
		return null;
	}

}
