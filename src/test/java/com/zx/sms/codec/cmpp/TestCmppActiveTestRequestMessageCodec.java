package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.Header;

public class TestCmppActiveTestRequestMessageCodec extends AbstractTestMessageCodec<CmppActiveTestRequestMessage> {

	protected CmppActiveTestRequestMessage createMsg(){
		Header header = new DefaultHeader();
		
		header.setSequenceId(0X761ae);
		
		CmppActiveTestRequestMessage msg = new CmppActiveTestRequestMessage(header);
		return msg;
	}
	@Test
	public void testCodec()
	{
		CmppActiveTestRequestMessage msg = createMsg();
		
		ByteBuf buf =encode(msg);
		
		ByteBuf copybuf = buf.copy();
		
		Assert.assertEquals(12, buf.readableBytes());
		
		Assert.assertEquals(12, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(),buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppActiveTestRequestMessage result = decode(copybuf);
		
		Assert.assertTrue(result instanceof CmppActiveTestRequestMessage); 
		
		Assert.assertEquals(msg.getHeader().getSequenceId(),((CmppActiveTestRequestMessage)result).getHeader().getSequenceId());
		System.out.println(msg.getHeader());
	}
}
