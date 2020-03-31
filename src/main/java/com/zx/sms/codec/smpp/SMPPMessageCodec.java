package com.zx.sms.codec.smpp;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.Pdu;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

public class SMPPMessageCodec extends MessageToMessageCodec<ByteBuf, Pdu> {
	private static final PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
	private static final DeliverSmReceiptCodec reportcodec = new DeliverSmReceiptCodec();
	private final Logger logger = LoggerFactory.getLogger(SMPPMessageCodec.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Pdu msg, List<Object> out) throws Exception {
		try {
			if (msg instanceof DeliverSmReceipt) {
				List<Object> deliout = new ArrayList(1);
				reportcodec.encode(ctx, (DeliverSmReceipt) msg, deliout);
				Pdu deli = (DeliverSm) deliout.get(0);
				ByteBuf buf = transcoder.encode(deli, ctx.alloc());
				out.add(buf);
			} else {
				ByteBuf buf = transcoder.encode(msg, ctx.alloc());
				out.add(buf);
			}
		} catch (Exception e) {
			logger.error("",e);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		try {
			Pdu pdu = transcoder.decode(msg);
			if (pdu != null) {
				if (pdu instanceof DeliverSm) {
					DeliverSm deli = (DeliverSm) pdu;
					reportcodec.decode(ctx, deli, out);
				} else {
					out.add(pdu);
				}

			}
		} catch (Exception e) {
			logger.error("",e);
		}
	}

}
