package com.zx.sms.connect.manager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.cmpp.SessionState;

/**
 * @author Lihuanghe(18852780@qq.com)
 */
public abstract class AbstractEndpointConnector implements EndpointConnector<EndpointEntity> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractEndpointConnector.class);

	private volatile AtomicInteger conCnt = new AtomicInteger();

	private SslContext sslCtx = null;
	/**
	 * 端口
	 */
	private EndpointEntity endpoint;

	private CircularList channels = new CircularList();

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
		try {
			if (channel.isOpen())
				channel.close().sync();

		} catch (InterruptedException e) {
			logger.error("close channel Error ", e);
		}
		// 将channel移除
		removeChannel(channel);
	}

	@Override
	public void close() throws Exception {
		Channel ch = channels.fetch();
		while (ch != null) {
			close(ch);
			ch = channels.fetch();
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

		return conCnt.get();
	}

	protected int incrementConn() {
		return conCnt.incrementAndGet();
	}

	protected int decrementConn() {
		return conCnt.decrementAndGet();
	}

	private CircularList getChannels() {
		return channels;
	}

	protected abstract AbstractSessionStateManager createSessionManager(EndpointEntity entity, Map storeMap, Map preSend);

	protected abstract void doBindHandler(ChannelPipeline pipe, EndpointEntity entity);

	protected abstract void doinitPipeLine(ChannelPipeline pipeline);

	protected ChannelInitializer<?> initPipeLine() {

		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();

				if (getEndpointEntity().isUseSSL() && getSslCtx() != null) {
					initSslCtx(ch, getEndpointEntity());
				}
				doinitPipeLine(pipeline);
			}
		};
	};

	public void addChannel(Channel ch) {

		// 标识连接已建立
		ch.attr(GlobalConstance.attributeKey).set(SessionState.Connect);
		getChannels().add(ch);
		int cnt = incrementConn();

		EndpointEntity endpoint = getEndpointEntity();
		Map<Serializable, Serializable> storedMap = null;
		if (endpoint.isReSendFailMsg()) {
			// 如果上次发送失败的消息要重发一次，则要创建持久化Map用于存储发送的message
			storedMap = BDBStoredMapFactoryImpl.INS.buildMap(endpoint.getId(), "Session_" + endpoint.getId());
		} else {
			storedMap = new HashMap();
		}

		Map preSendMap = new HashMap();

		logger.debug("Channel added To Endpoint {} .totalCnt:{} ,remoteAddress: {}", endpoint, cnt, ch.remoteAddress());
		if (cnt == 1 && endpoint.isReSendFailMsg()) {
			// 如果是第一个连接。要把上次发送失败的消息取出，再次发送一次

			if (storedMap != null && storedMap.size() > 0) {
				try {
					for (Map.Entry<Serializable, Serializable> entry : storedMap.entrySet()) {
						Serializable msg = entry.getValue();
						preSendMap.put(entry.getKey(), entry.getValue());
					}
				} catch (Exception e) {
					logger.warn("get storedMessage err ", e);
				} finally {
					// 删除所有积压的消息
					storedMap.clear();
				}
			}
		}
		
	// 增加流量整形 ，每个连接每秒发送，接收消息数不超过配置的值
		ch.pipeline().addAfter(GlobalConstance.codecName, "ChannelTrafficAfter",
				new MessageChannelTrafficShapingHandler(endpoint.getWriteLimit(), endpoint.getReadLimit(), 250));
		
		ch.pipeline().addAfter(GlobalConstance.codecName, "sessionStateManager", createSessionManager(endpoint, storedMap, preSendMap));
		
		bindHandler(ch.pipeline(), getEndpointEntity());
	}

	public void removeChannel(Channel ch) {
		ch.attr(GlobalConstance.attributeKey).set(SessionState.DisConnect);
		if (getChannels().remove(ch))
			decrementConn();
	}

	/**
	 * 连接建立成功后要加载的channelHandler
	 */
	protected void bindHandler(ChannelPipeline pipe, EndpointEntity entity) {

		// 调用子类的bind方法
		doBindHandler(pipe, entity);

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

				}
			}
		}
		// 黑洞处理，丢弃所有消息
		pipe.addLast("BlackHole", GlobalConstance.blackhole);

	}

	protected abstract void initSslCtx(Channel ch, EndpointEntity entity);
	
	protected long doCalculateSize(Object msg){
		if(msg instanceof BaseMessage){
			BaseMessage req = (BaseMessage)msg;
			if(req.isRequest()){
				return 1;
			}else{
				return 0;
			}
		}else{
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
		private ReadWriteLock lock = new ReentrantReadWriteLock();
		private List<Channel> collection = new ArrayList<Channel>();

		public Channel[] getall() {
			return collection.toArray(new Channel[0]);
		}

		public Channel fetch() {

			try {
				lock.readLock().lock();
				int size = collection.size();
				if (size == 0)
					return null;

				int idx = (int) DefaultSequenceNumberUtil.getNextAtomicValue(indexSeq, Limited);
				Channel ret = collection.get(idx % size);
				// 超过65535归0
				return ret;
			} finally {
				lock.readLock().unlock();
			}
		}

		public boolean add(Channel ele) {

			boolean r = false;
			try {
				lock.writeLock().lock();
				r = collection.add(ele);
			} finally {
				lock.writeLock().unlock();
			}
			return r;
		}

		public boolean remove(Channel ele) {

			boolean r = false;
			try {
				lock.writeLock().lock();
				r = collection.remove(ele);
			} finally {
				lock.writeLock().unlock();
			}
			return r;
		}

		private final static long Limited = 65535L;
		private AtomicLong indexSeq = new AtomicLong();
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

}
