package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppConnectRequestMessage;
import com.zx.sms.codec.cmpp.packet.CmppCancelResponse;
import com.zx.sms.codec.cmpp.packet.CmppConnectRequest;
import com.zx.sms.codec.cmpp.packet.CmppHead;


public class TestCmppConnectRequestMessageCodec extends AbstractTestMessageCodec<CmppConnectRequestMessage>{
	@Test
	public void testCode()
	{
		CmppConnectRequestMessage msg = new CmppConnectRequestMessage();
		msg.setSourceAddr("106581");
		//长度为16
		msg.setAuthenticatorSource("passwordpassword".getBytes());
		
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		int expectLength = CmppConnectRequest.AUTHENTICATORSOURCE.getBodyLength() +  CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppConnectRequestMessage result = decode(copybuf);
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());
		Assert.assertEquals(msg.getSourceAddr(),result.getSourceAddr());
		Assert.assertArrayEquals(msg.getAuthenticatorSource(), result.getAuthenticatorSource());
	}
}
