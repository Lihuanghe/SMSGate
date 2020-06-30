package com.zx.sms.connect.manager;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.handler.MessageLogHandler;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.concurrent.Promise;

/**
 * @author Lihuanghe(18852780@qq.com)
 */
public abstract class AbstractEndpointConnector implements EndpointConnector<EndpointEntity> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractEndpointConnector.class);

	private SslContext sslCtx = null;
	/**
	 * 端口
	 */
	private EndpointEntity endpoint;

	private CircularList channels = new CircularList();

	private final static String sessionHandler = "sessionStateManager";

	public AbstractEndpointConnector(EndpointEntity endpoint) {
		this.endpoint = endpoint;
		this.sslCtx = createSslCtx();
	}

	protected abstract SslContext createSslCtx();

	@Override
	public EndpointEntity getEndpointEntity() {

		return endpoint;
	}

	@Override
	public void close(Channel channel) throws Exception {
		if (channel.isOpen())
			channel.close();
		// 将channel移除
		removeChannel(channel);
	}

	@Override
	public void close() throws Exception {
		Channel[] chs = channels.getall();
		if (chs == null || chs.length == 0)
			return;
		for (Channel ch : chs) {
			close(ch);
		}
	}

	@Override
	public Channel fetch() {
		Channel ch = channels.fetch();

		if (ch != null && ch.isActive()) {
			if (ch.attr(GlobalConstance.attributeKey).get() == SessionState.Connect) {
				return ch;
			}
		}
		return null;
	}

	public SslContext getSslCtx() {
		return sslCtx;
	}

	@Override
	public int getConnectionNum() {

		return getChannels().size();
	}

	private CircularList getChannels() {
		return channels;
	}

	protected abstract AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean presend);

	protected abstract void doBindHandler(ChannelPipeline pipe, EndpointEntity entity);

	protected abstract void doinitPipeLine(ChannelPipeline pipeline);

	protected void addProxyHandler(Channel ch, URI proxy) throws NotSupportedException {
		if (proxy == null)
			return;
		String scheme = proxy.getScheme();
		String userinfo = proxy.getUserInfo();
		String host = proxy.getHost();
		int port = proxy.getPort();
		String username = null;
		String pass = null;

		if (userinfo != null && (!"".equals(userinfo))) {
			int idx = userinfo.indexOf(":");
			if (idx > 0) {
				username = userinfo.substring(0, idx);
				pass = userinfo.substring(idx + 1);
			}
		}

		ChannelPipeline pipeline = ch.pipeline();

		if ("HTTP".equalsIgnoreCase(scheme)) {
			if (username == null) {
				pipeline.addLast(new HttpProxyHandler(new InetSocketAddress(host, port)));
			} else {
				pipeline.addLast(new HttpProxyHandler(new InetSocketAddress(host, port), username, pass));
			}
		} else if ("SOCKS5".equalsIgnoreCase(scheme)) {
			if (username == null) {
				pipeline.addLast(new Socks5ProxyHandler(new InetSocketAddress(host, port)));
			} else {
				pipeline.addLast(new Socks5ProxyHandler(new InetSocketAddress(host, port), username, pass));
			}
		} else if ("SOCKS4".equalsIgnoreCase(scheme)) {
			if (username == null) {
				pipeline.addLast(new Socks4ProxyHandler(new InetSocketAddress(host, port)));
			} else {
				pipeline.addLast(new Socks4ProxyHandler(new InetSocketAddress(host, port), username));
			}
		} else {
			throw new NotSupportedException("not support proxy protocol " + scheme);
		}
	}

	protected ChannelInitializer<?> initPipeLine() {

		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				EndpointEntity entity = getEndpointEntity();
				// pipeline.addFirst(new LoggingHandler("proxy",
				// LogLevel.INFO));
				if (entity instanceof ClientEndpoint && StringUtils.isNotBlank(entity.getProxy())) {
					String uriString = entity.getProxy();
					try {
						URI uri = URI.create(uriString);
						addProxyHandler(ch, uri);
					} catch (Exception ex) {
						logger.error("parse Proxy URI {} failed.", uriString, ex);
					}
				}

				if (entity.isUseSSL() && getSslCtx() != null) {
					initSslCtx(ch, entity);
				}
				doinitPipeLine(pipeline);
			}
		};
	};

	public synchronized boolean addChannel(Channel ch) {
		int nowConnCnt = getConnectionNum();
		EndpointEntity endpoint = getEndpointEntity();
		if (endpoint.getMaxChannels() == 0 || endpoint.getMaxChannels() > nowConnCnt) {

			ConcurrentMap<Serializable, VersionObject> storedMap = null;
			if (endpoint.isReSendFailMsg()) {
				// 如果上次发送失败的消息要重发一次，则要创建持久化Map用于存储发送的message
				storedMap = BDBStoredMapFactoryImpl.INS.buildMap(endpoint.getId(), "Session_" + endpoint.getId());
			} else {
				storedMap = new ConcurrentHashMap();
			}

			logger.info("Channel added To Endpoint {} .totalCnt:{} ,remoteAddress: {}", endpoint, nowConnCnt + 1, ch.remoteAddress());

			if (nowConnCnt == 0 && endpoint.isReSendFailMsg()) {
				// 如果是第一个连接。要把上次发送失败的消息取出，再次发送一次
				ch.pipeline().addAfter(GlobalConstance.codecName, sessionHandler, createSessionManager(endpoint, storedMap, true));
			} else {
				ch.pipeline().addAfter(GlobalConstance.codecName, sessionHandler, createSessionManager(endpoint, storedMap, false));
			}

			// 增加流量整形 ，每个连接每秒发送，接收消息数不超过配置的值
			ch.pipeline().addAfter(GlobalConstance.codecName, "ChannelTrafficAfter",
					new MessageChannelTrafficShapingHandler(endpoint.getWriteLimit(), endpoint.getReadLimit(), 250));

			bindHandler(ch.pipeline(), getEndpointEntity());
			
			/* 所有的handler都已加入pipeliner后再标识连接已建立，
			 * 如过早加入connector，遇到有消息发送时，可能业务handler还未加入，
			 * 引起消息未经handler处理就发了出去。
			 */
			ch.attr(GlobalConstance.attributeKey).set(SessionState.Connect);
			getChannels().add(ch);
			return true;
		} else {
			logger.warn("allowed max channel count: {} ,deny to login.{}", endpoint.getMaxChannels(), endpoint);

			return false;
		}

	}

	public void removeChannel(Channel ch) {

		if (getChannels().remove(ch)) {
			ch.attr(GlobalConstance.attributeKey).set(SessionState.DisConnect);
		}
	}

	/**
	 * 连接建立成功后要加载的channelHandler
	 */
	protected void bindHandler(ChannelPipeline pipe, EndpointEntity entity) {

		if (entity instanceof CMPPServerEndpointEntity) {
			return;
		}
		pipe.addFirst("socketLog", new LoggingHandler(String.format(GlobalConstance.loggerNamePrefix, entity.getId()), LogLevel.TRACE));

		// 调用子类的bind方法
		doBindHandler(pipe, entity);

		pipe.addAfter(GlobalConstance.codecName, "msgLog", new MessageLogHandler(entity));

		List<BusinessHandlerInterface> handlers = entity.getBusinessHandlerSet();
		if (handlers != null && handlers.size() > 0) {
			for (BusinessHandlerInterface handler : handlers) {
				if (handler instanceof AbstractBusinessHandler) {
					AbstractBusinessHandler buziHandler = (AbstractBusinessHandler) handler;
					buziHandler.setEndpointEntity(entity);
					if (buziHandler.isSharable()) {
						pipe.addLast(buziHandler.name(), buziHandler);
					} else {
						AbstractBusinessHandler cloned = null;
						try {
							cloned = buziHandler.clone();

						} catch (CloneNotSupportedException e) {
							logger.error("handlers is not shareable and not implements Cloneable", e);
						}
						if (cloned != null) {
							cloned.setEndpointEntity(entity);
							pipe.addLast(buziHandler.name(), cloned);
							logger.info("handlers is not shareable . clone it success. {}", cloned);
						}
					}

				} else {
					handler.setEndpointEntity(entity);
					pipe.addLast(handler.name(), handler);
					logger.info("add share handlers . {}", handler);
				}
			}
		}
		// 黑洞处理，丢弃所有消息
		pipe.addLast("BlackHole", GlobalConstance.blackhole);

	}

	protected abstract void initSslCtx(Channel ch, EndpointEntity entity);

	protected long doCalculateSize(Object msg) {
		if (msg instanceof BaseMessage) {
			BaseMessage req = (BaseMessage) msg;
			if (req.isRequest()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return -1L;
		}
	}

	public Channel[] getallChannel() {
		return channels.getall();
	}

	/**
	 * 循环列表，用于实现轮循算法
	 */
	private class CircularList {
		private List<Channel> collection = Collections.synchronizedList(new ArrayList<Channel>(20));

		public Channel[] getall() {
			return collection.toArray(new Channel[collection.size()]);
		}

		public int size() {
			return collection.size();
		}

		public Channel fetch() {
			int size = collection.size();
			if (size == 0)
				return null;

			int idx = indexSeq.incrementAndGet();

			try {
				Channel ret = collection.get((idx & 0xffff) % size);
				return ret;
			} catch (IndexOutOfBoundsException ex) {
				// 多线程情况可能抛异常
				// 1：当线连接数为0了
				// 2：当前连接数小于index
				return collection.isEmpty() ? null : collection.get(0);
			} finally {
			}
		}

		public boolean add(Channel ele) {

			boolean r = false;
			try {
				r = collection.add(ele);
			} finally {
			}
			return r;
		}

		public boolean remove(Channel ele) {

			boolean r = false;
			try {
				r = collection.remove(ele);
			} finally {
			}
			return r;
		}

		private AtomicInteger indexSeq = new AtomicInteger();
	}

	private class MessageChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {
		public MessageChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
			super(writeLimit, readLimit, checkInterval);
			// 积压75条,或者延迟超过2.5s就不能再写了
			setMaxWriteSize(75);
			setMaxWriteDelay(2500);
		}

		@Override
		protected long calculateSize(Object msg) {
			if (msg instanceof ByteBuf) {
				return ((ByteBuf) msg).readableBytes();
			}
			if (msg instanceof ByteBufHolder) {
				return ((ByteBufHolder) msg).content().readableBytes();
			}
			return doCalculateSize(msg);
		}
	}

	public ChannelFuture asynwrite(Object msg) {
		Channel ch = fetchOneWritable();
		if (ch == null)
			return null;
		ChannelFuture future = ch.writeAndFlush(msg);
		return future;
	}

	public <T extends BaseMessage> List<Promise<T>> synwrite(List<T> msgs) {
		Channel ch = fetchOneWritable();
		if (ch == null)
			return null;
		AbstractSessionStateManager session = (AbstractSessionStateManager) ch.pipeline().get(sessionHandler);
		if (session == null)
			return null;
		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		for (BaseMessage msg : msgs) {
			arrPromise.add(session.writeMessagesync(msg));
		}

		return arrPromise;
	}

	public <T extends BaseMessage> Promise<T> synwrite(T message) {
		Channel ch = fetchOneWritable();
		if (ch == null)
			return null;
		AbstractSessionStateManager session = (AbstractSessionStateManager) ch.pipeline().get(sessionHandler);
		return session.writeMessagesync(message);
	}

	private Channel fetchOneWritable() {
		Channel ch = fetch();
		// 端口上还没有可用连接
		if (ch == null)
			return null;

		if (ch.isActive() && ch.isWritable()) {
			return ch;
		}
		return null;
	}

}
