package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppTerminateRequestMessage;


public class TestCmppTerminateRequestMessageCodec extends AbstractTestMessageCodec<CmppTerminateRequestMessage>{
	@Test
	public void testCodec()
	{
		CmppTerminateRequestMessage msg = new CmppTerminateRequestMessage();
		
		
		ByteBuf buf =encode(msg);
		
		ByteBuf copybuf = buf.copy();
		Assert.assertEquals(12, buf.readableBytes());
		
		Assert.assertEquals(12, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(),buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppTerminateRequestMessage result = decode(copybuf);
		
		Assert.assertTrue(result instanceof CmppTerminateRequestMessage); 
		
		Assert.assertEquals(msg.getHeader().getSequenceId(),((CmppTerminateRequestMessage)result).getHeader().getSequenceId());
	}
}
