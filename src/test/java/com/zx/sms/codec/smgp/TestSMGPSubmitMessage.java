package com.zx.sms.codec.smgp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractSMGPTestMessageCodec;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;

public class TestSMGPSubmitMessage extends AbstractSMGPTestMessageCodec<SMGPSubmitMessage> {

	@Test
	public void test1() {
		SMGPSubmitMessage msg = new SMGPSubmitMessage();
		msg.setDestTermIdArray(new String[]{"13800138000","13800138001","13800138002","13800138003"});
		msg.setLinkId("1023rsd");
		msg.setMsgContent("第一种：通过注解@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作");
		msg.setSrcTermId("10086988");
		msg.setMsgSrc("901988");
		test0(msg);
	}
	
	@Test
	public void test2() {
		SMGPSubmitMessage msg = new SMGPSubmitMessage();
		msg.setDestTermIdArray(new String[]{"13800138000","13800138001","13800138002","13800138003"});
		msg.setLinkId("1023rsd");
		msg.setMsgContent("第一种：通过注解@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作,第一种：通过注解@PostConstruct 和 @PreDestroy 方法 实现初始化和销毁bean之前进行的操作");
		msg.setSrcTermId("10086988");
		msg.setMsgSrc("901988");
		testlongCodec(msg);
	}
	
	public void testlongCodec(SMGPSubmitMessage msg)
	{
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
			
			
	    	copybuf.writeBytes(buf.copy());
			int length = buf.readableBytes();
			
			Assert.assertEquals(length, buf.readUnsignedInt());
			Assert.assertEquals(msg.getCommandId(), buf.readUnsignedInt());
			

			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    SMGPSubmitMessage result = decode(copybuf);
		Assert.assertNotNull(result);
		System.out.println(result);
		
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		Assert.assertEquals(msg.getSrcTermId(), result.getSrcTermId());
		Assert.assertArrayEquals(msg.getDestTermIdArray(), result.getDestTermIdArray());
		Assert.assertEquals(msg.getSrcTermId(), result.getSrcTermId());
	}
	
	private void test0(SMGPSubmitMessage msg) {

		System.out.println(msg);
		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();

		int length = buf.readableBytes();
		
		buf.release();
		SMGPSubmitMessage result = decode(newbuf);
		System.out.println(result);
		Assert.assertEquals(msg.getSequenceNo(), result.getSequenceNo());
		
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		Assert.assertEquals(msg.getSrcTermId(), result.getSrcTermId());
		Assert.assertArrayEquals(msg.getDestTermIdArray(), result.getDestTermIdArray());

		Assert.assertEquals(msg.getSrcTermId(), result.getSrcTermId());

	}
}
