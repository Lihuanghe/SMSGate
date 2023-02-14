package com.zx.sms.codec.cmpp;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmsDcs;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.MsgId;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestCmpp20SubmitRequestMessageCodec extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {
	@Override
	protected int getVersion(){
		return 0x20;
	}


	@Test
	public void testCodec()
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		
		msg.setDestterminalId(new String[]{"13800138000","13800138001","138001380002"});
		msg.setLinkID("0000");
		String content = UUID.randomUUID().toString();
		msg.setMsgContent(content);
		msg.setMsgContent(new SmsTextMessage("你好，我是闪信！",new SmsDcs((byte)15)));
		
		msg.setMsgid(new MsgId());
		msg.setServiceId("10000");
		msg.setSrcId("10000");
		
		
		ByteBuf buf = encode(msg);
		
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		
		Assert.assertEquals(length, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
		
	
		
		CmppSubmitRequestMessage result = decode(copybuf);
		System.out.println(result);
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());
		Assert.assertArrayEquals(msg.getDestterminalId(), result.getDestterminalId());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
	}

	@Test
	public void testchinesecode()
	{
		testlongCodec("1234567890123456789中01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" );
	}

	@Test
	public void testASCIIcode()
	{
		testlongCodec("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
	}
	
	
	public void testlongCodec(String content)
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[]{"13800138000","13800138001","138001380002"});
		msg.setLinkID("0000");
		msg.setMsgContent(content);
		msg.setMsgid(new MsgId());
		msg.setServiceId("10000");
		msg.setSrcId("10000");
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
			
			
	    	ByteBuf copy = buf.copy();
	    	copybuf.writeBytes(copy);
	    	copy.release();
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
			buf.release();

			buf =(ByteBuf)channel().readOutbound();
	    }
	    
		CmppSubmitRequestMessage result = decode(copybuf);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getUniqueLongMsgId().getId());
		System.out.println(result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
		Assert.assertArrayEquals(msg.getDestterminalId(), result.getDestterminalId());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
}
