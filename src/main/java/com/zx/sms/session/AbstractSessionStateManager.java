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
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.UniqueLongMsgId;
import com.zx.sms.common.GlobalConstance;
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
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;

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
	public AbstractSessionStateManager(EndpointEntity entity, ConcurrentMap<String, VersionObject<T>> storeMap, boolean preSend) {
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

	private final long version = CachedMillisecondClock.INS.now();

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
	private final ConcurrentMap<String, VersionObject<T>> storeMap;
	
	private ChannelHandlerContext ctx;
	private String channelId ;

	/**
	 * 会话刚建立时要发送的数据
	 */
	private boolean preSend;
	
	private long minDelay;

	private boolean preSendover = false;
	
	private ScheduledFuture logFuture;
	
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
		this.channelId = ctx.channel().id().asShortText();
	}
	
    private void setMessageDelayWritability(ChannelHandlerContext ctx, boolean writable) {
        ChannelOutboundBuffer cob = ctx.channel().unsafe().outboundBuffer();
        if (cob != null) {
            cob.setUserDefinedWritability(GlobalConstance.MESSAGE_DELAY_USER_DEFINED_WRITABILITY_INDEX, writable);
        }
    }
    
    private void incrementSendWindow(ChannelHandlerContext ctx) {
    	AtomicInteger windowSize = ctx.channel().attr(GlobalConstance.SENDWINDOWKEY).get();
    	if(windowSize !=null) {
    		int s = windowSize.incrementAndGet();
    	}
    }
    
	public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
		final EndpointConnector conn = entity.getSingletonConnector();
		if (conn != null) {
			Channel ch = ctx.channel();
			conn.removeChannel(ch);
		}
			
		logger.warn("Connection closed . {} , connect count : {}", entity, conn == null ? 0 : conn.getConnectionNum());
		
		ctx.executor().execute(new Runnable() {

			@Override
			public void run() {
				// 取消重试队列里的任务
				for (Iterator<Map.Entry<K, Entry>> itor = msgRetryMap.entrySet().iterator();itor.hasNext(); ) {
					Map.Entry<K, Entry> entry = itor.next();
					if(entry == null) continue;
					K seq = entry.getKey();
					Entry failedentry = entry.getValue();
					final T requestmsg = failedentry.request;
					boolean async = !failedentry.sync;
					// 所有连接都已关闭
					if (conn != null && conn.getConnectionNum() > 0){
						if (entity.isReSendFailMsg() && async) {
							// 连接断连，但是未收到Resp的消息，异步发送时。通过其它连接再发送一次
							Channel ch = conn.fetch();
							AbstractSessionStateManager session = ch.attr(GlobalConstance.sessionKey).get();
							ChannelHandlerContext otherCtx = session.getCtx();
							ChannelPromise f = otherCtx.newPromise();
							try {
								session.write(otherCtx, requestmsg, f);
								f.addListener(new GenericFutureListener() {
									@Override
									public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
										if(!future.isSuccess()) {
											errlogger.warn("send requestMsg {} from other channel  which is active failed.",requestmsg,future.cause());
										}
									}
								});
								storeMap.remove(channelSequenceKey(seq, channelId));
								
								logger.warn("current channel {} is closed.send requestMsg {} from other channel {} which is active.", ctx.channel(),requestmsg,otherCtx.channel());

							} catch (Exception e) {
								errlogger.error("send requestMsg {} from other channel {} which is active ", requestmsg,otherCtx.channel(),e);
							}
						} else {
							errlogger.error("Channel closed . Msg {} may not send Success. ", requestmsg);
						}

					}
					cancelRetry(failedentry, ctx.channel());
					responseFutureDone(failedentry, new IOException("channel closed."));
					itor.remove();
				}

				// 如果重发的消息从第一个连接发送，仍没有发送完毕时断连,。从其它连接发送
				if (preSend && (!preSendover)) {
					for (Iterator<Map.Entry<String, VersionObject<T>>>  itor = storeMap.entrySet().iterator();itor.hasNext();) {
						Map.Entry<String, VersionObject<T>> storeentry = itor.next();
						// 所有连接都已关闭
						if (conn == null)
							break;

						Channel ch = conn.fetch();

						if (ch != null && ch.isActive()) {
							String key = storeentry.getKey();

							VersionObject<T> vobj = storeentry.getValue();
							long v = vobj.getVersion();
							T msg = vobj.getObj();
							
							//只发送在本次连接建立之前的未成功的消息
							//v保存了消息创建时的时间
							if (version > v && msg != null) {
								// 如果配置了失败重发
								logger.warn("channelInactive Send last failed msg . {}", msg);
								ch.write(msg);
								itor.remove();
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
				VersionObject<T> vobj = storeMap.remove(channelSequenceKey(key, this.channelId));
				if (vobj != null) {
					final T request = vobj.getObj();
					long sendtime = vobj.getVersion();
					
					// 把response关联上request供使用。
					response.setRequest(request);
					
					//响应延迟过大
					long delay = delaycheck(sendtime);
					//计算最小时延
					minDelay =  Math.min(minDelay, delay);
					
					//响应延迟超过25%的超时时间,打印告警，并暂停一会
					if(delay > (entity.getRetryWaitTimeSec() * 1000/4)){
						errlogger.warn("Entity {} receive response time delay . delayTime :{} , Request :{}", entity.getId(),delay,request);
						//接收response回复时延太高，有可能对端已经开始积压了，暂停发送。
						setchannelunwritableWhenDelay(ctx,delay-minDelay);
					}
					
					Entry entry = msgRetryMap.get(key);
					
					//取消息重发任务,再次发送时重新注册任务
					cancelRetry(entry, ctx.channel());
					
					//增加发送窗口
					incrementSendWindow(ctx);
					
					// 根据Response 判断是否需要重发,比如CMPP协议，如果收到result==8，表示超速，需要重新发送
					//Sgip 及 Smpp协议收到88（超速）要重发
					//如果一条消息一直超速，限制重发次数

					if (needSendAgainByResponse(request, response) && entry.overSpeedSendCnt.get() < entity.getOverSpeedSendCountLimit()) {
			            if(logFuture == null || logFuture.isDone()) {
			            	//1秒打印一次
			            	logFuture = ctx.executor().schedule(new Runnable() {
			                    @Override
			                    public void run() {
			                    	//超速错误可能非常多，1秒打印一次信息
			                    	errlogger.warn("Entity {} Receive Speed Error Response . Resp:{},Req: {} " ,entity.getId(),response,request);
			                    }
			                },1000,TimeUnit.MILLISECONDS);
			            }
						//网关异常时会发送大量超速错误(result=8),造成大量重发，浪费资源。这里先停止发送，过40毫秒再回恢复
						setchannelunwritableWhenDelay(ctx,delay);
						//延迟后重发
						reWriteLater(ctx, entry.request, ctx.newPromise(), delay);
						
					}else{
						//给同步发送的promise响应resonse
						responseFutureDone(entry, response);
						msgRetryMap.remove(key);
					}
				} else {
					errlogger.warn("Entity {} receive ResponseMessage ,but not found related Request Msg. response:{}", entity.getId(),response);
				}
			}
		}
		ctx.fireChannelRead(msg);
	}

	//查检发送req与收到res的时间差
	private long delaycheck(long sendtime){
		return System.currentTimeMillis() - sendtime ;
	}
	
	private void setchannelunwritableWhenDelay(final ChannelHandlerContext ctx,long millitime){
		if(ctx.channel().isWritable()){
			setMessageDelayWritability(ctx, false);
			
			ctx.executor().schedule(new Runnable() {
				@Override
				public void run() {
					setMessageDelayWritability(ctx, true);
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
					try {
						preSendMsg(ctx);
					}catch(Exception ex) {
						logger.error("preSendMsg error ",ex);
					}
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
						
						//因为滑动窗口在 WindowSizeChannelTrafficShapingHandler 里没有处理response超时的情况，因此在这里要增加窗口
						incrementSendWindow(ctx);
						
						int times = entry.cnt.get();
						
						if (times >= entity.getMaxRetryCnt()) {

							// 会有future泄漏的情况发生，这里cancel掉自己，来规避泄漏
							Future future = ref.get();
							if (future != null)
								future.cancel(false);

							cancelRetry(entry,ctx.channel());
							
							responseFutureDone(entry,new SendFailException("retry send msg over "+times+" times"));
							msgRetryMap.remove(seq);
							// 删除消息
							storeMap.remove(channelSequenceKey(seq, channelId));
							// 重试发送都失败的消息要记录
							if(closeWhenRetryFailed(message)) {
								logger.error("entity : {} , retry send {} times ,the connection may die.close it .\nMessage {} ", entity.getId(),times,message);
								ctx.close();
							}else {
								logger.error("entity : {} , retry send {} times ,keep connection alive. \nMessage {} ", entity.getId(),times,message);
								//不关闭通道的，设置连接不可写，1s后恢复
								setchannelunwritableWhenDelay(ctx,1000);
							}
						} else {
							logger.warn("entity : {} , retry send Msg : {}", entity.getId(),message);
							msgWriteCount++;
							entry.cnt.incrementAndGet();
							ChannelPromise retryPromise =  ctx.newPromise();
							ctx.writeAndFlush(message,retryPromise);
							retryPromise.addListener(new ChannelFutureListener() {
								@Override
								public void operationComplete(ChannelFuture future) throws Exception {
									if (future.isSuccess()) {
										//超时重发这里再保存一次，是为了更新VersionObject里的version时间
										//方便后边判断响应延迟时间
										storeMap.put(channelSequenceKey(seq, channelId), new VersionObject<T>(message));
									}
								}
							});
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
			logger.warn("receive message {} not exists in msgRetryMap,maybe response received before create retrytask .", message);
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
		
		//本通道账号的第一个连接，要发送之前失败的消息
		if (preSend) {
			for (Iterator<Map.Entry<String, VersionObject<T>>>  itor = storeMap.entrySet().iterator();itor.hasNext();) {
				Map.Entry<String, VersionObject<T>> entry =  itor.next();
				if (!ctx.channel().isActive()) {
					isbreak = true;
					break;
				}

				String key = entry.getKey();

				VersionObject<T> vobj = entry.getValue();
				long v = vobj.getVersion();
				T msg = vobj.getObj();

				//只发送在本次连接建立之前的未成功的消息
				//v保存了消息创建时的时间
				if (version > v && msg != null) {
					logger.warn("preSendMsg Send last failed msg .{} : {}", key ,msg);
					itor.remove();
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
				errlogger.error("Entity {} .Msg Life over .{}", entity.getId(),message);
				promise.tryFailure(new SmsLifeTerminateException("Msg Life over"));
				
				DefaultPromise failed = new DefaultPromise<T>(ctx.executor());
				failed.tryFailure(new SmsLifeTerminateException("Msg Life over"));
				return failed;
			}
			
			final K seq = getSequenceId(message);
			// 记录已发送的请求,在发送msg前生记录到map里。防止生成retryTask前就收到resp的情况发生
			Entry tmpentry ;
			Entry old = msgRetryMap.get(seq);
			if (old != null) {
				tmpentry = old;
				//2018-08-27 当网关返回超速错时，也会存在想同的seq
				//消息内容不同，可能是相同的seq，但不同的短信				
				if(!message.equals(old.request)){
					// bugfix: 集群环境下可能产生相同的seq. 已经存在一个相同的seq.
					logger.error("has repeat Sequense {},\nold:{}\nnew:{}", seq,old.request,message);
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
				}else {
					//消息内容相同表示此消息是因为超速错导致的重发,增加超速计数
					tmpentry.overSpeedSendCnt.incrementAndGet();
				}
			} else{
				tmpentry = new Entry(message,syn);
				//收到响应时将此对象设置为完成状态
				tmpentry.resfuture = new DefaultPromise<T>(ctx.executor());
				msgRetryMap.put(seq, tmpentry);
			}
			
			msgWriteCount++;
			// 持久化到队列
			storeMap.put(channelSequenceKey(seq,this.channelId), new VersionObject<T>(message));

			promise.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {

					if (future.isSuccess()) {
						
						//这里重新保存一次，是为了更新VersionObject里的version时间为消息实际发出去的时间
						storeMap.put(channelSequenceKey(seq,channelId), new VersionObject<T>(message));
						// 注册重试任务
						scheduleRetryMsg(ctx, message);
						
					}else {
						//发送失败,必须清除msgRetryMap里的对象，否则上层业务
						//可能提交相同seq的消息，造成死循环
						logger.error("remove fail message {}", message,future.cause());
						
						incrementSendWindow(ctx);
						
						storeMap.remove(channelSequenceKey(seq, channelId));
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
		AtomicInteger overSpeedSendCnt = new AtomicInteger(0);
		T request;
		boolean sync =  false;
		
		DefaultPromise<T> resfuture ;
		Entry(T request,boolean sync) {
			this.request = request;
			this.sync =  sync;
		}
	}
	
	public Promise<T> writeMessagesync(T message){
		
		//设置长短信 UniqueLongMsgId的ChannelId remoteAddr属性
		if (message instanceof LongSMSMessage && ((LongSMSMessage)message).getUniqueLongMsgId()!=null && ((LongSMSMessage)message).getUniqueLongMsgId().getChannelId() == null) {
			LongSMSMessage lmsg = (LongSMSMessage)message;
			lmsg.setUniqueLongMsgId(new UniqueLongMsgId(lmsg.getUniqueLongMsgId(), ctx.channel()));
		}
		
		return safewrite(ctx,message,ctx.newPromise(),true);
	}
	
	private String channelSequenceKey(K k ,String channelId) {
		return channelId+k.toString();
	}

	public EndpointEntity getEntity() {
		return entity;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	
}
