package com.zx.sms.codec.cmpp.wap;

import java.util.List;

import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

public abstract class AbstractLongMessageHandler<T extends BaseMessage> extends MessageToMessageCodec<T, T> {
	private final Logger logger = LoggerFactory.getLogger(AbstractLongMessageHandler.class);

	private EndpointEntity  entity;
	
	public AbstractLongMessageHandler(EndpointEntity entity) {
		this.entity = entity;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, T msg, List<Object> out) throws Exception {
		if ((entity==null || entity.getSupportLongmsg() == SupportLongMessage.BOTH||entity.getSupportLongmsg() == SupportLongMessage.RECV) && msg instanceof LongSMSMessage && needHandleLongMessage(msg)) {
			
			LongMessageFrame frame = ((LongSMSMessage)msg).generateFrame();
			String key = generateFrameKey(msg);
			try {
				SmsMessage content = LongMessageFrameHolder.INS.putAndget(key, frame);

				if (content != null) {
					resetMessageContent(msg, content);
					out.add(msg);
				} else {
					// 短信片断未接收完全，直接给网关回复resp，等待其它片断
					BaseMessage res = response(msg);
					res.setRequest(msg);
					ctx.writeAndFlush(res);
					
					//为了能让业务hander知道合并长短信时生成的msgId，这里将res 和req都做为userEvent抛出来
					ctx.fireUserEventTriggered(res);
				}
			} catch (Exception ex) {
				logger.error("", ex);
				// 长短信解析失败，直接给网关回复 resp . 并丢弃这个短信
				logger.error("Decode Message Error ,msg dump :{}", ByteBufUtil.hexDump(frame.getMsgContentBytes()));
				BaseMessage res = response(msg);
				res.setRequest(msg);
				ctx.writeAndFlush(res);
				
				//为了能让业务hander知道合并长短信时生成的msgId，这里将res 和req都做为userEvent抛出来
				ctx.fireUserEventTriggered(res);
			}
		} else {
			out.add(msg);
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, T requestMessage, List<Object> out) throws Exception {
		if ((entity==null || entity.getSupportLongmsg() == SupportLongMessage.BOTH||entity.getSupportLongmsg() == SupportLongMessage.SEND) && requestMessage instanceof LongSMSMessage  && needHandleLongMessage(requestMessage)) {
			SmsMessage msgcontent = ((LongSMSMessage)requestMessage).getSmsMessage();
			List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);
			boolean first = true;
			LongSMSMessage lmsg = (LongSMSMessage)requestMessage;
			for (LongMessageFrame frame : frameList) {
				T t = (T)lmsg.generateMessage(frame);
				out.add(t);
			}
		} else {
			out.add(requestMessage);
		}
	}

	protected abstract BaseMessage response(T msg);

	protected abstract boolean needHandleLongMessage(T msg);

	protected abstract String generateFrameKey(T msg) throws Exception;

	protected abstract void resetMessageContent(T t, SmsMessage content);
}
