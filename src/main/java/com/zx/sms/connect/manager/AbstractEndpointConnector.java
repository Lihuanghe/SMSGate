package com.zx.sms.connect.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.session.SessionState;

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
	
	protected abstract void doAddChannel(Channel ch,int cnt);
	protected abstract void doBindHandler(ChannelPipeline pipe, EndpointEntity entity);
	protected abstract ChannelInitializer<?> initPipeLine();
	public void addChannel(Channel ch) {
		
		// 标识连接已建立
		ch.attr(GlobalConstance.attributeKey).set(SessionState.Connect);
		getChannels().add(ch);
		int cnt = incrementConn();
		doAddChannel(ch,cnt);
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
		
		//调用子类的bind方法
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


	
	public Channel[] getallChannel(){
		return channels.getall();
	}

	/**
	 * 循环列表，用于实现轮循算法
	 */
	private class CircularList {
		private ReadWriteLock lock = new ReentrantReadWriteLock();
		private List<Channel> collection = new ArrayList<Channel>();
		
		public Channel[] getall(){
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



}
