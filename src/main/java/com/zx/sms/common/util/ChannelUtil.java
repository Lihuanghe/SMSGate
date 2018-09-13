package com.zx.sms.common.util;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.ArrayList;
import java.util.List;

import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;

public class ChannelUtil {

	private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);

	public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg) {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		return asyncWriteToEntity(connector, msg, null);
	}

	public static ChannelFuture asyncWriteToEntity(final String entity, final Object msg) {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		return asyncWriteToEntity(connector, msg, null);
	}

	public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg, GenericFutureListener listner) {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		return asyncWriteToEntity(connector, msg, listner);
	}

	public static ChannelFuture asyncWriteToEntity(final String entity, final Object msg, GenericFutureListener listner) {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		return asyncWriteToEntity(connector, msg, listner);
	}

	private static ChannelFuture asyncWriteToEntity(EndpointConnector connector, final Object msg, GenericFutureListener listner) {
		if (connector == null || msg == null)
			return null;

		ChannelFuture promise = connector.asynwrite(msg);

		if (promise == null)
			return null;

		if (listner == null) {
			promise.addListener(new GenericFutureListener() {
				@Override
				public void operationComplete(Future future) throws Exception {
					// 如果发送消息失败，记录失败日志
					if (!future.isSuccess()) {
						StringBuilder sb = new StringBuilder();
						sb.append("SendMessage ").append(msg.toString()).append(" Failed. ");
						logger.error(sb.toString(), future.cause());
					}
				}
			});

		} else {
			promise.addListener(listner);
		}
		return promise;
	}

	/**
	 * 同步发送长短信类型 <br/>
	 * 注意：该方法将拆分后的短信直接发送，不会再调用BusinessHandler里的write方法了。
	 */
	public static List<Promise> syncWriteLongMsgToEntity(String entity, BaseMessage msg) throws Exception {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		List<Promise> arrPromise = new ArrayList<Promise>();
		if (msg instanceof LongSMSMessage) {
			LongSMSMessage<BaseMessage> lmsg = (LongSMSMessage<BaseMessage>) msg;
			if (!lmsg.isReport()) {
				// 长短信拆分
				SmsMessage msgcontent = lmsg.getSmsMessage();
				List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);
				for (LongMessageFrame frame : frameList) {
					BaseMessage basemsg = (BaseMessage) lmsg.generateMessage(frame);
					Promise promise = connector.synwrite(basemsg);
					if (promise == null) {
						// 为空，可能是连接断了,直接返回
						return null;
					}
					arrPromise.add(promise);
				}
				return arrPromise;
			}
		}

		Promise promise = connector.synwrite(msg);
		if (promise == null) {
			// 为空，可能是连接断了,直接返回
			return null;
		}
		arrPromise.add(promise);
		return arrPromise;

	}

	/**
	 * 同步发送消息类型 <br/>
	 * 注意：该方法将直接发送，不会再调用BusinessHandler里的write方法了。
	 */
	public static Promise syncWriteMsgToEntity(String entity, BaseMessage msg) throws Exception {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);

		Promise promise = connector.synwrite(msg);
		if (promise == null) {
			// 为空，可能是连接断了,直接返回
			return null;
		}

		return promise;
	}
}
