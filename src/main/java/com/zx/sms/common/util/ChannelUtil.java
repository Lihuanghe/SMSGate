package com.zx.sms.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;

public class ChannelUtil {

	private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);
	/**
	 * 同步发送消息到端口，发送完成才返回。
	 * 
	 * 方法会阻塞线程，直到消息发送完成
	 */
	public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg) {
		int i = 5;
		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		while (connector != null && i-- > 0) {
			Channel ch = connector.fetch();
			// 端口上还没有可用连接
			if (ch == null)
				break;

			if (ch.isActive()) {

				ChannelFuture future = ch.writeAndFlush(msg);

				future.addListener(new GenericFutureListener<ChannelFuture>() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						// 如果发送消息失败，记录失败日志
						if (!future.isSuccess()) {
							StringBuilder sb = new StringBuilder();
							sb.append("SendMessage ").append(msg.toString()).append(" Requet Failed To ").append(entity.toString());
							logger.error(sb.toString(), future.cause());
						}
					}
				});
				return future;
			}
		}
		return null;
	}
}
