package com.zx.sms.codec.cmpp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsAlphabet;
import org.marre.sms.SmsMessage;
import org.marre.sms.SmsMsgClass;
import org.marre.sms.SmsTextMessage;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.cmpp.packet.CmppDeliverRequest;
import com.zx.sms.codec.cmpp.packet.CmppHead;
import com.zx.sms.codec.cmpp.packet.CmppSubmitRequest;
import com.zx.sms.common.util.MsgId;

public class TestCmppSubmitRequestMessageCodec  extends AbstractTestMessageCodec<CmppSubmitRequestMessage>{


	@Test
	public void testCodec()
	{
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[]{"13800138000"});
		msg.setLinkID("0000");
		msg.setMsgContent("123");
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
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
	}

	@Test
	public void testchinesecode()
	{
		
		testlongCodec(createTestReq("1234567890123456789中01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" ));
	}

	@Test
	public void testASCIIcode()
	{
		testlongCodec(createTestReq("1ABC56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
	}
	
	
	
	@Test
	public void testSLPUSH()
	{
		CmppSubmitRequestMessage msg = createTestReq("");
		WapSLPush sl = new WapSLPush("http://www.baidu.com");
		SmsMessage wap = new SmsWapPushMessage(sl);
		msg.setMsgContent(wap);
		CmppSubmitRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage)result.getMsg();
		WapSLPush actsl = (WapSLPush)smsmsg.getWbxml();
		Assert.assertEquals(sl.getUri(), actsl.getUri());
	}
	
	@Test
	public void testSIPUSH()
	{
		CmppSubmitRequestMessage msg = createTestReq("");
		WapSIPush si = new WapSIPush("http://www.baidu.com","baidu");
		SmsMessage wap = new SmsWapPushMessage(si);
		msg.setMsgContent(wap);
		CmppSubmitRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage)result.getMsg();
		WapSIPush actsi = (WapSIPush)smsmsg.getWbxml();
		Assert.assertEquals(si.getUri(), actsi.getUri());
		Assert.assertEquals(si.getMessage(), actsi.getMessage());
	}
	
	@Test
	public void testMMSPUSH()
	{
		CmppSubmitRequestMessage msg = createTestReq("");
		SmsMmsNotificationMessage mms = new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd?d2=23",50*1024);
		mms.setFrom("10085");
		msg.setMsgContent(mms);
		CmppSubmitRequestMessage result =testWapCodec(msg);
		SmsMmsNotificationMessage smsmsg = (SmsMmsNotificationMessage)result.getMsg();
		
		Assert.assertEquals(mms.getContentLocation_(), smsmsg.getContentLocation_());
	}
	
	@Test
	public void testseptedMsg(){

		String origin = "112aaaasssss2334455@£$¥èéùìòçØøÅåΔ_ΦΓΛΩΠΨΣΘΞ^{}\\[~]|€ÆæßÉ!\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà";
		System.out.println(origin);
		CmppSubmitRequestMessage msg = createTestReq(origin);
		msg.setMsgContent(new SmsTextMessage(origin, SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN));
		
		CmppSubmitRequestMessage ret =  testWapCodec(msg);
		Assert.assertEquals(msg.getMsgContent(), ret.getMsgContent());
	}
	
	private CmppSubmitRequestMessage createTestReq(String content) {

		// 取时间，用来查看编码解码时间
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[]{"13800138000"});
		msg.setLinkID("0000");
		msg.setMsgContent(content);
		msg.setMsgid(new MsgId());
		msg.setServiceId("10086");
		msg.setSrcId("10086");
		return msg;
	}
	public CmppSubmitRequestMessage  testWapCodec(CmppSubmitRequestMessage msg)
	{

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
	
		Assert.assertEquals(msg.getServiceId(), result.getServiceId());
		Assert.assertTrue(result.getMsg() instanceof SmsMessage);
		return result;
	}
	
	public void testlongCodec(CmppSubmitRequestMessage msg)
	{

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
