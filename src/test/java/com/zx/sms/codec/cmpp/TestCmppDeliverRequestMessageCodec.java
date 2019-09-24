package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsMessage;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.common.util.MsgId;

public class TestCmppDeliverRequestMessageCodec extends AbstractTestMessageCodec<CmppDeliverRequestMessage>{
	private static final Logger logger = LoggerFactory.getLogger(TestCmppDeliverRequestMessageCodec.class);

	@Test
	public void testperformance() {
		int i = 0;
		int l = 100000;
		CmppDeliverRequestMessage msg = createTestReq("ad3 中");
		//先预热
		for(;i< 1000;i++){
			decode(encode(msg));
		}
		//开始计时
		
		long start = System.currentTimeMillis();
		
		for(i =0 ;i< l;i++){
			decode(encode(msg));
		}
		long end = System.currentTimeMillis();
		System.out.print((end - start)*1000000/l);
		System.out.println("ns");
		//assert(msg.getMsgContent().equals(result.getMsgContent()));
	}

	@Test
	public void testCodec() {

		CmppDeliverRequestMessage msg = createTestReq("ad3 中");

		test0(msg);
	}

	@Test
	public void testReportCodec() {
		CmppDeliverRequestMessage msg = createTestReq("k k k ");
		msg.setMsgContent((SmsMessage)null);
		CmppReportRequestMessage reportRequestMessage = new CmppReportRequestMessage();
		reportRequestMessage.setSmscSequence(0x1234L);
		reportRequestMessage.setMsgId(new MsgId());
		reportRequestMessage.setDestterminalId("13800138000");
		reportRequestMessage.setStat("9876");
		msg.setReportRequestMessage(reportRequestMessage);
		test0(msg);
	}

	private void test0(CmppDeliverRequestMessage msg) {

		
		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();

		int length = buf.readableBytes();
		
		Assert.assertEquals(length, buf.readUnsignedInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());

		buf.release();
		CmppDeliverRequestMessage result = decode(newbuf);

		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());
		if (msg.isReport()) {
			Assert.assertEquals(msg.getReportRequestMessage().getSmscSequence(), result.getReportRequestMessage().getSmscSequence());
		} else {
			Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		}
		Assert.assertEquals(msg.getSrcterminalId(), result.getSrcterminalId());

	}

	private CmppDeliverRequestMessage createTestReq(String content) {

		Header header = new DefaultHeader();
		// 取时间，用来查看编码解码时间

		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage(header);
		msg.setDestId("13800138000");
		msg.setLinkid("0000");
		// 70个汉字
		msg.setMsgContent(content);
		msg.setMsgId(new MsgId());
		msg.setServiceid("10086");
		msg.setSrcterminalId("13800138000");
		msg.setSrcterminalType((short) 1);
		header.setSequenceId(System.nanoTime() & 0x7fffffff);
		return msg;
	}
	
	@Test
	public void testchinesecode()
	{
		CmppDeliverRequestMessage msg = createTestReq("1234567890123456789中01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" );
		testlongCodec(msg);
	}

	@Test
	public void testASCIIcode()
	{
		CmppDeliverRequestMessage msg = createTestReq("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		testlongCodec(msg);
	}
	
	@Test
	public void testSLPUSH()
	{
		CmppDeliverRequestMessage msg = createTestReq("");
		WapSLPush sl = new WapSLPush("http://www.baidu.com");
		SmsMessage wap = new SmsWapPushMessage(sl);
		msg.setMsgContent(wap);
		CmppDeliverRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage)result.getSmsMessage();
		WapSLPush actsl = (WapSLPush)smsmsg.getWbxml();
		Assert.assertEquals(sl.getUri(), actsl.getUri());
	}
	
	@Test
	public void testSIPUSH()
	{
		CmppDeliverRequestMessage msg = createTestReq("");
		WapSIPush si = new WapSIPush("http://www.baidu.com","baidu");
		SmsMessage wap = new SmsWapPushMessage(si);
		msg.setMsgContent(wap);
		CmppDeliverRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage)result.getSmsMessage();
		WapSIPush actsi = (WapSIPush)smsmsg.getWbxml();
		Assert.assertEquals(si.getUri(), actsi.getUri());
		Assert.assertEquals(si.getMessage(), actsi.getMessage());
	}
	
	@Test
	public void testMMSPUSH()
	{
		CmppDeliverRequestMessage msg = createTestReq("");
		SmsMmsNotificationMessage mms = new SmsMmsNotificationMessage("https://www.baidu.com/s?wd=SMPPv3.4%20%E9%95%BF%E7%9F%AD%E4%BF%A1&rsv_spt=1&rsv_iqid=0xdd4666100001e74c&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=0&oq=SMPPv%2526lt%253B.4%2520ton%2520npi&rsv_t=50fdNrphqry%2FYfHh29wvp8KzJ9ogqigiPr33FT%2FpcGQu6X34vByQNu4O%2FLNZgIiXdd16&inputT=3203&rsv_pq=d576ead9000016eb&rsv_sug3=60&rsv_sug1=15&rsv_sug7=000&rsv_sug2=0&rsv_sug4=3937&rsv_sug=1",50*1024);
		msg.setMsgContent(mms);
		mms.setTransactionId("ABC");
		CmppDeliverRequestMessage result =testWapCodec(msg);
		SmsMmsNotificationMessage smsmsg = (SmsMmsNotificationMessage)result.getSmsMessage();
		Assert.assertEquals(smsmsg.getContentLocation_(), smsmsg.getContentLocation_());
	}
	
	public CmppDeliverRequestMessage testWapCodec(CmppDeliverRequestMessage msg)
	{
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
			
			
	    	copybuf.writeBytes(buf.copy());
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readUnsignedInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
			

			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    CmppDeliverRequestMessage result = decode(copybuf);
		System.out.println(result);
		Assert.assertTrue(result.getSmsMessage() instanceof SmsMessage);
		return result;
	}
	
	public void testlongCodec(CmppDeliverRequestMessage msg)
	{
		
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
			
			
	    	copybuf.writeBytes(buf.copy());
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readUnsignedInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
			

			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    CmppDeliverRequestMessage result = decode(copybuf);
		
		System.out.println(result.getMsgContent());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
}
