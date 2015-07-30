/**
 * 
 */
package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppCancelResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppCancelResponse;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppCancelResponseMessageCodec extends MessageToMessageCodec<Message, CmppCancelResponseMessage> {
	private PacketType packetType;
	
	public CmppCancelResponseMessageCodec() {
		this(CmppPacketType.CMPPCANCELRESPONSE);
	}

	public CmppCancelResponseMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
		if (packetType.getCommandId() != commandId)
		{
			//不解析，交给下一个codec
			out.add(msg);
			return;
		}

		CmppCancelResponseMessage responseMessage = new CmppCancelResponseMessage(msg.getHeader());
		ByteBuf  bodyBuffer = msg.getBodyBuffer();
		responseMessage.setSuccessId(bodyBuffer.readUnsignedInt());
		ReferenceCountUtil.release(bodyBuffer);
		out.add(responseMessage);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppCancelResponseMessage msg, List<Object> out) throws Exception {

		ByteBuf bodyBuffer = ctx.alloc().buffer(CmppCancelResponse.SUCCESSID.getLength());
        bodyBuffer.writeInt((int) msg.getSuccessId());
		
		msg.setBodyBuffer(bodyBuffer);
		out.add(msg);
	}

}
