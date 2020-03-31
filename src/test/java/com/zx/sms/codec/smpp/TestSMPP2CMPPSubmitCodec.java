package com.zx.sms.codec.smpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsMessage;
import org.marre.sms.SmsPort;
import org.marre.sms.SmsPortAddressedTextMessage;
import org.marre.sms.SmsTextMessage;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;

import com.zx.sms.codec.AbstractSMPPTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.smpp.SMPPCodecChannelInitializer;
import com.zx.sms.handler.smpp.SMPP2CMPPBusinessHandler;
import com.zx.sms.handler.smpp.SMPPLongMessageHandler;

public class TestSMPP2CMPPSubmitCodec extends AbstractSMPPTestMessageCodec<CmppSubmitRequestMessage> {
	protected void doinitChannel(Channel ch){
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		ChannelPipeline pipeline = ch.pipeline();
		SMPPCodecChannelInitializer codec = new SMPPCodecChannelInitializer();
		pipeline.addLast("serverLog", new LoggingHandler(LogLevel.DEBUG));
		pipeline.addLast(codec.pipeName(), codec);
		pipeline.addLast( "SMPPLongMessageHandler", new SMPPLongMessageHandler(null));
		pipeline.addLast("SMPP2CMPPCodec", new SMPP2CMPPBusinessHandler());
	}
	


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
		
		
		CmppSubmitRequestMessage result = decode(copybuf);
		
		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());

		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}

	@Test
	public void testchinesecode()
	{
		
		testlongCodec(createTestReq("尊敬的客户,您好！您于2016-03-23 14:51:36通过中国移动10085销售专线订购的【一加手机高清防刮保护膜】，请点击支付http://www.10085.cn/web85/page/zyzxpay/wap_order.html?orderId=76DEF9AE1808F506FD4E6CB782E3B8E7EE875E766D3D335C 完成下单。请在60分钟内完成支付，如有疑问，请致电10085咨询，谢谢！中国移动10085"));
	}

	@Test
	public void testASCIIcode()
	{
		testlongCodec(createTestReq("12345678901AssBC56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abcdefghijklmnopqrstuvwxyzABCE"));
	}
	
	
	@Test
	public void testSLPUSH()
	{
		CmppSubmitRequestMessage msg = createTestReq("");
		WapSLPush sl = new WapSLPush("http://www.baidu.com");
		SmsMessage wap = new SmsWapPushMessage(sl);
		msg.setMsgContent(wap);
		CmppSubmitRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage)result.getSmsMessage();
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
		SmsWapPushMessage smsmsg = (SmsWapPushMessage)result.getSmsMessage();
		WapSIPush actsi = (WapSIPush)smsmsg.getWbxml();
		Assert.assertEquals(si.getUri(), actsi.getUri());
		Assert.assertEquals(si.getMessage(), actsi.getMessage());
	}
	@Test
	public void testPortTextSMSH()
	{
		Random rnd_ = new Random();
		CmppSubmitRequestMessage msg = createTestReq("");
		SmsPortAddressedTextMessage textMsg =new SmsPortAddressedTextMessage(new SmsPort(rnd_.nextInt() &0xffff,"")  ,new SmsPort(rnd_.nextInt()&0xffff,""),"这是一条端口文本短信");
		msg.setMsgContent(textMsg);
		CmppSubmitRequestMessage result =testWapCodec(msg);
		SmsPortAddressedTextMessage smsmsg = (SmsPortAddressedTextMessage)result.getSmsMessage();
		Assert.assertEquals(textMsg.getDestPort_(), smsmsg.getDestPort_());
		Assert.assertEquals(textMsg.getOrigPort_(), smsmsg.getOrigPort_());
		Assert.assertEquals(textMsg.getText(), smsmsg.getText());
	}
	@Test
	public void testMMSPUSH()
	{
		CmppSubmitRequestMessage msg = createTestReq("");
		SmsMmsNotificationMessage mms = new SmsMmsNotificationMessage("https://www.baidu.com/s?wd=SMPPv3.4%20%E9%95%BF%E7%9F%AD%E4%BF%A1&rsv_spt=1&rsv_iqid=0xdd4666100001e74c&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=0&oq=SMPPv%2526lt%253B.4%2520ton%2520npi&rsv_t=50fdNrphqry%2FYfHh29wvp8KzJ9ogqigiPr33FT%2FpcGQu6X34vByQNu4O%2FLNZgIiXdd16&inputT=3203&rsv_pq=d576ead9000016eb&rsv_sug3=60&rsv_sug1=15&rsv_sug7=000&rsv_sug2=0&rsv_sug4=3937&rsv_sug=1",50*1024);
		mms.setFrom("10085");
		mms.setSubject("这是一条测试彩信，彩信消息ID是：121241");
		msg.setMsgContent(mms);
		CmppSubmitRequestMessage result =testWapCodec(msg);
		SmsMmsNotificationMessage smsmsg = (SmsMmsNotificationMessage)result.getSmsMessage();
		Assert.assertEquals(mms.getSubject_(), smsmsg.getSubject_());
		Assert.assertEquals(mms.getContentLocation_(), smsmsg.getContentLocation_());
		Assert.assertEquals(mms.getFrom_(), smsmsg.getFrom_());
	}
	
	@Test
	public void testseptedMsg(){

		String origin = "112aaaasssss2334455@£$¥èéùìòçØøÅåΔ_ΦΓΛΩΠΨΣΘΞ^{}\\[~]|€ÆæßÉ!\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà";
		System.out.println(origin);
		CmppSubmitRequestMessage msg = createTestReq(origin);
		msg.setMsgContent(new SmsTextMessage(origin));
		
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
		System.out.println(result);
		Assert.assertTrue(result.getSmsMessage() instanceof SmsMessage);
		return result;
	}
	
	public void testlongCodec(CmppSubmitRequestMessage msg)
	{
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
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
	
}
