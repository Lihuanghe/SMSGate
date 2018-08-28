package com.zx.sms.codec.smpp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.smpp.msg.Pdu;

public class SMPPMessageCodec extends MessageToMessageCodec<ByteBuf, Pdu> {
	private static final PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
	@Override
	protected void encode(ChannelHandlerContext ctx, Pdu msg, List<Object> out) throws Exception {
		ByteBuf buf = transcoder.encode(msg,ctx.alloc());
		out.add(buf);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		Pdu pdu = transcoder.decode(msg);
		if(pdu!=null) out.add(pdu);
	}

}
