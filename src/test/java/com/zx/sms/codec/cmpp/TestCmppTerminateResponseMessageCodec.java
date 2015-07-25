package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppTerminateResponseMessage;

public class TestCmppTerminateResponseMessageCodec extends AbstractTestMessageCodec<CmppTerminateResponseMessage>{

	@Test
	public void testCodec()
	{
		CmppTerminateResponseMessage msg = new CmppTerminateResponseMessage(1);
		
		ByteBuf buf =encode(msg);
		
		ByteBuf copybuf = buf.copy();
		Assert.assertEquals(12, buf.readableBytes());
		
		Assert.assertEquals(12, buf.readUnsignedInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(),buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());
		
		CmppTerminateResponseMessage result = decode(copybuf);
		
		Assert.assertTrue(result instanceof CmppTerminateResponseMessage); 
		
		Assert.assertEquals(msg.getHeader().getSequenceId(),((CmppTerminateResponseMessage)result).getHeader().getSequenceId());
	}
}
