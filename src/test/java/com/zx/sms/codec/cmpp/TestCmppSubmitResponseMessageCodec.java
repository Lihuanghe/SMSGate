package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp.packet.CmppSubmitResponse;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitResponse;
import com.zx.sms.common.util.MsgId;

public class TestCmppSubmitResponseMessageCodec  extends AbstractTestMessageCodec<CmppSubmitResponseMessage>{
	@Test
	public void testCode()
	{
		CmppSubmitResponseMessage msg = new CmppSubmitResponseMessage(238);
		
		msg.setMsgId(new MsgId());
		
		msg.setResult(3413&0xff);
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		int expectLength = (getVersion()==0x30?CmppSubmitResponse.MSGID.getBodyLength():Cmpp20SubmitResponse.MSGID.getBodyLength()) +  CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppSubmitResponseMessage result = decode(copybuf);
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

		Assert.assertEquals(msg.getMsgId(), result.getMsgId());
		Assert.assertEquals(msg.getResult(), result.getResult());
	}
}
