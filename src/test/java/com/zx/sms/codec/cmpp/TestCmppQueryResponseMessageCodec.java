package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp.packet.CmppQueryResponse;

public class TestCmppQueryResponseMessageCodec  extends AbstractTestMessageCodec<CmppQueryResponseMessage>{


	@Test
	public void testCodec()
	{
		CmppQueryResponseMessage msg = new CmppQueryResponseMessage(1313);
		msg.setQueryCode("sdf");

		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		int expectLength = CmppQueryResponse.QUERYCODE.getBodyLength() +  CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
	
		CmppQueryResponseMessage result = decode(copybuf);
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

		Assert.assertEquals(msg.getQueryCode(), result.getQueryCode());

	}
}
