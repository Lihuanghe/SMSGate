package io.netty.handler.traffic;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.ScheduledFuture;


/**
 *  这个类是为了在netty原有流量整形（ChannelTrafficShapingHandler）的基础上，扩展增加滑动窗口的发送控制能力
 *  因此，主体代码跟官方保持一致，只修改 sendAllValid 方法，增加判断当前channel窗口是否大于0。
 */
public class WindowSizeChannelTrafficShapingHandler extends AbstractTrafficShapingHandler {
	private static final Logger logger = LoggerFactory.getLogger(WindowSizeChannelTrafficShapingHandler.class);
    private final ArrayDeque<ToSend> messagesQueue = new ArrayDeque<ToSend>();
    private long queueSize;

    /**
     * Create a new instance using default
     * max time as delay allowed value of 15000 ms.
     *
     * @param writeLimit
     *          0 or a limit in bytes/s
     * @param readLimit
     *          0 or a limit in bytes/s
     * @param checkInterval
     *          The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed.
     */
    public WindowSizeChannelTrafficShapingHandler(EndpointEntity entity, long checkInterval) {
    	//限速参数不能小于0
        super(entity.getWriteLimit() >0 ?  entity.getWriteLimit() : 9999, entity.getReadLimit() > 0 ? entity.getReadLimit()  : 9999, checkInterval);
		// 一个连接 积压条数超过每秒速度的60% 就不能再写了
		setMaxWriteSize( entity.getWriteLimit() * 3 / 5);
		// 一个连接 积压延迟超过600ms 就不能再写了
		setMaxWriteDelay(1000 * 3 / 5);
    }
    
    private ScheduledFuture sf;

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        TrafficCounter trafficCounter = new TrafficCounter(this, ctx.executor(), "ChannelTC" +
                ctx.channel().hashCode(), checkInterval);
        setTrafficCounter(trafficCounter);
        trafficCounter.start();    
        sf =  ctx.executor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendAllValid(ctx, TrafficCounter.milliSecondFromNano());
            }
        }, 3, 1, TimeUnit.SECONDS);
        
        
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
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
        // write order control
        synchronized (this) {
        	
        	//这里增加判断是否可写
            if (delay == 0 && ctx.channel().isWritable() && messagesQueue.isEmpty()) {
                trafficCounter.bytesRealWriteFlowControl(size);
                ctx.write(msg, promise);
                return;
            }
            newToSend = new ToSend(delay + now, msg, promise);
            messagesQueue.addLast(newToSend);
            queueSize += size;
            checkWriteSuspend(ctx, delay, queueSize);
        }
        final long futureNow = newToSend.relativeTimeAction;
        ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                sendAllValid(ctx, futureNow);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
    
    /**
     *判断滑动窗口可写标志是否为true; 即窗口是否大于0 
     */
    private boolean isWindowPositive(ChannelHandlerContext ctx) {
   		ChannelOutboundBuffer cob = ctx.channel().unsafe().outboundBuffer();
   		return cob.getUserDefinedWritability(GlobalConstance.WINDOW_SIZE_ZERO_USER_DEFINED_WRITABILITY_INDEX);
    }

    private void sendAllValid(final ChannelHandlerContext ctx, final long now) {
        // write order control
        synchronized (this) {
        	
            ToSend newToSend = messagesQueue.pollFirst();
            for (; newToSend != null; newToSend = messagesQueue.pollFirst()) {
            	
            	//判断发送窗口是否大于0
                if (newToSend.relativeTimeAction <= now && isWindowPositive(ctx)) {
                    long size = calculateSize(newToSend.toSend);
                    trafficCounter.bytesRealWriteFlowControl(size);
                    queueSize -= size;
                    ctx.write(newToSend.toSend, newToSend.promise);
                } else {
                    messagesQueue.addFirst(newToSend);
                    break;
                }
            }
            if (messagesQueue.isEmpty()) {
                releaseWriteSuspended(ctx);
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
	
}
