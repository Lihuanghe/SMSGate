package com.zx.sms.common.util;

import java.util.ArrayList;
import java.util.List;

import com.zx.sms.common.ErrorChannelFuture;
import io.netty.util.concurrent.*;
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

import io.netty.channel.ChannelFuture;

public class ChannelUtil {

	private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);

	public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg) {
		EndpointConnector connector = entity.getSingletonConnector();
		return asyncWriteToEntity(connector, msg, null);
	}

	public static ChannelFuture asyncWriteToEntity(String entity, Object msg) {
		EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
		EndpointConnector connector = e.getSingletonConnector();
		return asyncWriteToEntity(connector, msg, null);
	}

	public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg, GenericFutureListener listner) {

		EndpointConnector connector = entity.getSingletonConnector();
		return asyncWriteToEntity(connector, msg, listner);
	}

	public static ChannelFuture asyncWriteToEntity(final String entity, final Object msg, GenericFutureListener listner) {

		EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
		EndpointConnector connector = e.getSingletonConnector();
		return asyncWriteToEntity(connector, msg, listner);
	}

	private static ChannelFuture asyncWriteToEntity(EndpointConnector connector, final Object msg, GenericFutureListener listner) {
		if(msg == null){
			return new ErrorChannelFuture("The message to write is null");
		}
		if (connector == null)
			return new ErrorChannelFuture("No available connector,the client may not connected");

		ChannelFuture promise = connector.asynwrite(msg);

		if (promise == null)
			return  new ErrorChannelFuture("Write msg with unknown error");

		if (listner == null) {
			promise.addListener(new ErrorGenericFutureListener(msg));

		} else {
			promise.addListener(listner);
		}
		return promise;
	}

	public static <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(EndpointEntity e, BaseMessage msg) throws Exception {
		List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
		EndpointConnector connector = e.getSingletonConnector();
		if(connector == null) {
			DefaultPromise<T> errorPromise = new DefaultPromise<T>(GlobalEventExecutor.INSTANCE);
			errorPromise.setFailure(new IllegalStateException("No available connector,the client may not connected"));
			arrPromise.add(errorPromise);
			return arrPromise;
		}
		
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
			DefaultPromise<T> errorPromise = new DefaultPromise<T>(GlobalEventExecutor.INSTANCE);
			errorPromise.setFailure(new IllegalStateException("Write msg with unknown error"));
			arrPromise.add(errorPromise);
			return arrPromise;
		}

		arrPromise.add(promise);
		return arrPromise;
	}

	/**
	 * 同步发送长短信类型 <br/>
	 * 注意：该方法将拆分后的短信直接发送，不会再调用BusinessHandler里的write方法了。
	 */
	public static <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(String entity, BaseMessage msg) throws Exception {
		EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
		if(e == null) {
			logger.warn("EndpointEntity {} is null",entity);
			DefaultPromise<T> errorPromise = new DefaultPromise<T>(GlobalEventExecutor.INSTANCE);
			errorPromise.setFailure(new IllegalStateException("Write msg with unknown error"));
			List<Promise<T>> arrPromise = new ArrayList<Promise<T>>(1);
			arrPromise.add(errorPromise);
			return arrPromise;
		}
		return syncWriteLongMsgToEntity(e,msg);
	}

	/**
	 * 同步发送消息类型 <br/>
	 * 注意：该方法将直接发送至编码器，不会再调用BusinessHandler里的write方法了。
	 * 因此对于Deliver和Submit消息必须自己进行长短信拆分，设置PDU等相关字段
	 *一般此方法用来发送二进制短信等特殊短信，需要自己生成短信的二进制内容。
	 *正常短信下发要使用 syncWriteLongMsgToEntity 方法
	 */
	public static <T extends BaseMessage> Promise<T> syncWriteBinaryMsgToEntity(String entity, BaseMessage msg) throws Exception {
		EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
		EndpointConnector connector = e.getSingletonConnector();

		Promise<T> promise = connector.synwrite(msg);
		if (promise == null) {
			// 为空，可能是连接断了,直接返回
			DefaultPromise<T> errorPromise = new DefaultPromise<T>(GlobalEventExecutor.INSTANCE);
			errorPromise.setFailure(new IllegalStateException("Write msg with unknown error"));
			return errorPromise;
		}

		return promise;
	}

	private static class ErrorGenericFutureListener implements GenericFutureListener {
		private final Object msg;

		public ErrorGenericFutureListener(Object msg) {
			this.msg = msg;
		}

		@Override
		public void operationComplete(Future future) throws Exception {
			// 如果发送消息失败，记录失败日志
			if (!future.isSuccess()) {
				StringBuilder sb = new StringBuilder();
				sb.append("SendMessage ").append(msg.toString()).append(" Failed. ");
				if(future instanceof ErrorChannelFuture)
					logger.error(sb.toString(), ((ErrorChannelFuture)future).getErrorMsg());
				else
					logger.error(sb.toString(), future.cause());

			}
		}
	}
}
