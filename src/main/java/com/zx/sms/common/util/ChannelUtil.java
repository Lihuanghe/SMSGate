package com.zx.sms.common.util;

import java.util.ArrayList;
import java.util.List;

import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

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
	public static <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(String entity, BaseMessage msg) throws Exception {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);
		if(connector == null) return null;
		
		if (msg instanceof LongSMSMessage) {
			LongSMSMessage<BaseMessage> lmsg = (LongSMSMessage<BaseMessage>) msg;
			if (!lmsg.isReport()) {
				// 长短信拆分
				SmsMessage msgcontent = lmsg.getSmsMessage();
				List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);
				
				//保证同一条长短信，通过同一个tcp连接发送
				List<BaseMessage> msgs = new ArrayList<BaseMessage>();
				for (LongMessageFrame frame : frameList) {
					BaseMessage basemsg = (BaseMessage) lmsg.generateMessage(frame);
					msgs.add(basemsg);
				}
				return connector.synwrite(msgs);
			}
		}

		Promise promise = connector.synwrite(msg);
		if (promise == null) {
			// 为空，可能是连接断了,直接返回
			return null;
		}
		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		arrPromise.add(promise);
		return arrPromise;

	}

	/**
	 * 同步发送消息类型 <br/>
	 * 注意：该方法将直接发送至编码器，不会再调用BusinessHandler里的write方法了。
	 * 因此对于Deliver和Submit消息必须自己进行长短信拆分，设置PDU等相关字段
	 *一般此方法用来发送二进制短信等特殊短信，需要自己生成短信的二进制内容。
	 *正常短信下发要使用 syncWriteLongMsgToEntity 方法
	 */
	public static <T extends BaseMessage> Promise<T> syncWriteBinaryMsgToEntity(String entity, BaseMessage msg) throws Exception {

		EndpointConnector connector = EndpointManager.INS.getEndpointConnector(entity);

		Promise<T> promise = connector.synwrite(msg);
		if (promise == null) {
			// 为空，可能是连接断了,直接返回
			return null;
		}

		return promise;
	}
}
