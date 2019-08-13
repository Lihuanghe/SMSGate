package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppActiveTestResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppActiveTestResponse;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitRequest;

public class TestCmppActiveTestResponseMessageCodec extends AbstractTestMessageCodec<CmppActiveTestResponseMessage>{

	@Test
	public void testCodec()
	{
		CmppActiveTestResponseMessage msg = new CmppActiveTestResponseMessage(0x12fL);
		msg.setReserved((short )2);
		
		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();
		

		int length = buf.readableBytes();
		int expectLength =  CmppActiveTestResponse.RESERVED.getBodyLength()+ CmppHead.COMMANDID.getHeadLength();
		Assert.assertEquals(expectLength,length);
		Assert.assertEquals(expectLength, buf.readUnsignedInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(),buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());
		
		
		CmppActiveTestResponseMessage result = decode(newbuf);
		
		Assert.assertEquals(2, result.getReserved());
		System.out.println(msg.getHeader());
	
	}
}
