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

		test0(msg);

	}

	private void test0(CmppDeliverRequestMessage msg) {

		
		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();

		int length = buf.readableBytes();
		
		Assert.assertEquals(length, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());


		CmppDeliverRequestMessage result = decode(newbuf);

		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());
		if (msg.isReport()) {
			Assert.assertEquals(msg.getReportRequestMessage().getSmscSequence(), result.getReportRequestMessage().getSmscSequence());
		} else {
			Assert.assertNotNull(result.getUniqueLongMsgId().getId());
			Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		}
		Assert.assertEquals(msg.getSrcterminalId(), result.getSrcterminalId());

	}

	private CmppDeliverRequestMessage createTestReq(String content) {

		Header header = new DefaultHeader();
		// 取时间，用来查看编码解码时间

		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage(header);
		msg.setDestId("10658909");
		msg.setLinkid("0000");
		// 70个汉字
		msg.setMsgContent(content);
		msg.setMsgId(new MsgId());
		msg.setServiceid("10006");
		msg.setSrcterminalId("13800138000");
		msg.setSrcterminalType((short) 1);
		header.setSequenceId((int)System.nanoTime());
		return msg;
	}
	


	@Test
	public void testchinesecode()
	{
		testlongCodec("1234567890123456789中01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" );
	}

	@Test
	public void testASCIIcode()
	{
		testlongCodec("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abcdefghij");
	}
	
	
	public void testlongCodec(String content)
	{
		CmppDeliverRequestMessage msg = createTestReq(content);
		
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
			

			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    CmppDeliverRequestMessage result = decode(copybuf);
		
		System.out.println(result.getMsgContent());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		Assert.assertNotNull(result.getUniqueLongMsgId().getId());
	}
}
