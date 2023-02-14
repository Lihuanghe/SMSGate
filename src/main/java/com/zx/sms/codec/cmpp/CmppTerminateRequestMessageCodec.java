/**
 * 
 */
package com.zx.sms.codec.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.CmppTerminateRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class CmppTerminateRequestMessageCodec extends MessageToMessageCodec<Message, CmppTerminateRequestMessage> {
	PacketType packetType;

	public CmppTerminateRequestMessageCodec() {
		this(CmppPacketType.CMPPTERMINATEREQUEST);
	}

	public CmppTerminateRequestMessageCodec(PacketType packetType) {
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

		CmppTerminateRequestMessage requestMessage = new CmppTerminateRequestMessage(msg.getHeader());
		out.add(requestMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppTerminateRequestMessage msg, List<Object> out) throws Exception {
		msg.setBodyBuffer(GlobalConstance.emptyBytes);
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		out.add(msg);
	}

}
