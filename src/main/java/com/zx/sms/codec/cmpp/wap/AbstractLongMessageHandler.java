package com.zx.sms.codec.cmpp.wap;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

public abstract class AbstractLongMessageHandler<T extends BaseMessage> extends MessageToMessageCodec<T, T> {
	private final Logger logger = LoggerFactory.getLogger(AbstractLongMessageHandler.class);

	private EndpointEntity entity;

	public AbstractLongMessageHandler(EndpointEntity entity) {
		this.entity = entity;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, T msg, List<Object> out) throws Exception {
		if ((entity == null || entity.getSupportLongmsg() == SupportLongMessage.BOTH|| entity.getSupportLongmsg() == SupportLongMessage.RECV)
				&& msg instanceof LongSMSMessage
				&& ((LongSMSMessage) msg).needHandleLongMessage()) {
			
			LongSMSMessage lmsg = (LongSMSMessage) msg;
			UniqueLongMsgId uniqueId = lmsg.getUniqueLongMsgId();

			try {
				SmsMessageHolder hoder = LongMessageFrameHolder.INS.putAndget(entity, uniqueId.getId(), lmsg,
						entity != null && entity.isRecvLongMsgOnMultiLink());

				if (hoder != null) {

					// 合并完成，及时删除UniqueLongMsgId中的uniqeId缓存
					uniqueId.clearCacheKey();

					resetMessageContent((T) hoder.msg, hoder.smsMessage);

					// 长短信合并完成，返回的这个msg里已经包含了所有的短信短断。后边的handler响应response时要包含这些片断。
					out.add(hoder.msg);
				}
			} catch (Exception ex) {
				// 合并失败的，及时删除UniqueLongMsgId中的uniqeId缓存
				uniqueId.clearCacheKey();
				
				// 长短信解析失败，直接给网关回复 resp . 并丢弃这个短信
				logger.error("Decode Message Error ,entity : {} ,uniqueId : {} , msg dump :{}", entity.getId(),
						uniqueId, ByteBufUtil.hexDump(lmsg.generateFrame().getMsgContentBytes()));
			}
		} else {
			out.add(msg);
		}
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, T requestMessage, List<Object> out) throws Exception {

		if ((entity == null || entity.getSupportLongmsg() == SupportLongMessage.BOTH|| entity.getSupportLongmsg() == SupportLongMessage.SEND) 
				&& requestMessage instanceof LongSMSMessage
				&& ((LongSMSMessage) requestMessage).needHandleLongMessage()) {
			
			List<T> splittedMsg = LongMessageFrameHolder.INS.splitLongSmsMessage(entity, requestMessage,ctx.channel());
			
			out.addAll(splittedMsg);
		} else {
			out.add(requestMessage);
		}
	}
	
	protected abstract void resetMessageContent(T t, SmsMessage content);
}
