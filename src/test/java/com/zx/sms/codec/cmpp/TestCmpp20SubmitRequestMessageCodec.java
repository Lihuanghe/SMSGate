package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp.packet.CmppSubmitRequest;
import com.zx.sms.codec.cmpp20.packet.Cmpp20DeliverResponse;
import com.zx.sms.codec.cmpp20.packet.Cmpp20SubmitRequest;
import com.zx.sms.common.util.MsgId;

public class TestCmpp20SubmitRequestMessageCodec extends AbstractTestMessageCodec<CmppSubmitRequestMessage> {
	@Override
	protected int getVersion(){
		return 0x20;
	}


	@Test
	public void testCodec()
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[]{"13800138000"});
		msg.setLinkID("0000");
		msg.setMsgContent("根据国家法律法规，对于未实名号码将实施业务办理限制直至暂停通信服务。为了更好地维护您的个人权益，请您尽快办理实名补登记。即日起至2015年12月31日期间成功办理还能在次月获得1GB北京本地通用流量。为您提供多种自助办理方式，您只需下载北京移动客户端http://mobilebj.cn/?c=144/，即可通过“首页-热点推荐-实名登记”进行办理；或关注“中国移动北京公司”微信公众号，点击“我要办理-实名补登记”进行办理。");
		System.out.println(msg.getMsgContent().length());
		msg.setMsgid(new MsgId());
		msg.setServiceId("10086");
		msg.setSrcId("10086");
		ByteBuf buf = encode(msg);
		
		ByteBuf copybuf = buf.copy();
		
		int length = buf.readableBytes();
		
		Assert.assertEquals(length, buf.readUnsignedInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());
		
	
		
		CmppSubmitRequestMessage result = decode(copybuf);
		System.out.println(result);
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

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
		msg.setDestterminalId(new String[]{"13800138000"});
		msg.setLinkID("0000");
		msg.setMsgContent(content);
		msg.setMsgid(new MsgId());
		msg.setServiceId("10086");
		msg.setSrcId("10086");
		msg.setSupportLongMsg(true);
		channel().writeOutbound(msg);
		ByteBuf buf =channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
			
			
	    	copybuf.writeBytes(buf.copy());
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readUnsignedInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
			

			buf =channel().readOutbound();
	    }
	    
		CmppSubmitRequestMessage result = decode(copybuf);
		
		System.out.println(result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
}
