package com.zx.sms.connect.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.cmpp.CMPPMessageLogHandler;
import com.zx.sms.session.cmpp.SessionLoginManager;
import com.zx.sms.session.cmpp.SessionState;
import com.zx.sms.session.cmpp.SessionStateManager;

/**
 * @author Lihuanghe(18852780@qq.com)
 */
public abstract class AbstractEndpointConnector implements EndpointConnector<EndpointEntity> {
	private static final Logger logger = LoggerFactory.getLogger(AbstractEndpointConnector.class);

	private volatile AtomicInteger conCnt = new AtomicInteger();

	/**
	 * 端口
	 */
	private EndpointEntity endpoint;

	private CircularList<Channel> channels = new CircularList<Channel>();

	public AbstractEndpointConnector(EndpointEntity endpoint) {
		this.endpoint = endpoint;
	}

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
		//将channel移除
		removeChannel(channel);
	}

	@Override
	public synchronized void close() throws Exception {
		Channel ch = channels.fetch();
		while(ch!=null){
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

	private CircularList<Channel> getChannels() {
		return channels;
	}

	public synchronized void addChannel(Channel ch) {
		int maxChannels = getEndpointEntity().getMaxChannels();

		if (maxChannels != 0 && maxChannels <= getConnectionNum()) {
			logger.warn("MaxChannels config is {}. no more channel will be created . ", maxChannels);
			ch.close();
			return;
		}
		// 标识连接已建立
		ch.attr(GlobalConstance.attributeKey).set(SessionState.Connect);
		getChannels().add(ch);
		int cnt = incrementConn();
		// 如果是CMPP端口
		if (getEndpointEntity() instanceof CMPPEndpointEntity) {
			// 创建持久化Map用于存储发送的message
			Map<Long, Message> storedMap = BDBStoredMapFactoryImpl.INS.buildMap(getEndpointEntity().getId(), "Session_" + getEndpointEntity().getId());
			Map<Long, Message> preSendMap = new HashMap<Long, Message>();

			logger.debug("Channel added To Endpoint {} .totalCnt:{} ,Channel.ID: {}", endpoint, cnt, ch.id());
			if (cnt == 1) {
				// 如果是第一个连接。要把上次发送失败的消息取出，再次发送一次

				if (storedMap != null && storedMap.size() > 0) {
					for (Map.Entry<Long, Message> entry : storedMap.entrySet()) {
						preSendMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			CMPPEndpointEntity cmppentity = (CMPPEndpointEntity) getEndpointEntity();
			//将SessinManager放在messageHeaderCodec后边。因为要处理Submit 和 deliver消息的长短信分拆
			ch.pipeline().addBefore(CMPPCodecChannelInitializer.codecName,"sessionStateManager", new SessionStateManager(cmppentity, storedMap, preSendMap));
			// 加载业务handler
			bindHandler(ch.pipeline(), cmppentity);
		}
	}

	public synchronized void removeChannel(Channel ch) {
		ch.attr(GlobalConstance.attributeKey).set(SessionState.DisConnect);
		getChannels().remove(ch);
		decrementConn();
	}

	/**
	 * 连接建立成功后要加载的channelHandler
	 */
	protected void bindHandler(ChannelPipeline pipe, CMPPEndpointEntity entity) {
		// 修改连接空闲时间,使用server.xml里配置的连接空闲时间生效
		if (entity instanceof CMPPServerChildEndpointEntity) {
			ChannelHandler handler = pipe.get(GlobalConstance.IdleCheckerHandlerName);
			if (handler != null) {
				pipe.replace(handler, GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
			}
		}
		pipe.addFirst("socketLog", new LoggingHandler(entity.getId(),LogLevel.TRACE));
		pipe.addLast("msgLog",new CMPPMessageLogHandler(entity));
		
		pipe.addLast("CmppActiveTestRequestMessageHandler", GlobalConstance.activeTestHandler);
		pipe.addLast("CmppActiveTestResponseMessageHandler", GlobalConstance.activeTestRespHandler);
		pipe.addLast("CmppTerminateRequestMessageHandler", GlobalConstance.terminateHandler);
		pipe.addLast("CmppTerminateResponseMessageHandler", GlobalConstance.terminateRespHandler);

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
							pipe.addLast(buziHandler.name(), cloned);
							logger.info("handlers is not shareable . clone it success. {}",cloned);
						}
					}

				}
			}
		}
		// 黑洞处理，丢弃所有消息
		pipe.addLast("BlackHole", GlobalConstance.blackhole);

	}
	
	
	protected ChannelInitializer<?> initPipeLine() {
		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
			
				CMPPCodecChannelInitializer codec = null;
				if(getEndpointEntity() instanceof CMPPEndpointEntity){
					pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, ((CMPPEndpointEntity)getEndpointEntity()).getIdleTimeSec(), TimeUnit.SECONDS));
					codec = new CMPPCodecChannelInitializer(((CMPPEndpointEntity)getEndpointEntity()).getVersion());
					
				}
				else{
					pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
					codec = new CMPPCodecChannelInitializer();
				}

				pipeline.addLast("CmppServerIdleStateHandler", GlobalConstance.idleHandler);
				pipeline.addLast(codec.pipeName(), codec);
			
				pipeline.addLast("sessionLoginManager", new SessionLoginManager(getEndpointEntity()));
			}
		};
	}

	/**
	 * 循环列表，用于实现轮循算法
	 */
	private class CircularList<T> {
		private AtomicInteger index = new AtomicInteger();
		private List<T> collection = new ArrayList<T>();

		public synchronized T fetch() {

			int size = collection.size();
			if (size == 0)
				return null;

			int idx = index.getAndIncrement();
			T ret = collection.get(idx % size);
			// 超过65535归0
			index.compareAndSet(0xffff, 0);
			return ret;
		}

		public synchronized boolean add(T ele) {

			return collection.add(ele);
		}

		public synchronized boolean remove(T ele) {

			return collection.remove(ele);
		}
	}

}
