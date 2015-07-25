package com.zx.sms.handler.api.trans;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

/**
 * 
 * @author Lihuanghe(18852780@qq.com)
 *
 */
@Component
@Sharable
public class MessageTransferHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(MessageTransferHandler.class);

	private Future future;

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt == SessionState.Connect) {
			final ChannelHandlerContext finalCtx = ctx;
			final CMPPEndpointEntity finalentity = (CMPPEndpointEntity)getEndpointEntity();
			future = EventLoopGroupFactory.INS.getBusiWork().submit(new Runnable() {

				@Override
				public void run() {

					BlockingQueue<Message> queue = BDBStoredMapFactoryImpl.INS.getQueue(finalentity.getId(),finalentity.getId());
					while (true) {
						Message msg;
						try {
							msg = queue.take();
							Future promise = ChannelUtil.syncWriteToChannel(finalCtx.channel(), msg);
							if (!promise.isSuccess()) {
								logger.error("发送失败!!", promise.cause());
							}
						} catch (Exception e) {
							logger.error("发送失败!!", e);
							break;
						}
					}
					finalCtx.close();
				}
			});
		}
		ctx.fireUserEventTriggered(evt);
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (future != null)
			future.cancel(true);
		ctx.fireChannelInactive();
	}

	@Override
	public String name() {
		
		return "MessageTransferHandler-Trans";
	}

}
