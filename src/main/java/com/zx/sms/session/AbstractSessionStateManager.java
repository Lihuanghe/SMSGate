package com.zx.sms.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.IOException;
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

import com.zx.sms.BaseMessage;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.session.cmpp.SessionState;

/**
 * @author Lihuanghe(18852780@qq.com) 消息发送窗口拜你控制和消息重发 ，消息持久化
 */
public abstract class AbstractSessionStateManager<K, T extends BaseMessage> extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSessionStateManager.class);
	// 用来记录连接上的错误消息
	private final Logger errlogger;

	/**
	 * @param entity
	 *            Session关联的端口
	 * @param storeMap
	 *            存储该端口上的持久化消息，存储不会很大，收到消息resp后就会删除持久化消息。
	 * @param preSend
	 *            预发送数据，连接建立后要发送数据
	 */
	public AbstractSessionStateManager(EndpointEntity entity, Map<K, VersionObject<T>> storeMap, boolean preSend) {
		this.entity = entity;
		errlogger = LoggerFactory.getLogger("error." + entity.getId());
		this.storeMap = storeMap;
		this.preSend = preSend;
	}

	/**
	 * 连接流量统计
	 **/
	private long msgReadCount = 0;
	private long msgWriteCount = 0;
	private EndpointEntity entity;

	private final long version = System.currentTimeMillis();

	private final static ScheduledThreadPoolExecutor msgResend = new ScheduledThreadPoolExecutor(Integer.valueOf(PropertiesUtils.getproperties(
			"GlobalMsgResendThreadCount", "4")), new ThreadFactory() {

		private final AtomicInteger threadNumber = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "msgResend-" + threadNumber.getAndIncrement());

			t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}, new ThreadPoolExecutor.DiscardPolicy());

	/**
	 * 重发队列
	 **/
	private final ConcurrentHashMap<K, Entry> msgRetryMap = new ConcurrentHashMap<K, Entry>();
	/**
	 * 发送未收到resp的消息，需要使用可持久化的Map.
	 */
	private final Map<K, VersionObject<T>> storeMap;

	/**
	 * 会话刚建立时要发送的数据
	 */
	private boolean preSend;

	private boolean preSendover = false;

	public int getWaittingResp() {
		return storeMap.size();
	}

	public long getReadCount() {
		return msgReadCount;
	}

	public long getWriteCount() {
		return msgWriteCount;
	}

	public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
		logger.warn("Connection closed. channel:{}", ctx.channel());

		ctx.executor().execute(new Runnable() {

			@Override
			public void run() {
				// 取消重试队列里的任务
				for (Map.Entry<K, Entry> entry : msgRetryMap.entrySet()) {
					T requestmsg = entry.getValue().request;

					EndpointConnector conn = EndpointManager.INS.getEndpointConnector(entity);
					// 所有连接都已关闭
					if (conn != null){
						Channel ch = conn.fetch();

						if (ch != null && ch.isActive()) {

							if (entity.isReSendFailMsg()) {
								// 连接断连，但是未收到Resp的消息，通过其它连接再发送一次
								ch.writeAndFlush(requestmsg);
								logger.warn("current channel {} is closed.send requestMsg {} from other channel {} which is active.", ctx.channel(),requestmsg, ch);
							} else {
								errlogger.error("Channel closed . Msg {} may not send Success. ", requestmsg);
							}
						}
					}
					cancelRetry(requestmsg, ctx.channel());
				}

				// 如果重发的消息没有发送完毕。从其它连接发送
				if (preSend && (!preSendover)) {
					for (Map.Entry<K, VersionObject<T>> entry : storeMap.entrySet()) {
						EndpointConnector conn = EndpointManager.INS.getEndpointConnector(entity);
						// 所有连接都已关闭
						if (conn == null)
							break;

						Channel ch = conn.fetch();

						if (ch != null && ch.isActive()) {
							K key = entry.getKey();

							VersionObject<T> vobj = entry.getValue();
							long v = vobj.getVersion();
							T msg = vobj.getObj();
							
							//只发送在本次连接建立之前的未成功的消息
							//v保存了消息创建时的时间
							if (version > v && msg != null) {
								// 如果配置了失败重发
								logger.debug("Send last failed msg . {}", msg);
								ch.writeAndFlush(msg);
							}
						}
					}
				}
			}

		});

		ctx.fireChannelInactive();
	}

	protected abstract K getSequenceId(T msg);

	protected abstract boolean needSendAgainByResponse(T req, T res);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		msgReadCount++;
		if (msg instanceof BaseMessage) {
			final T message = (T) msg;
			// 设置消息的生命周期

			// 如果是resp，取消息消息重发
			if (message.isResponse()) {
				// 删除发送成功的消息
				K key = getSequenceId(message);
				VersionObject<T> vobj = storeMap.remove(key);
				if (vobj != null) {
					T request = vobj.getObj();
					Entry cancelentry = cancelRetry(request, ctx.channel());

					// 根据Response 判断是否需要重发,比如CMPP协议，如果收到result==8，表示超速，需要重新发送
					if (needSendAgainByResponse(request, message)) {
						reWriteLater(ctx, request, ctx.newPromise(), 400);
					}

					// 把response关联上request供使用。
					message.setRequest(request);
				} else {
					errlogger.warn("receive ResponseMessage ,but not found related Request Msg. {}", message);
				}
			}
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object message, ChannelPromise promise) throws Exception {

		if (message instanceof BaseMessage) {
			BaseMessage msg = (BaseMessage) message;
			// 发送消息超过生命周期

			if (msg.isTerminated()) {
				errlogger.error("Msg Life over .{}", msg);
				promise.setFailure(new RuntimeException("Msg Life over"));
				return;
			}

			if (msg.isRequest()) {
				// 发送，未收到Response时，60秒后重试,
				writeWithWindow(ctx, (T) msg, promise);
			} else {
				// 发送Response消息时直接发送
				ctx.write(msg, promise);
			}
		} else {
			// 不是Message消息时直接发送
			ctx.write(message, promise);
		}
	}

	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt == SessionState.Connect) {
			ctx.executor().execute(new Runnable() {

				@Override
				public void run() {
					preSendMsg(ctx);
				}

			});

		}
		ctx.fireUserEventTriggered(evt);
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
						
						if(!ctx.channel().isActive()) return;
						
						int times = entry.cnt.get();
						
						logger.warn("retry Send Msg : {}", message);
						if (times >= entity.getMaxRetryCnt()) {

							// 会有future泄漏的情况发生，这里cancel掉自己，来规避泄漏
							Future future = ref.get();
							if (future != null)
								future.cancel(false);

							cancelRetry(message, ctx.channel());

							// 删除发送成功的消息
							storeMap.remove(seq);
							// 发送3次都失败的消息要记录

							logger.error("retry send msg {} times。cancel retry task", times);

							errlogger.error("RetryFailed: {}", message);

							logger.error("retry send Message {} 3 times,the connection may die.close it", message);
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
			// 删除任务
			if (entry.future instanceof RunnableScheduledFuture) {
				msgResend.remove((RunnableScheduledFuture) entry.future);
			}

		} else {
			logger.debug("cancelRetry task failed.");
		}
		return entry;
	}

	private void preSendMsg(ChannelHandlerContext ctx) {
		boolean isbreak = false;
		if (preSend) {
			for (Map.Entry<K, VersionObject<T>> entry : storeMap.entrySet()) {

				if (!ctx.channel().isActive()) {
					isbreak = true;
					break;
				}

				K key = entry.getKey();

				VersionObject<T> vobj = entry.getValue();
				long v = vobj.getVersion();
				T msg = vobj.getObj();

				//只发送在本次连接建立之前的未成功的消息
				//v保存了消息创建时的时间
				if (version > v && msg != null) {
					logger.debug("Send last failed msg . {}", msg);
					writeWithWindow(ctx, msg, ctx.newPromise());
				}
			}
		}
		preSendover = !isbreak;
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
				storeMap.put(seq, new VersionObject<T>(message));

				promise.addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {

						if (future.isSuccess()) {
							// 注册重试任务
							scheduleRetryMsg(ctx, message);
						}
					}

				});
				ctx.writeAndFlush(message, promise);
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
