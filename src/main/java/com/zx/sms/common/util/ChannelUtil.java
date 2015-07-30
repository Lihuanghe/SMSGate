package com.zx.sms.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;

public class ChannelUtil {
	private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);
	/**
	 * 同步发送消息，发送完成才返回。
	 * 方法会阻塞线程，直到消息发送完成
	 */
	public static Future syncWriteToChannel(Channel ch, Object msg) throws ClosedChannelException,InterruptedException {

		// 通过其它连接发送
		ChannelPromise promise = ch.newPromise();
		ch.writeAndFlush(msg, promise);
		// 阻塞，等 netty发送完成
		
		promise.sync();
		return promise;
	}

	/**
	 * 同步发送消息到端口，发送完成才返回。
	 * 
	 * 方法会阻塞线程，直到消息发送完成
	 */
	public static Future syncWriteToEntity(EndpointEntity entity, Object msg) throws ClosedChannelException,InterruptedException {
		Future promise = null;
		int i =  5;
		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		while (connector !=null && i-- > 0) {
			Channel ch = connector.fetch();
			//端口上还没有可用连接
			if(ch == null){
				break;
			}
			
			if (ch.isActive()) {
				promise = syncWriteToChannel(ch, msg);
				if (promise.isSuccess()) {
					break;
				}
				logger.warn("shoud never here. {}" ,msg);
			}
		}
		return promise;
	}
}
