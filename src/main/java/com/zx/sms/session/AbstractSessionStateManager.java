package com.zx.sms.session;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import com.zx.sms.common.SendFailException;
import com.zx.sms.common.SmsLifeTerminateException;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

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
	public AbstractSessionStateManager(EndpointEntity entity, ConcurrentMap<K, VersionObject<T>> storeMap, boolean preSend) {
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

	private final static ScheduledThreadPoolExecutor msgResend = new ScheduledThreadPoolExecutor(Integer.parseInt(PropertiesUtils.getProperties(
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
	private final ConcurrentMap<K, VersionObject<T>> storeMap;
	
	private ChannelHandlerContext ctx;

	/**
	 * 会话刚建立时要发送的数据
	 */
	private boolean preSend;
	
	private long minDelay;

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
	
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
		this.ctx = ctx;
	}
	
    private void setUserDefinedWritability(ChannelHandlerContext ctx, boolean writable) {
        ChannelOutboundBuffer cob = ctx.channel().unsafe().outboundBuffer();
        if (cob != null) {
            cob.setUserDefinedWritability(31, writable);
        }
    }

	public void channelInactive(final ChannelHandlerContext ctx) throws Exception {

		ctx.executor().execute(new Runnable() {

			@Override
			public void run() {
				// 取消重试队列里的任务
				EndpointConnector conn = entity.getSingletonConnector();
				
				for (Iterator<Map.Entry<K, Entry>> itor = msgRetryMap.entrySet().iterator();itor.hasNext(); ) {
					Map.Entry<K, Entry> entry = itor.next();
					if(entry == null) continue;
					Entry failedentry = entry.getValue();
					T requestmsg = failedentry.request;
					boolean async = !failedentry.sync;
					// 所有连接都已关闭
					if (conn != null){
						Channel ch = conn.fetch();
						if (ch != null && ch.isActive()) {
							if (entity.isReSendFailMsg() && async) {
								// 连接断连，但是未收到Resp的消息，异步发送时。通过其它连接再发送一次
								ch.writeAndFlush(requestmsg);
								logger.warn("current channel {} is closed.send requestMsg {} from other channel {} which is active.", ctx.channel(),requestmsg, ch);
							} else {
								errlogger.error("Channel closed . Msg {} may not send Success. ", requestmsg);
							}
						}
					}
					cancelRetry(failedentry, ctx.channel());
					responseFutureDone(failedentry, new IOException("channel closed."));
					itor.remove();
				}

				// 如果重发的消息没有发送完毕。从其它连接发送
				if (preSend && (!preSendover)) {
					for (Map.Entry<K, VersionObject<T>> storeentry : storeMap.entrySet()) {
						// 所有连接都已关闭
						if (conn == null)
							break;

						Channel ch = conn.fetch();

						if (ch != null && ch.isActive()) {
							K key = storeentry.getKey();

							VersionObject<T> vobj = storeentry.getValue();
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
	
	protected abstract boolean closeWhenRetryFailed(T req) ;

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		msgReadCount++;
		if (msg instanceof BaseMessage) {
			// 如果是resp，取消 消息重发
			if (((T)msg).isResponse()) {
				// 删除发送成功的消息
				final T response =  (T) msg;
				K key = getSequenceId(response);
				VersionObject<T> vobj = storeMap.remove(key);
				if (vobj != null) {
					T request = vobj.getObj();
					long sendtime = vobj.getVersion();
					
					// 把response关联上request供使用。
					response.setRequest(request);
					
					//响应延迟过大
					long delay = delaycheck(sendtime);
					//计算最小时延
					minDelay =  Math.min(minDelay, delay);
					if(delay > (entity.getRetryWaitTimeSec() * 1000/4)){
						errlogger.warn("delaycheck . delay :{} , SequenceId :{}", delay,getSequenceId(response));
						//接收response回复时延太高，有可能对端已经开始积压了，暂停发送。
						setchannelunwritable(ctx,delay-minDelay);
					}
					
					Entry entry = msgRetryMap.get(key);
					
					// 根据Response 判断是否需要重发,比如CMPP协议，如果收到result==8，表示超速，需要重新发送
					if (needSendAgainByResponse(request, response)) {
						//取消息重发任务,再次发送时重新注册任务
						cancelRetry(entry, ctx.channel());
						
						//网关异常时会发送大量超速错误(result=8),造成大量重发，浪费资源。这里先停止发送，过40毫秒再回恢复
						setchannelunwritable(ctx,delay);
						//延迟后重发
						reWriteLater(ctx, entry.request, ctx.newPromise(), delay);
						
					}else{
						cancelRetry(entry, ctx.channel());
						//给同步发送的promise响应resonse
						responseFutureDone(entry, response);
						msgRetryMap.remove(key);
					}
				} else {
					errlogger.warn("receive ResponseMessage ,but not found related Request Msg. response:{}", response);
				}
			}
		}
		ctx.fireChannelRead(msg);
	}

	//查检发送req与收到res的时间差
	private long delaycheck(long sendtime){
		return System.currentTimeMillis() - sendtime ;
	}
	
	private void setchannelunwritable(final ChannelHandlerContext ctx,long millitime){
		if(ctx.channel().isWritable()){
			setUserDefinedWritability(ctx, false);
			
			ctx.executor().schedule(new Runnable() {
				@Override
				public void run() {
						setUserDefinedWritability(ctx, true);
				}
			}, millitime, TimeUnit.MILLISECONDS);
		}
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object message, ChannelPromise promise) throws Exception {

		if (message instanceof BaseMessage) {
			BaseMessage msg = (BaseMessage) message;

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
			safewrite(ctx, message, promise,false);
		} catch (Exception e) {
			promise.tryFailure(e);
			logger.error("writeWithWindow: ", e.getCause() != null ? e.getCause() : e);
		}
		return true;
	}

	private void scheduleRetryMsg(final ChannelHandlerContext ctx, final T message) {

		final K seq = getSequenceId(message);

		final Entry entry = msgRetryMap.get(seq);

		if (entry != null) {

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
						
						logger.warn("entity : {} , retry Send Msg : {}", entity.getId(),message);
						if (times >= entity.getMaxRetryCnt()) {

							// 会有future泄漏的情况发生，这里cancel掉自己，来规避泄漏
							Future future = ref.get();
							if (future != null)
								future.cancel(false);

							cancelRetry(entry,ctx.channel());
							responseFutureDone(entry,new SendFailException("retry send msg over "+times+" times"));
							msgRetryMap.remove(seq);
							// 删除消息
							storeMap.remove(seq);
							// 重试发送都失败的消息要记录
							errlogger.error("entity : {} , RetryFailed: {}", entity.getId(),message);

							if(closeWhenRetryFailed(message)) {
								logger.error("entity : {} , retry send {} times Message {} ,the connection may die.close it", entity.getId(),times,message);
								ctx.close();
							}else {
								//不关闭通道的，设置连接不可写，1s后恢复
								setchannelunwritable(ctx,1000);
							}

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

	private Entry responseFutureDone(Entry entry,T response){
		if(entry!=null &&entry.resfuture!=null){
			entry.resfuture.setSuccess(response);
			return entry;
		}
		return null;
	}
	
	private Entry responseFutureDone(Entry entry,Throwable cause){
		if(entry!=null &&entry.resfuture!=null){
			entry.resfuture.tryFailure(cause);
			return entry;
		}
		return null;
	}
	
	private Entry cancelRetry(Entry entry, Channel channel) {
//		Entry entry = msgRetryMap.remove(getSequenceId(requestMsg));
		if (entry != null && entry.future != null) {
			entry.future.cancel(false);
			// 删除任务
			if (entry.future instanceof RunnableScheduledFuture) {
				msgResend.remove((RunnableScheduledFuture) entry.future);
			}
			entry.future = null;
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
	private Promise<T> safewrite(final ChannelHandlerContext ctx, final T message, final ChannelPromise promise,boolean syn) {
		if (ctx.channel().isActive()) {
			
			// 发送消息超过生命周期
			if (message.isTerminated()) {
				errlogger.error("Msg Life over .{}", message);
				promise.tryFailure(new SmsLifeTerminateException("Msg Life over"));
				
				DefaultPromise failed = new DefaultPromise<T>(ctx.executor());
				failed.tryFailure(new SmsLifeTerminateException("Msg Life over"));
				return failed;
			}
			
			final K seq = getSequenceId(message);
			// 记录已发送的请求,在发送msg前生记录到map里。防止生成retryTask前就收到resp的情况发生
			boolean has = msgRetryMap.containsKey(seq);
			Entry tmpentry = new Entry(message,syn);
			if (has) {
				Entry old = msgRetryMap.get(seq);
				
				//2018-08-27 当网关返回超速错时，也会存在想同的seq
				//消息相同表示此消息是因为超速错导致的重发,什么都不做。
				//否则可能是相同的seq，但不同的短信				
				if(!message.equals(old.request)){
					// bugfix: 集群环境下可能产生相同的seq. 如果已经存在一个相同的seq.
					// 此消息延迟250ms再发
					logger.error("has repeat Sequense {}", seq);
					if(syn){
						//同步调用时，立即返回失败。
						StringBuilder sb = new StringBuilder();
						sb.append("seqId:").append(seq);
						sb.append(".it Has a same sequenceId with another message:").append(old.request).append(". wait it complete.");
						IOException cause = new IOException(sb.toString());
						DefaultPromise failed = new DefaultPromise<T>(ctx.executor());
						failed.tryFailure(cause);
						return failed;
					}else{
						//异步调用时等250ms后再试发一次
						reWriteLater(ctx, message, promise, 250);
						return null;
					}
				}
			} else{
				//收到响应时将此对象设置为完成状态
				tmpentry.resfuture = new DefaultPromise<T>(ctx.executor());
				msgRetryMap.put(seq, tmpentry);
			}
			
			msgWriteCount++;
			// 持久化到队列
			storeMap.put(seq, new VersionObject<T>(message));

			promise.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {

					if (future.isSuccess()) {
						// 注册重试任务
						scheduleRetryMsg(ctx, message);
					}else {
						//发送失败,必须清除msgRetryMap里的对象，否则上层业务
						//可能提交相同seq的消息，造成死循环
						logger.error("remove fail message Sequense {}", seq);
						
						storeMap.remove(seq);
						Entry entry = msgRetryMap.remove(seq);
//						//发送到网络失败
						responseFutureDone(entry,future.cause());
					}
				}

			});
			ctx.writeAndFlush(message, promise);
			
			return tmpentry.resfuture;
		} else {
			// 如果连接已关闭，通知上层应用
			StringBuilder sb = new StringBuilder();
			sb.append("Connection ").append(ctx.channel()).append(" has closed");
			IOException cause = new IOException(sb.toString());
			
			if (promise != null && (!promise.isDone())) {
				promise.tryFailure(cause);
			}
			DefaultPromise<T> failed = new DefaultPromise<T>(ctx.executor());
			failed.tryFailure(cause);
			return failed;
		}
	}

	private void reWriteLater(final ChannelHandlerContext ctx, final T message, final ChannelPromise promise, final long delay) {
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
		boolean sync =  false;
		
		DefaultPromise<T> resfuture ;
		Entry(T request,boolean sync) {
			this.request = request;
			this.sync =  sync;
		}
	}
	
	public Promise<T> writeMessagesync(T message){
		return safewrite(ctx,message,ctx.newPromise(),true);
	}

	public EndpointEntity getEntity() {
		return entity;
	}
}
