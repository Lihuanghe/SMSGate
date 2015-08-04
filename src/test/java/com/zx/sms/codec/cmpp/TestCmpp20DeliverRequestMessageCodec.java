package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.common.util.MsgId;

public class TestCmpp20DeliverRequestMessageCodec extends AbstractTestMessageCodec<CmppDeliverRequestMessage>{
	private static final Logger logger = LoggerFactory.getLogger(TestCmpp20DeliverRequestMessageCodec.class);

	@Override
	protected int getVersion(){
		return 0x20;
	}


	@Test
	public void testCodec() {

		CmppDeliverRequestMessage msg = createTestReq("ad3 中");

		test0(msg);
	}

	@Test
	public void testReportCodec() {
		CmppDeliverRequestMessage msg = createTestReq("k k k ");
		CmppReportRequestMessage reportRequestMessage = new CmppReportRequestMessage();
		reportRequestMessage.setSmscSequence(0x1234L);
		reportRequestMessage.setMsgId(new MsgId());
		reportRequestMessage.setDestterminalId("13800138000");
		reportRequestMessage.setStat("9876");
		msg.setReportRequestMessage(reportRequestMessage);
		msg.setRegisteredDelivery((short) 1);

		test0(msg);

	}

	private void test0(CmppDeliverRequestMessage msg) {

		
		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();

		int length = buf.readableBytes();
		
		Assert.assertEquals(length, buf.readUnsignedInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readUnsignedInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readUnsignedInt());


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
		msg.setRegisteredDelivery((short) 0);
		msg.setServiceid("10086");
		msg.setSrcterminalId("13800138000");
		msg.setSrcterminalType((short) 1);
		header.setSequenceId(System.nanoTime() & 0x7fffffff);
		return msg;
	}
	
	@Test
	public void testchinesecode()
	{
		String str = "1234567890123456789中01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		testlongCodecNoSupport(str);
		testlongCodec(str);
	}

	@Test
	public void testASCIIcode()
	{
		String str = "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		testlongCodecNoSupport(str);
		testlongCodec(str);
	}
	
	
	public void testlongCodecNoSupport(String content)
	{
		CmppDeliverRequestMessage msg = createTestReq(content);
		
		msg.setSupportLongMsg(false);
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
		ch.writeInbound(copybuf);
	
	    CmppDeliverRequestMessage result = ch.readInbound();
	    StringBuilder sb = new StringBuilder();
	    while(result!=null){
	    	System.out.println(result.getMsgContent());
	    	sb.append(result.getMsgContent().substring(5));
	    	result = ch.readInbound();
	    }
	    Assert.assertEquals(msg.getMsgContent(), sb.toString());
	}
	
	public void testlongCodec(String content)
	{
		CmppDeliverRequestMessage msg = createTestReq(content);
		
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
	    
	    CmppDeliverRequestMessage result = decode(copybuf);
		
		System.out.println(result.getMsgContent());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
	}
}
