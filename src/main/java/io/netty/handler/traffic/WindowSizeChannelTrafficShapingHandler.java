package io.netty.handler.traffic;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.ScheduledFuture;


/**
 *  这个类是为了在netty原有流量整形（ChannelTrafficShapingHandler）的基础上，扩展增加滑动窗口的发送控制能力
 *  因此，主体代码跟官方保持一致，只修改 sendAllValid 方法，增加判断当前channel窗口是否大于0。
 *  在本class里发送的消息后对窗口进行扣减 。在 sessionManager里 收到response或者response超时后对窗口进行增加
 */
public class WindowSizeChannelTrafficShapingHandler extends AbstractTrafficShapingHandler {
    private static final Logger logger = LoggerFactory.getLogger(WindowSizeChannelTrafficShapingHandler.class);
    private final ArrayDeque<ToSend> messagesQueue = new ArrayDeque<ToSend>();
    private long queueSize;
    private EndpointEntity entity;
    
    //默认是启用滑动窗口
    private boolean useWindow = true;

    /**
     * Create a new instance using default
     * max time as delay allowed value of 15000 ms.
     *
     * @param entity
     *         EntityPoint
     * @param checkInterval
     *          The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed.
     */
    public WindowSizeChannelTrafficShapingHandler(EndpointEntity entity, long checkInterval) {
        //限速参数不能小于0
        super(entity.getWriteLimit() , entity.getReadLimit() , checkInterval);
       
        // 一个连接积压条数超过每秒速度的200% ,或者不限速时超过500。就不能再写了
        setMaxWriteSize( (entity.getWriteLimit() > 0 ?  entity.getWriteLimit() : 250) * 2);
        // 一个连接 积压延迟超过1000ms 就不能再写了
        setMaxWriteDelay(1000);
        this.entity = entity;
        
        //如果窗口小于1，表示不启动滑动窗口
        this.useWindow = entity.getWindow() >= 1;
    }
    
    private ScheduledFuture sf;
    
    private Future readFuture;
    private Future submitFuture;
    private ScheduledFuture logFuture;

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
    	
		//在连接上创建发送窗口记数器，该记数器在下边SessionManagerHanlder 和 WindowSizeChannelTrafficShapingHandler
		//中用来统计发送的request个数和接收到的response个数
    	ctx.channel().attr(GlobalConstance.SENDWINDOWKEY).set(new AtomicInteger(this.entity.getWindow()));
    	
    	TrafficCounter trafficCounter = new TrafficCounter(this, ctx.executor(), "ChannelTC" +
                ctx.channel().hashCode(), checkInterval) ;
        setTrafficCounter(trafficCounter);
        trafficCounter.start();   
        
        //如果messagesQueue队列里有积压的未发送消息，但此时连接上即没有发送消息，也没有接收消息。
        //此时要定时1.5秒发送下队列里的消息
        sf = ctx.executor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendAllValid(ctx, TrafficCounter.milliSecondFromNano());
            }
        }, 3, 1500, TimeUnit.MILLISECONDS);
        
        
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    	//连接关闭时移除该对象
    	ctx.channel().attr(GlobalConstance.SENDWINDOWKEY).set(null);
    	
        trafficCounter.stop();
        // write order control
        synchronized (this) {
            if (ctx.channel().isActive()) {
                for (ToSend toSend : messagesQueue) {
                    long size = calculateSize(toSend.toSend);
                    trafficCounter.bytesRealWriteFlowControl(size);
                    queueSize -= size;
                    ctx.write(toSend.toSend, toSend.promise);
                }
            } else {
                for (ToSend toSend : messagesQueue) {
                    if (toSend.toSend instanceof ByteBuf) {
                        ((ByteBuf) toSend.toSend).release();
                    }
                    
                    //连接关闭，设置future失败，避免上层业务死等
                    toSend.promise.tryFailure( new IOException("channel InActive.failed by WindowSizeChannelTrafficShapingHandler."));
                }
            }
            messagesQueue.clear();
        }
        releaseWriteSuspended(ctx);
        releaseReadSuspended(ctx);
        
        //连接关闭时，删除定时任务
        if(sf!=null) {
            if(!sf.isCancelled())
                sf.cancel(false);
        }
        
        super.handlerRemoved(ctx);
    }

    private static final class ToSend {
        final long relativeTimeAction;
        final Object toSend;
        final ChannelPromise promise;

        private ToSend(final long delay, final Object toSend, final ChannelPromise promise) {
            relativeTimeAction = delay;
            this.toSend = toSend;
            this.promise = promise;
        }
    }

    @Override
    void submitWrite(final ChannelHandlerContext ctx, final Object msg,
            final long size, final long delay, final long now,
            final ChannelPromise promise) {
        final ToSend newToSend;
        
        //response直接发送，不经过发送窗口
        if (msg instanceof BaseMessage) {
            BaseMessage req = (BaseMessage) msg;
            if (!req.isRequest()) {
            	ctx.write(msg, promise);
            	return;
            }
        } 
        
        // write order control
        synchronized (this) {
            
            //这里增加判断是否可写
            if (delay == 0 && allowSendMsg(ctx)  && messagesQueue.isEmpty()) {
                trafficCounter.bytesRealWriteFlowControl(size);
                writeAndDecrement(ctx,msg,promise);
                return;
            }
            newToSend = new ToSend(delay + now, msg, promise);
            messagesQueue.addLast(newToSend);
            queueSize += size;
            checkWriteSuspend(ctx, delay, queueSize);
        }
        final long futureNow = newToSend.relativeTimeAction;
        //如果延迟大于10ms，才启动定时任务
        if(delay > 10) {
            ctx.executor().schedule(new Runnable() {
                @Override
                public void run() {
                    sendAllValid(ctx, futureNow);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }else {
        	//这里是为了减少重复调用sendAllValid的次数
            //通过future判断是否重复启动了多次sendAllValid 
            if(submitFuture == null || submitFuture.isDone()) {
                submitFuture = ctx.executor().submit(new Runnable() {
                    @Override
                    public void run() {
                        sendAllValid(ctx, futureNow);
                    }
                });
            }
        }

    }
    
    private void sendAllValid(final ChannelHandlerContext ctx, final long now) {
        // write order control
        synchronized (this) {
            
            ToSend newToSend = messagesQueue.pollFirst();
            for (; newToSend != null; newToSend = messagesQueue.pollFirst()) {
                
                //判断是否可以真实发送消息
                if (newToSend.relativeTimeAction <= now && allowSendMsg(ctx)) {
                    long size = calculateSize(newToSend.toSend);
                    trafficCounter.bytesRealWriteFlowControl(size);
                    queueSize -= size;
                    writeAndDecrement(ctx,newToSend.toSend, newToSend.promise);
                } else {
                    messagesQueue.addFirst(newToSend);
                    break;
                }
            }
            
            if (messagesQueue.isEmpty()) {
                releaseWriteSuspended(ctx);
            }
        }
        //积压消息过大，打印告警日志
        if(queueSize > getMaxWriteSize()*2) {
        	final long t_size = queueSize;
        	final long time = CachedMillisecondClock.INS.now();
            if(logFuture == null || logFuture.isDone()) {
            	//1秒打印一次
            	logFuture = ctx.executor().schedule(new Runnable() {
                    @Override
                    public void run() {
                    	logger.warn("time : {} ,ch: {}-{} ,messagesQueue contain message more than : {}" ,
                    			DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(time),
                    			entity.getId(),
                    			ctx.channel().id(),
                    			t_size);
                    }
                },1000,TimeUnit.MILLISECONDS);
            }
        }
        	
        ctx.flush();
    }

    /**
    * @return current size in bytes of the write buffer.
    */
   public long queueSize() {
       return queueSize;
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
    
    private long doCalculateSize(Object msg) {
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
    
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof BaseMessage) {
            BaseMessage req = (BaseMessage) msg;
            
            //收到response后
            if (req.isResponse()) {
                
                //增加发送窗口的动作放在SessionStateManager里处理
                //方便处理response超时的情况
                
                //立即发送积压消息
                //通过future判断是否重复启动了多次sendAllValid 
                if(readFuture == null || readFuture.isDone()) {
                	//这里避免多次调用sendAllValid
                    readFuture = ctx.executor().submit(new Runnable() {
                        @Override
                        public void run() {
                            sendAllValid(ctx, TrafficCounter.milliSecondFromNano());
                        }
                    });
                }
            }
        } 
        super.channelRead(ctx, msg);
    }
      
      private void writeAndDecrement(ChannelHandlerContext ctx,Object msg, ChannelPromise promise) {
          
          ctx.write(msg, promise);
          
          if (msg instanceof BaseMessage) {
                BaseMessage req = (BaseMessage) msg;
                
                //request消息，要减少发送窗口
                if (req.isRequest()) {
                    decrementSendWindow(ctx);
                }
            } 
      }
      
      private void decrementSendWindow(ChannelHandlerContext ctx) {
          AtomicInteger ati = ctx.channel().attr(GlobalConstance.SENDWINDOWKEY).get();
          if(ati != null)
              ati.decrementAndGet();
      }
      
      private int getSendWindow(ChannelHandlerContext ctx) {
          AtomicInteger ati = ctx.channel().attr(GlobalConstance.SENDWINDOWKEY).get();
          if(ati != null)
              return ati.get();
          
          return -1;
      }  
      
      private boolean allowSendMsg(ChannelHandlerContext ctx) {
    	  return (!useWindow) || getSendWindow(ctx) > 0;
      }
}
