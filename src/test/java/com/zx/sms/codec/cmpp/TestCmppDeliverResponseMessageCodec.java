package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppDeliverResponse;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp20.packet.Cmpp20DeliverResponse;
import com.zx.sms.common.util.MsgId;

public class TestCmppDeliverResponseMessageCodec extends AbstractTestMessageCodec<CmppDeliverResponseMessage> {

	@Test
	public void testCode()
	{
		CmppDeliverResponseMessage msg = new CmppDeliverResponseMessage(238);
		
		msg.setMsgId(new MsgId());
		
		msg.setResult(3);
		ByteBuf buf = encode(msg);
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		int expectLength = (getVersion()==0x30?CmppDeliverResponse.MSGID.getBodyLength():Cmpp20DeliverResponse.MSGID.getBodyLength()) +  CmppHead.COMMANDID.getHeadLength();
		
		Assert.assertEquals(expectLength, length);
		Assert.assertEquals(expectLength, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
		CmppDeliverResponseMessage result = decode(copybuf);
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

		Assert.assertEquals(msg.getMsgId(), result.getMsgId());
		Assert.assertEquals(msg.getResult(), result.getResult());
	}
}
