package com.zx.sms.connect.manager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.cmpp.CMPPMessageLogHandler;
import com.zx.sms.handler.cmpp.ReWriteSubmitMsgSrcHandler;
import com.zx.sms.session.cmpp.SessionLoginManager;
import com.zx.sms.session.cmpp.SessionState;
import com.zx.sms.session.cmpp.SessionStateManager;

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

	public void addChannel(Channel ch) {
		EndpointEntity endpoint = getEndpointEntity();
		// 标识连接已建立
		ch.attr(GlobalConstance.attributeKey).set(SessionState.Connect);
		getChannels().add(ch);
		int cnt = incrementConn();
		// 如果是CMPP端口
		if (endpoint instanceof CMPPEndpointEntity) {
			CMPPEndpointEntity cmppentity = (CMPPEndpointEntity)endpoint;
			Map<Long, Message> storedMap = null;
			if( cmppentity.isReSendFailMsg()){
				// 如果上次发送失败的消息要重发一次，则要创建持久化Map用于存储发送的message
				 storedMap = BDBStoredMapFactoryImpl.INS.buildMap(endpoint.getId(), "Session_" +endpoint.getId());
			}else{
				 storedMap = new HashMap<Long, Message>();
			}
			
			Map<Long, Message> preSendMap = new HashMap<Long, Message>();

			logger.debug("Channel added To Endpoint {} .totalCnt:{} ,Channel.ID: {}", endpoint, cnt, ch.id());
			if (cnt == 1 && cmppentity.isReSendFailMsg()) {
				// 如果是第一个连接。要把上次发送失败的消息取出，再次发送一次

				if (storedMap != null && storedMap.size() > 0) {
					try {
						for (Map.Entry<Long, Message> entry : storedMap.entrySet()) {
							Message msg = entry.getValue();
							//不能重发Term消息。
							//如果上次连接关闭时Term消息未收到响应，重发会导致本次连接关闭
							if(msg.getHeader().getCommandId()!= CmppPacketType.CMPPTERMINATEREQUEST.getCommandId()&&
									msg.getHeader().getCommandId()!= CmppPacketType.CMPPTERMINATERESPONSE.getCommandId())
							{
								preSendMap.put(entry.getKey(), entry.getValue());
							}else{
								logger.warn("last CMPPTERMINATE msg may not success.");
							}
							
						}
					} catch (Exception e) {
						logger.warn("get storedMessage err ", e);
					}finally{
						//删除所有积压的消息
						storedMap.clear();
					}
				}
			}
			

			// 增加流量整形 ，每个连接每秒发送，接收消息数不超过配置的值
			// 这个放在前边，保证真实发送到连接上的速率。可以避免网关抖动造成的发送超速。
			// 网关抖动时，网关会先积压大量response，然后突然发送大量response给SessionManager,
			// 造成在Session里等待的消息集中发送,因此造成超速。
			ch.pipeline().addBefore(CMPPCodecChannelInitializer.codecName, "CMPPChannelTrafficBefore",
					new CMPPChannelTrafficShapingHandler(cmppentity.getWriteLimit(), cmppentity.getReadLimit(), 250));

			// 将SessinManager放在messageHeaderCodec后边。因为要处理Submit 和
			// deliver消息的长短信分拆
			ch.pipeline().addBefore(CMPPCodecChannelInitializer.codecName, "sessionStateManager", new SessionStateManager(cmppentity, storedMap, preSendMap));

			// 这个放在后边，限制发送方的速度，超速后设置连接不可写
			ch.pipeline().addBefore(CMPPCodecChannelInitializer.codecName, "CMPPChannelTrafficAfter",
					new CMPPChannelTrafficShapingHandler(cmppentity.getWriteLimit(), cmppentity.getReadLimit(), 250));
			// 加载业务handler
			bindHandler(ch.pipeline(), cmppentity);
		}
	}

	public void removeChannel(Channel ch) {
		ch.attr(GlobalConstance.attributeKey).set(SessionState.DisConnect);
		if (getChannels().remove(ch))
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

		pipe.addFirst("socketLog", new LoggingHandler(String.format(GlobalConstance.loggerNamePrefix, entity.getId()), LogLevel.TRACE));
		pipe.addLast("msgLog", new CMPPMessageLogHandler(entity));

		if (entity instanceof ClientEndpoint) {
			pipe.addLast("reWriteSubmitMsgSrcHandler", new ReWriteSubmitMsgSrcHandler(entity));
		}

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

	protected ChannelInitializer<?> initPipeLine() {
		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();

				if (getSslCtx() != null && getEndpointEntity().isUseSSL()) {
					initSslCtx(ch, getEndpointEntity());
				}

				CMPPCodecChannelInitializer codec = null;
				if (getEndpointEntity() instanceof CMPPEndpointEntity) {
					pipeline.addLast(GlobalConstance.IdleCheckerHandlerName,
							new IdleStateHandler(0, 0, ((CMPPEndpointEntity) getEndpointEntity()).getIdleTimeSec(), TimeUnit.SECONDS));
					codec = new CMPPCodecChannelInitializer(((CMPPEndpointEntity) getEndpointEntity()).getVersion());

				} else {
					pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
					codec = new CMPPCodecChannelInitializer();
				}

				pipeline.addLast("CmppServerIdleStateHandler", GlobalConstance.idleHandler);
				pipeline.addLast(codec.pipeName(), codec);

				pipeline.addLast("sessionLoginManager", new SessionLoginManager(getEndpointEntity()));
			}
		};
	}
	
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

	/**
	 * 重写了calculateSize方法，按消息条数计算流量
	 *
	 **/

	private class CMPPChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {
		public CMPPChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
			super(writeLimit, readLimit, checkInterval);
			// 积压75条,或者延迟超过2.5s就不能再写了
			setMaxWriteSize(75);
			setMaxWriteDelay(2500);
		}

		private boolean isRequestMsg(Message msg) {
			long commandId = msg.getHeader().getCommandId();
			return (commandId & 0x80000000L) == 0L;
		}

		@Override
		protected long calculateSize(Object msg) {
			if (msg instanceof ByteBuf) {
				return ((ByteBuf) msg).readableBytes();
			}
			if (msg instanceof ByteBufHolder) {
				return ((ByteBufHolder) msg).content().readableBytes();
			}
			if (msg instanceof Message) {
				// 只计算Request信息
				if (isRequestMsg((Message) msg)) {
					return 1;
				}
			}
			return -1;
		}
	}

}
