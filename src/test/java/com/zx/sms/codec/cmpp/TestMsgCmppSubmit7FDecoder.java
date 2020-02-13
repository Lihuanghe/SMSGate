package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.MsgId;

public class TestMsgCmppSubmit7FDecoder extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {
	@Override
	protected int getVersion() {
		return 0x7f;
	}

	public void testlongCodec(String content)
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[]{"13800138000"});
		msg.setLinkID("0000");
		msg.setMsgContent(content);
		msg.setMsgid(new MsgId());
		msg.setServiceId("10086");
		msg.setSrcId("10086");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a", 1);
		map.put("b", "adf");
		msg.setAttachment((Serializable)map);
		
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
			
			
	    	copybuf.writeBytes(buf.copy());
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
			

			buf =(ByteBuf)channel().readOutbound();
	    }
	    
		CmppSubmitRequestMessage result = decode(copybuf);
		
		System.out.println(result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		Assert.assertTrue(result.getAttachment() instanceof Map);
		Map retmap = (Map)result.getAttachment() ;
		Assert.assertEquals("adf", retmap.get("b"));
	}
	
	@Test
	public void testlongCodec()
	{
		testlongCodec("1234567890123456789中01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" );
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
		buf.readInt();
		
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());
	
		
		CmppSubmitRequestMessage result = decode(copybuf);
		

		Assert.assertEquals(msg.getAttachment(), result.getAttachment());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}

}
