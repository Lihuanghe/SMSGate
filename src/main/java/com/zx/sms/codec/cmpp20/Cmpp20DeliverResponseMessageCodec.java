/**
 * 
 */
package com.zx.sms.codec.cmpp20;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppDeliverResponse;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.cmpp20.packet.Cmpp20DeliverResponse;
import com.zx.sms.codec.cmpp20.packet.Cmpp20PacketType;
import com.zx.sms.common.util.DefaultMsgIdUtil;

/**
 * shifei(shifei@asiainfo.com)
 *
 */
public class Cmpp20DeliverResponseMessageCodec extends MessageToMessageCodec<Message, CmppDeliverResponseMessage> {
	private PacketType packetType;
	/**
	 * 
	 */
	public Cmpp20DeliverResponseMessageCodec() {
		this(Cmpp20PacketType.CMPPDELIVERRESPONSE);
	}

	public Cmpp20DeliverResponseMessageCodec(PacketType packetType) {
		this.packetType = packetType;
	}


	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        long commandId = ((Long) msg.getHeader().getCommandId()).longValue();
        
        if(packetType.getCommandId() != commandId) {
			//不解析，交给下一个codec
			out.add(msg);
			return;
        } ;	
		out.add(decode(msg));
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, CmppDeliverResponseMessage msg, List<Object> out) throws Exception {
        
		ByteBuf bodyBuffer = Unpooled.buffer(Cmpp20DeliverResponse.MSGID.getBodyLength());
        bodyBuffer.writeBytes(DefaultMsgIdUtil.msgId2Bytes(msg.getMsgId()));
        bodyBuffer.writeByte((int) msg.getResult());
        
        msg.setBodyBuffer(bodyBuffer.array());
        msg.getHeader().setBodyLength(msg.getBodyBuffer().length);
        ReferenceCountUtil.release(bodyBuffer);
		out.add(msg);
		
	}
	public static CmppDeliverResponseMessage decode(Message msg){
        CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(msg.getHeader());
     
        
        ByteBuf bodyBuffer = Unpooled.wrappedBuffer(msg.getBodyBuffer());
        
		responseMessage.setMsgId(DefaultMsgIdUtil.bytes2MsgId(bodyBuffer
				.readBytes(Cmpp20DeliverResponse.MSGID.getLength())
				.array()));
		responseMessage.setResult(bodyBuffer.readUnsignedByte());
		
		ReferenceCountUtil.release(bodyBuffer);
		return responseMessage;
	}
}
