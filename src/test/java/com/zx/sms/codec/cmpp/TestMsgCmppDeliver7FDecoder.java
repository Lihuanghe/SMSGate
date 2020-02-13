package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;

public class TestMsgCmppDeliver7FDecoder extends AbstractTestMessageCodec<CmppDeliverRequestMessage> {
	@Override
	protected int getVersion() {
		return 0x7F;
	}

	
	
	@Test
	public void testCodec()
	{
		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage();
	
		msg.setAttachment("adfad");
		msg.setMsgContent("12341");
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		// packageLength
		buf.readInt();
		
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
	
		
		CmppDeliverRequestMessage result = decode(copybuf);
		

		Assert.assertEquals(msg.getAttachment(), result.getAttachment());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
	@Test
	public void testCodecNullAttach()
	{
		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage();
	
	
		msg.setMsgContent("12341");
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		// packageLength
		buf.readInt();
		
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
	
		
		CmppDeliverRequestMessage result = decode(copybuf);
		

		Assert.assertEquals(msg.getAttachment(), result.getAttachment());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
	
}
