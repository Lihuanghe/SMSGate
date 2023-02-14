package com.zx.sms.codec.cmpp;


import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppCancelRequestMessage;
import com.zx.sms.codec.cmpp.packet.CmppCancelRequest;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.common.util.MsgId;

public class TestCmppCancelRequestMessageCodec  extends AbstractTestMessageCodec<CmppCancelRequestMessage>{

	@Test
	public void testCode()
	{
		CmppCancelRequestMessage msg = new CmppCancelRequestMessage();
		msg.setMsgId(new MsgId());
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		
		
		int length = buf.readableBytes();
		int expectLength = CmppCancelRequest.MSGID.getBodyLength() +  CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppCancelRequestMessage result = decode(copybuf);
		Assert.assertEquals(msg.getMsgId(), result.getMsgId());
	}
}
