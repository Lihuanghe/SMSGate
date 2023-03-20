package com.zx.sms.connect.manager;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.handler.HAProxyMessageHandler;
import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
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
	private SslContext sslCtx = null;
	
	public AbstractServerEndpointConnector(EndpointEntity e) {
		super(e);
		this.sslCtx = createSslCtx();
		bootstrap.group(EventLoopGroupFactory.INS.getBoss(), EventLoopGroupFactory.INS.getWorker())
				.channel(EventLoopGroupFactory.selectServerChannelClass())
				.option(ChannelOption.SO_BACKLOG, 100)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.handler(new LoggingHandler(LogLevel.DEBUG))
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childOption(ChannelOption.SO_RCVBUF, 16384)
				.childOption(ChannelOption.SO_SNDBUF, 8192)
//				.childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024))
				.childOption(ChannelOption.TCP_NODELAY, true)
				.childHandler(initPipeLine());
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
	
	protected ChannelInitializer<?> initPipeLine() {

		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				EndpointEntity entity = getEndpointEntity();

				if (entity.isProxyProtocol()) {
					logger.info ("add HAProxyMessageHandler .");
					pipeline.addLast(new HAProxyMessageDecoder());
					pipeline.addLast(new HAProxyMessageHandler());
				}

				if (entity.isUseSSL() && sslCtx != null) {
					logger.info("EndpointEntity {} Use SSL.", entity);
					pipeline.addLast(sslCtx.newHandler(ch.alloc()));
				}

				pipeline.addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelActive(ChannelHandlerContext ctx) {
						allChannels.add(ctx.channel());
						ctx.fireChannelActive();
					}
				});
				
				doinitPipeLine(pipeline);
			}
		};
	};

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
	protected abstract void doinitPipeLine(ChannelPipeline pipeline) ;
	
	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {

	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		// TODO Auto-generated method stub
		return null;
	}

}
