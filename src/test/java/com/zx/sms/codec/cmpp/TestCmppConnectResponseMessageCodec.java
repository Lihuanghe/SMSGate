package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppConnectResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppConnectResponse;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp20.packet.Cmpp20ConnectResponse;

public class TestCmppConnectResponseMessageCodec extends AbstractTestMessageCodec<CmppConnectResponseMessage>{
	@Test
	public void testCode()
	{
		CmppConnectResponseMessage msg = new CmppConnectResponseMessage(238);
		
		
		//长度为16
		msg.setAuthenticatorISMG("passwordpassword".getBytes());
		
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		int expectLength = (getVersion()==0x30?CmppConnectResponse.AUTHENTICATORISMG.getBodyLength():Cmpp20ConnectResponse.AUTHENTICATORISMG.getBodyLength()) +  CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppConnectResponseMessage result = decode(copybuf);
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

		Assert.assertArrayEquals(msg.getAuthenticatorISMG(), result.getAuthenticatorISMG());
	}
}
