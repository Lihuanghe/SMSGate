package com.zx.sms.session.cmpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

/**
 * @author Lihuanghe(18852780@qq.com) 消息发送窗口拜你控制和消息重发 ，消息持久化
 */
public class SessionStateManager extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);
	//用来记录连接上的错误消息
	private final Logger errlogger ;
	/**
	 *@param entity
	 *Session关联的端口
	 *@param storeMap
	 *存储该端口上的持久化消息，存储不会很大，收到消息resp后就会删除持久化消息。
	 *@param preSend
	 *预发送数据，连接建立后要发送的数据
	 */
	public SessionStateManager(CMPPEndpointEntity entity , Map<Long, Message> storeMap,Map<Long, Message> preSend) {
		this.entity = entity;
		windowSize = entity.getWindows();
		if (windowSize == 0) {
			windows = null;
		} else {
			windows = new Semaphore(windowSize);
		}
		errlogger = LoggerFactory.getLogger(entity.getId());
		this.storeMap = storeMap ;
		this.preSend = preSend;
	}

	/**
	 * 连接流量统计
	 **/
	private long msgReadCount = 0;
	private long msgWriteCount = 0;
	private CMPPEndpointEntity entity;

	/**
	 * 消息窗口，默认16
	 **/
	private final int windowSize;
	private Semaphore windows;
	/**
	 * 重发队列
	 **/
	private final ConcurrentHashMap<Long, Entry> msgRetryMap = new ConcurrentHashMap<Long, Entry>();
	/**
	 * 发送未收到resp的消息，需要使用可持久化的Map.
	 */
	private final Map<Long, Message> storeMap;
	
	/**
	 * 会话刚建立时要发送的数据
	 */
	private Map<Long, Message> preSend;
	/**
	 * 异步等待发送窗口的队列
	 */
	private ConcurrentLinkedQueue<Runnable> waitWindowQueue = new ConcurrentLinkedQueue<Runnable>();


	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.warn("Connection closed. channel:{}",ctx.channel());
		// 取消重试队列里的任务
		for (Map.Entry<Long, Entry> entry : msgRetryMap.entrySet()) {
			final Long key = entry.getKey();
			Entry en = cancelRetry(key,ctx.channel());
			
			EndpointConnector conn = CMPPEndpointManager.INS.getEndpointConnector(entity);
			//所有连接都已关闭
			if(conn == null) break;
			
			Channel ch  = conn.fetch();
			if(ch!=null){
				ch.write(en.request);
				logger.debug("current channel {} is closed.send requestMsg from other channel {} which is active.",ctx.channel(),ch);
			}
		}
		// 释放发送窗口
		if (windowSize != 0) {
			int avab = windows.availablePermits();
			if (avab < windowSize) {
				try{
					windows.release(windowSize - avab);
				}catch(Exception ex){
					logger.warn("window release failed .",ex);
				}
			}
		}
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		msgReadCount++;
		if (msg instanceof Message) {
			final Message message = (Message) msg;
			//设置消息的生命周期
			message.setLifeTime(entity.getLiftTime());
			
			// 如果是resp，取消息消息重发
			if (!isRequestMsg(message)) {
				// 删除发送成功的消息
				// 要先删除成功的消息，然后再开一个发送窗口。

				storeMap.remove(message.getHeader().getSequenceId());
				cancelRetry(message.getHeader().getSequenceId(),ctx.channel());
			}
		}
		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

		if (msg instanceof Message) {
			
			//发送消息超过生命周期
			if(((Message) msg).isTerminationLife()){
				errlogger.error("Msg Life over .{}" ,msg);
				promise.setFailure(new RuntimeException("Msg Life over"));
				return ;
			}
			
			if (isRequestMsg((Message) msg)) {
				// 发送，未收到Response时，60秒后重试,
				writeWithWindow(ctx, (Message) msg, promise);
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

	/**
	 * 获取发送窗口，并且注册重试任务
	 **/
	private boolean writeWithWindow(final ChannelHandlerContext ctx, final Message message, final ChannelPromise promise) {
		boolean acquired = false;
		try {
			// 获取发送窗口
			acquired = (windows == null ? true : windows.tryAcquire(0, TimeUnit.SECONDS));
			// 防止一个连接死掉，把服务挂死，这里要处理窗口不够用的情况
			if (acquired) {
				safewrite(ctx, message, promise);
			} else {
				// 加入等待队列
				waitWindowQueue.offer(new Runnable() {
					@Override
					public void run() {
						safewrite(ctx, message, promise);
					}
				});
			}

		} catch (InterruptedException e) {
			logger.error("windows acquire interrupted: ", e.getCause() != null ? e.getCause() : e);
		}
		return acquired;
	}

	private void scheduleRetryMsg(final ChannelHandlerContext ctx, final Message message, final ChannelPromise promise) {

		long seq = message.getHeader().getSequenceId();

		Entry entry = msgRetryMap.get(seq);

		//发送次数大于1时要重发
		if (entry != null && entity.getMaxRetryCnt()>1) {
			

			Future<?> future = EventLoopGroupFactory.INS.getMsgResend().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					try {
						int times = message.incrementAndGetRequests();
						logger.debug("retry Send Msg : {}" ,message);
						if (times > entity.getMaxRetryCnt()) {

							cancelRetry(message.getHeader().getSequenceId(),ctx.channel());

							// 删除发送成功的消息
							storeMap.remove(message.getHeader().getSequenceId());
							// TODO 发送3次都失败的消息要记录
							logger.error("retry send msg {} times。cancel retry task",times);
							
							errlogger.error("RetryFailed: {}",message);
							
							if (message instanceof CmppActiveTestRequestMessage) {
								ctx.close();
								logger.error("retry send CmppActiveTestRequestMessage 3 times,the connection may die.close it");
							}
						} else {

							msgWriteCount++;
							// 重发不用申请窗口
							ctx.writeAndFlush(message, ctx.newPromise());
						}
					} catch (Throwable e) {
						logger.error("retry send Msg Error.", e);
					}
				}
			}, entity.getRetryWaitTimeSec(), entity.getRetryWaitTimeSec(), TimeUnit.SECONDS);

			entry.future = future;
			
			//这里增加一次判断，是否已收到resp消息,已到resp后，msgRetryMap里的entry会被 remove掉。
			if(msgRetryMap.get(seq)==null){
				future.cancel(true);
			}
			
		} else if(entry == null){
			//当程序执行到这里时，可能已收到resp消息，此时entry为空。
			logger.warn("receive seq {} not exists in msgRetryMap,maybe response received before create retrytask .", seq);
		}

	}

	private Entry cancelRetry(Long seq ,Channel channel) {
		Entry entry = msgRetryMap.remove(seq);

		if (entry != null && entry.future != null) {
			entry.future.cancel(true);
		}
		if (windows != null) {
			// 如果等窗口的队列里有任务，先发送等待的消息
			if (channel!=null && channel.isActive() && !waitWindowQueue.isEmpty()) {

				Runnable task = waitWindowQueue.poll();
				if (task != null) {
					EventLoopGroupFactory.INS.getWaitWindow().submit(task);
				}
			} else {
				windows.release();
			}
		}

		return entry;
	}

	private boolean isRequestMsg(Message msg) {
		long commandId = msg.getHeader().getCommandId();
		return (commandId & 0x80000000L) == 0L;
	}


	private void preSendMsg(ChannelHandlerContext ctx) {
		if(preSend!=null && preSend.size()>0) {
			for (Map.Entry<Long, Message> entry : preSend.entrySet()) {
				Long key = entry.getKey();
				Message msg = entry.getValue();
				
				if(msg.isTerminationLife()){
					errlogger.error("Msg Life is Over. {}" ,msg);
					continue;
				}
				if (msg != null) {
					logger.debug("Send last failed msg . {}", msg);
					storeMap.remove(key);
					writeWithWindow(ctx,msg,ctx.newPromise());
				}
			}
			//让GC回收内存
			preSend.clear();
			preSend = null;
		}
	}

	/**
	 * 发送msg,首先做消息持久化
	 */
	private void safewrite(ChannelHandlerContext ctx, final Message message, ChannelPromise promise) {
		if (ctx.channel().isActive()) {
			final Long seq = message.getHeader().getSequenceId();

			message.incrementAndGetRequests();
			msgWriteCount++;
			// 记录已发送的请求,在发送msg前生记录到map里。防止生成retryTask前就收到resp的情况发生
			msgRetryMap.put(seq, new Entry(message));
			// 持久化到队列
			storeMap.put(seq, message);
			
			ctx.write(message, promise);

			// 注册重试任务
			scheduleRetryMsg(ctx, message, promise);
			ctx.flush();
		} else {
			// 如果连接已关闭，通知上层应用
			if(promise!=null && (!promise.isDone()))promise.setFailure(new ClosedChannelException());
		}
	}

	private class Entry {
		// 保证future的可见性，
		volatile Future future;
		Message request;

		Entry(Message request) {
			this.request = request;
		}
	}

}
