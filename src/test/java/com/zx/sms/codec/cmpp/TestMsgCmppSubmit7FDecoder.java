package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;

public class TestMsgCmppSubmit7FDecoder extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {
	@Override
	protected int getVersion() {
		return 0x7F;
	}

	
	
	@Test
	public void testCodec()
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 1);
		map.put("b", "adf");
		msg.setAttachment((Serializable)map);
		msg.setMsgContent("12341");
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		// packageLength
		buf.readUnsignedInt();
		
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());
	
		
		CmppSubmitRequestMessage result = decode(copybuf);
		

		Assert.assertEquals(msg.getAttachment(), result.getAttachment());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}

}
