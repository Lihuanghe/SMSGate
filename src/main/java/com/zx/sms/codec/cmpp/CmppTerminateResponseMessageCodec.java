/**
 * 
 */
package com.zx.sms.codec.cmpp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.CmppTerminateResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class CmppTerminateResponseMessageCodec extends MessageToMessageCodec<Message, CmppTerminateResponseMessage> {
	private PacketType packetType;
	
	public CmppTerminateResponseMessageCodec() {
		this(CmppPacketType.CMPPTERMINATERESPONSE);
	}

	public CmppTerminateResponseMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		int commandId =  msg.getHeader().getCommandId();
        if(packetType.getCommandId() != commandId) {
			//不解析，交给下一个codec
			out.add(msg);
			return;
        } ;
        
        CmppTerminateResponseMessage responseMessage = new CmppTerminateResponseMessage(msg.getHeader());
        out.add(responseMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppTerminateResponseMessage msg, List<Object> out) throws Exception {
		msg.setBodyBuffer(GlobalConstance.emptyBytes);
		msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
		out.add(msg);
		
	}

}
