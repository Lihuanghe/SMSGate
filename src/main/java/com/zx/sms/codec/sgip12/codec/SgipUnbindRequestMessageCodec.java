/**
 * 
 */
package com.zx.sms.codec.sgip12.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.sgip12.msg.SgipUnbindRequestMessage;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SgipUnbindRequestMessageCodec extends MessageToMessageCodec<Message, SgipUnbindRequestMessage> {
	PacketType packetType;

	public SgipUnbindRequestMessageCodec() {
		this(SgipPacketType.UNBINDREQUEST);
	}

	public SgipUnbindRequestMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		int commandId =  msg.getHeader().getCommandId();
		if (packetType.getCommandId() != commandId)
		{
			//不解析，交给下一个codec
			out.add(msg);
			return;
		}

		SgipUnbindRequestMessage requestMessage = new SgipUnbindRequestMessage(msg.getHeader());
		requestMessage.setTimestamp(msg.getTimestamp());
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, SgipUnbindRequestMessage msg, List<Object> out) throws Exception {
		msg.setBodyBuffer(GlobalConstance.emptyBytes);
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		out.add(msg);
	}

}
