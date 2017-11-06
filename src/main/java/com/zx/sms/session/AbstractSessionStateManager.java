package com.zx.sms.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.session.cmpp.SessionState;

/**
 * @author Lihuanghe(18852780@qq.com) 消息发送窗口拜你控制和消息重发 ，消息持久化
 */
public abstract class AbstractSessionStateManager<K,T extends Serializable> extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSessionStateManager.class);
	// 用来记录连接上的错误消息
	private final Logger errlogger;
	
	//cmpp自定义的可写标致位，1-3已被 AbstractTrafficShapingHandler 使用。
	final int userDefinedWritabilityIndex = 4;

	/**
	 * @param entity
	 *            Session关联的端口
	 * @param storeMap
	 *            存储该端口上的持久化消息，存储不会很大，收到消息resp后就会删除持久化消息。
	 * @param preSend
	 *            预发送数据，连接建立后要发送的数据
	 */
	public AbstractSessionStateManager(EndpointEntity entity, Map<K, T> storeMap, Map<K, T> preSend) {
		this.entity = entity;
		errlogger = LoggerFactory.getLogger("error."+entity.getId());
		this.storeMap = storeMap;
		this.preSend = preSend;
	}

	/**
	 * 连接流量统计
	 **/
	private long msgReadCount = 0;
	private long msgWriteCount = 0;
	private EndpointEntity entity;
	
	private  final static ScheduledThreadPoolExecutor msgResend = new ScheduledThreadPoolExecutor(Integer.valueOf(PropertiesUtils.getproperties("GlobalMsgResendThreadCount","4")),new ThreadFactory() {
	      
        private final AtomicInteger threadNumber = new AtomicInteger(1);
      
        public Thread newThread(Runnable r) {
            Thread t = new Thread( r,"msgResend-" + threadNumber.getAndIncrement());
            
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    },new ThreadPoolExecutor.DiscardPolicy());

	
	
	/**
	 * 重发队列
	 **/
	private final ConcurrentHashMap<K, Entry> msgRetryMap = new ConcurrentHashMap<K, Entry>();
	/**
	 * 发送未收到resp的消息，需要使用可持久化的Map.
	 */
	private final Map<K, T> storeMap;

	/**
	 * 会话刚建立时要发送的数据
	 */
	private Map<K, T> preSend;
	
	public int getWaittingResp(){
		return storeMap.size();
	}

	public long getReadCount(){
		return msgReadCount;
	}
	
	public long getWriteCount(){
		return msgWriteCount;
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.warn("Connection closed. channel:{}", ctx.channel());
		// 取消重试队列里的任务
		for (Map.Entry<K, Entry> entry : msgRetryMap.entrySet()) {
			T requestmsg = entry.getValue().request;

			EndpointConnector conn = EndpointManager.INS.getEndpointConnector(entity);
			// 所有连接都已关闭
			if (conn == null)
				break;

			Channel ch = conn.fetch();

			if (ch != null && ch.isActive()) {

				if (entity.isReSendFailMsg()) {
					// 连接断连，但是未收到Resp的消息，通过其它连接再发送一次
					ch.writeAndFlush(requestmsg);
					logger.debug("current channel {} is closed.send requestMsg from other channel {} which is active.", ctx.channel(), ch);
				} else {
					errlogger.error("Channel closed . Msg {} may not send Success. ", requestmsg);
				}
			}
			cancelRetry(requestmsg, ctx.channel());
		}

		//设置连接为可写
		setUserDefinedWritability(ctx.channel(), true);
		ctx.fireChannelInactive();
	}
	protected abstract K getSequenceId(T msg);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		msgReadCount++;
		if (msg instanceof Serializable) {
			final T message = (T) msg;
			// 设置消息的生命周期

			// 如果是resp，取消息消息重发
			if (!isRequestMsg(message)) {
				// 删除发送成功的消息
				K key = getSequenceId(message);
				T request = storeMap.remove(key);
				if (request != null) {
					Entry cancelentry = cancelRetry(request, ctx.channel());
				}
				else{
					errlogger.warn("receive ResponseMessage ,but not found related Request Msg. {}",message);
				}
			}
		}
		ctx.fireChannelRead(msg);
	}

	protected abstract boolean checkTerminateLife(Object msg);
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

		if (msg instanceof Serializable) {

			// 发送消息超过生命周期
			
			if (!checkTerminateLife(msg)) {
				errlogger.error("Msg Life over .{}", msg);
				promise.setFailure(new RuntimeException("Msg Life over"));
				return;
			}

			if (isRequestMsg((T) msg)) {
				// 发送，未收到Response时，60秒后重试,
				writeWithWindow(ctx, (T) msg, promise);
			} else {
				// 发送Response消息时直接发送
				ctx.write(msg, promise);
			}
		} else {
			// 不是Message消息时直接发送
			ctx.write(msg, promise);
		}
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt == SessionState.Connect) {
			preSendMsg(ctx);
		}
		ctx.fireUserEventTriggered(evt);
	}

    void setUserDefinedWritability(Channel ch, boolean writable) {
        ChannelOutboundBuffer cob = ch.unsafe().outboundBuffer();
        if (cob != null ) {
            cob.setUserDefinedWritability(userDefinedWritabilityIndex, writable);
        }
    }
    
	/**
	 * 获取发送窗口，并且注册重试任务
	 **/
	private boolean writeWithWindow(final ChannelHandlerContext ctx, final T message, final ChannelPromise promise) {
		
		try {
			safewrite(ctx, message, promise);
		} catch (Exception e) {
			promise.setFailure(e);
			logger.error("writeWithWindow: ", e.getCause() != null ? e.getCause() : e);
		}
		return true;
	}

	private void scheduleRetryMsg(final ChannelHandlerContext ctx, final T message) {

		final K seq = getSequenceId(message);

		final Entry entry = msgRetryMap.get(seq);

		// 发送次数大于1时要重发
		if (entry != null && entity.getMaxRetryCnt() > 1) {

			/*
			 * TODO bugfix:不知道什么原因，会导致 下面的future任务没有cancel掉。
			 * 这里增加一个引用，当会试任务超过次数限制后，cancel掉自己。
			 * 些任务不能被中断interupted.如果storeMap.remove()被中断会破坏BDB的内部状态，使用BDB无法继续工作
			 */
			final AtomicReference<Future> ref = new AtomicReference<Future>();
			
			Runnable task = new Runnable() {

				@Override
				public void run() {
					try {
						int times = entry.cnt.get();
						logger.warn("retry Send Msg : {}", message);
						if (times > entity.getMaxRetryCnt()) {

							//会有future泄漏的情况发生，这里cancel掉自己，来规避泄漏
							Future future = ref.get();
							if (future != null)
								future.cancel(false);

							cancelRetry(message, ctx.channel());

							// 删除发送成功的消息
							storeMap.remove(seq);
							// 发送3次都失败的消息要记录

							logger.error("retry send msg {} times。cancel retry task", times);

							errlogger.error("RetryFailed: {}", message);
							
							logger.error("retry send Message {} 3 times,the connection may die.close it",message);
							ctx.close();
							
						} else {

							msgWriteCount++;
							entry.cnt.incrementAndGet();
							// 重发不用申请窗口
							ctx.writeAndFlush(message, ctx.newPromise());
						}
					} catch (Throwable e) {
						logger.error("retry Send Msg Error: {}", message);
						logger.error("retry send Msg Error.", e);
					}
				}
			};
			Future<?> future = msgResend.scheduleWithFixedDelay(task, entity.getRetryWaitTimeSec(), entity.getRetryWaitTimeSec(), TimeUnit.SECONDS);

			ref.set(future);

			entry.future = future;

			// 这里增加一次判断，是否已收到resp消息,已到resp后，msgRetryMap里的entry会被 remove掉。
			if (msgRetryMap.get(seq) == null) {
				future.cancel(false);
			}

		} else if (entry == null) {
			// 当程序执行到这里时，可能已收到resp消息，此时entry为空。
			logger.warn("receive seq {} not exists in msgRetryMap,maybe response received before create retrytask .", seq);
		}

	}

	private Entry cancelRetry(T requestMsg, Channel channel) {
		Entry entry = msgRetryMap.remove(getSequenceId(requestMsg));
	
		if (entry != null && entry.future != null) {
			entry.future.cancel(false);
			//删除任务
			if(entry.future instanceof RunnableScheduledFuture){
				msgResend.remove((RunnableScheduledFuture)entry.future);
			}
			
		}else{
			logger.warn("cancelRetry task failed.");
		}
		return entry;
	}

	protected abstract boolean isRequestMsg(T msg);

	private void preSendMsg(ChannelHandlerContext ctx) {
		if (preSend != null && preSend.size() > 0) {
			for (Map.Entry<K, T> entry : preSend.entrySet()) {
				K key = entry.getKey();
				T msg = entry.getValue();

				if (msg != null) {
					// 如果配置了失败重发
					if (entity.isReSendFailMsg()) {
						logger.debug("Send last failed msg . {}", msg);
						storeMap.remove(key);
						writeWithWindow(ctx, msg, ctx.newPromise());
					} else {
						// 删除消息不重发
						storeMap.remove(key);
						errlogger.warn("msg send may not success. {}", msg);
					}
				}

			}
			// 让GC回收内存
			preSend.clear();
			preSend = null;
		}
	}

	/**
	 * 发送msg,首先做消息持久化
	 */
	private void safewrite(final ChannelHandlerContext ctx, final T message, final ChannelPromise promise) {
		if (ctx.channel().isActive()) {
			final K seq = getSequenceId(message);

			// 记录已发送的请求,在发送msg前生记录到map里。防止生成retryTask前就收到resp的情况发生
			Entry tmpentry = new Entry(message);
			Entry old = msgRetryMap.putIfAbsent(seq, tmpentry);

			if (old != null) {
				// bugfix: 集群环境下可能产生相同的seq. 如果已经存在一个相同的seq.
				// 此消息延迟250ms再发
				logger.error("has repeat Sequense {}", seq);

				reWriteLater(ctx, message, promise, 250);

			} else {
				msgWriteCount++;
				// 持久化到队列
				storeMap.put(seq, message);

				ctx.write(message,promise);

				// 注册重试任务
				scheduleRetryMsg(ctx, message);
				ctx.flush();
			}

		} else {
			// 如果连接已关闭，通知上层应用
			if (promise != null && (!promise.isDone())) {
				StringBuilder sb = new StringBuilder();
				sb.append("Connection ").append(ctx.channel()).append(" has closed");
				promise.setFailure(new IOException(sb.toString()));
			}
		}
	}

	private void reWriteLater(final ChannelHandlerContext ctx, final T message, final ChannelPromise promise, final int delay) {
		msgResend.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					write(ctx, message, promise);
				} catch (Exception e) {
					logger.error("has repeat Sequense ,and write Msg err {}", message);
				}
			}

		}, delay, TimeUnit.MILLISECONDS);
	}
	
	private class Entry {
		// 保证future的可见性，
		volatile Future future;
		AtomicInteger cnt = new AtomicInteger(1);
		T request;

		Entry(T request) {
			this.request = request;
		}
	}

}
