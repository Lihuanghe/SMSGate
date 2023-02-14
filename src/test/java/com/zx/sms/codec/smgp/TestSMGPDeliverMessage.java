package com.zx.sms.codec.smgp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractSMGPTestMessageCodec;
import com.zx.sms.codec.smgp.msg.MsgId;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPReportData;

public class TestSMGPDeliverMessage extends AbstractSMGPTestMessageCodec<SMGPDeliverMessage>{

	@Test
	public void testascii() {
		SMGPDeliverMessage msg = new SMGPDeliverMessage();
		msg.setDestTermId("13800138000");
		msg.setLinkId("1023rsd");
		msg.setMsgContent("12345678901AssBC56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abcdefgh");
		msg.setMsgId(new MsgId());
		msg.setSrcTermId("10000988");
		//140字纯文字符，拆分为1条
		Assert.assertEquals(1,testlongCodec(msg));
		//超过140字纯文字符，拆分为2条
		msg.setMsgContent("12345678901AssBC56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abcdefgh"+"a");
		Assert.assertEquals(2,testlongCodec(msg));
	}
	
	@Test
	public void test1() {
		SMGPDeliverMessage msg = new SMGPDeliverMessage();
		msg.setDestTermId("13800138000");
		msg.setLinkId("1023rsd");
		msg.setMsgContent("第一种：通过注解@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作");
		msg.setMsgId(new MsgId());
		msg.setSrcTermId("10000988");
		testlongCodec(msg);
	}
	
	@Test
	public void test2() {
		SMGPDeliverMessage msg = new SMGPDeliverMessage();
		msg.setDestTermId("13800138000");
		msg.setLinkId("1023rsd");
		msg.setMsgContent("第一种：通过注解@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作,第一种：通过注解@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作");
		msg.setMsgId(new MsgId());
		msg.setSrcTermId("10000988");
		Assert.assertEquals(2,testlongCodec(msg));
	}
	
	@Test
	public void testReport() {
		dotestReport("【圆通】快递已");
		dotestReport("0【圆通】快递已");
		dotestReport("04【圆通】快递已");
		dotestReport("040【圆通】快递已");
		dotestReport("a【圆通】快递已");
		dotestReport("0b【圆通】快递已");
		dotestReport("abc【圆通】快递已");
		dotestReport("040【圆通】快递已");
		dotestReport("081mmm993");
		dotestReport("0asdf");
		dotestReport("08asdfa");
		dotestReport("081mmm993");
		dotestReport("");
		dotestReport(null);
	}
	
	
	private void dotestReport(String text) {
		SMGPDeliverMessage msg = new SMGPDeliverMessage();
		msg.setDestTermId("13800138000");
		msg.setLinkId("1023rsd");
		SMGPReportData report  = new SMGPReportData();
		report.setStat("Delivd");
		report.setDlvrd("111");
		report.setDoneTime("20180703000111");
		report.setErr("000");
		report.setMsgId(new MsgId());
		report.setSub("asf");
		report.setSubTime("20180703000111");
		report.setTxt(text);
		msg.setReport(report);
		msg.setMsgId(new MsgId());
		msg.setSrcTermId("10000988");
		testlongCodec(msg);
	}
	
	public int testlongCodec(SMGPDeliverMessage msg)
	{
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		int frameLength = 0;
	    while(buf!=null){
	    	frameLength++;
	    	ByteBuf copy = buf.copy();
	    	copybuf.writeBytes(copy);
	    	copy.release();
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readInt());
			Assert.assertEquals(msg.getCommandId(), buf.readInt());
			
			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    SMGPDeliverMessage result = decode(copybuf);
		Assert.assertNotNull(result);
		System.out.println(result);
		if(!result.isReport())
			Assert.assertNotNull(result.getUniqueLongMsgId().getId());
		if (msg.isReport()) {
			Assert.assertEquals(msg.getReport().getStat(), result.getReport().getStat());
			Assert.assertEquals(msg.getReport().getTxt(), result.getReport().getTxt());
		} else {
			Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
			Assert.assertEquals(msg.getSrcTermId(), result.getSrcTermId());
			Assert.assertEquals(msg.getDestTermId(), result.getDestTermId());
			Assert.assertEquals(msg.getRecvTime(), result.getRecvTime());
		}
		Assert.assertEquals(msg.getSrcTermId(), result.getSrcTermId());
		return frameLength;
	}
	
}
