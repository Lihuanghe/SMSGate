package com.zx.sms.codec.cmpp;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmsDcs;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.SignatureType;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

import io.netty.buffer.ByteBuf;

public class TestCmppFixSignHeadCodec extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {
	@Override
	protected int getVersion(){
		return 0x20;
	}

		
	private static String signTxt = "【温馨提示】";
	protected EndpointEntity buildEndpointEntity() {
		EndpointEntity e = new CMPPClientEndpointEntity();
		e.setId(EndPointID);
		e.setSignatureType(new SignatureType(false,signTxt));
		return e;
	}
	
	@Test
	public void testCodecLong()
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		
		msg.setDestterminalId(new String[]{"13800138000","13800138001","138001380002"});
		msg.setLinkID("0000");
		String content = UUID.randomUUID().toString();
		msg.setMsgContent(new SmsTextMessage(signTxt+"移娃没理解您的问题2【温馨提示】移娃没理解您的问题3【温馨提示】移娃没理解您的问题4【温馨提示】移娃没理解您的问题5【",new SmsDcs((byte)8)));
		
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
		Assert.assertEquals(msg.getMsgContent(), signTxt+result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
	}
	

	@Test
	public void testCodec()
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		
		msg.setDestterminalId(new String[]{"13800138000","13800138001","138001380002"});
		msg.setLinkID("0000");
		String content = UUID.randomUUID().toString();
		msg.setMsgContent(content);
		msg.setMsgContent(new SmsTextMessage(signTxt+"你好，我是闪信！",new SmsDcs((byte)15)));
		
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
		Assert.assertEquals(msg.getMsgContent(), signTxt+result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
	}

	
}
