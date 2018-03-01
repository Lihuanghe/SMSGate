package com.zx.sms.connect.manager.smpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.smpp.DeliverSmReceiptCodec;
import com.zx.sms.codec.smpp.SMPPMessageCodec;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

public class SMPPCodecChannelInitializer extends ChannelInitializer<Channel> {
	private static final Logger logger = LoggerFactory.getLogger(SMPPCodecChannelInitializer.class);
	
	public static String pipeName() {
		return "cmppCodec";
	}
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addBefore(pipeName(), "FrameDecoder", new LengthFieldBasedFrameDecoder(4 * 1024 , 0, 4, -4, 0, true));

		pipeline.addBefore(pipeName(), GlobalConstance.codecName, new SMPPMessageCodec());
		pipeline.addBefore(pipeName(), "DeliverSmReceiptCodec", new DeliverSmReceiptCodec());
		
		pipeline.addBefore(pipeName(), "SMPPLongMessageHandler", new SMPPLongMessageHandler());
		
	}
	
	private class SMPPLongMessageHandler extends AbstractLongMessageHandler<BaseSm> {
		
		
		@Override
		protected void response(ChannelHandlerContext ctx, BaseSm msg) {
			ctx.writeAndFlush(msg.createResponse());
		}

		@Override
		protected boolean needHandleLongMessage(BaseSm msg) {
			
			if(msg instanceof DeliverSmReceipt)
			{
				return false;
			}
			return true;
		}

		@Override
		protected String generateFrameKey(BaseSm msg) throws Exception{
			if(msg instanceof SubmitSm){
				return msg.getDestAddress().getAddress();
			}else if(msg instanceof DeliverSm){
				return msg.getSourceAddress().getAddress();
			}else{
				logger.warn("not support Type {}" ,msg.getClass());
				throw new NotSupportedException("not support LongMessage Type  "+ msg.getClass());
			}
		}

		@Override
		protected SmsMessage getSmsMessage(BaseSm t) {
			return t.getSmsMsg();
		}

		@Override
		protected void resetMessageContent(BaseSm t, SmsMessage content) {
			t.setSmsMsg(content);
		}
		
	}

}
