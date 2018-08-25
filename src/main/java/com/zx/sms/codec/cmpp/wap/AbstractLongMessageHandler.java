package com.zx.sms.codec.cmpp.wap;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;

public abstract class AbstractLongMessageHandler<T> extends MessageToMessageCodec<T, T> {
	private final Logger logger = LoggerFactory.getLogger(AbstractLongMessageHandler.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, T msg, List<Object> out) throws Exception {
		if (msg instanceof LongSMSMessage && needHandleLongMessage(msg)) {
			
			LongMessageFrame frame = ((LongSMSMessage)msg).generateFrame();
			String key = generateFrameKey(msg);
			try {
				SmsMessage content = LongMessageFrameHolder.INS.putAndget(key, frame);

				if (content != null) {
					resetMessageContent(msg, content);
					out.add(msg);
				} else {
					// 短信片断未接收完全，直接给网关回复resp，等待其它片断
					response(ctx, msg);
				}
			} catch (Exception ex) {
				logger.error("", ex);
				// 长短信解析失败，直接给网关回复 resp . 并丢弃这个短信
				logger.error("Decode Message Error ,msg dump :{}", ByteBufUtil.hexDump(frame.getMsgContentBytes()));
				response(ctx, msg);
			}
		} else {
			out.add(msg);
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, T requestMessage, List<Object> out) throws Exception {
		if (requestMessage instanceof LongSMSMessage  && needHandleLongMessage(requestMessage)) {
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

	protected abstract void response(ChannelHandlerContext ctx, T msg);

	protected abstract boolean needHandleLongMessage(T msg);

	protected abstract String generateFrameKey(T msg) throws Exception;

	protected abstract void resetMessageContent(T t, SmsMessage content);
}
