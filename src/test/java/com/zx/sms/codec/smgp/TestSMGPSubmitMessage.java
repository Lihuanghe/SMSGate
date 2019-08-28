package com.zx.sms.codec.smgp;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.AbstractSMGPTestMessageCodec;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;

public class TestSMGPSubmitMessage extends AbstractSMGPTestMessageCodec<SMGPSubmitMessage> {

	protected int getversion() {
		return 0x30;
	}
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
	
	@Test
	// 网络反馈的一个报错的smgp报文
	public void testerr() {
		byte[] arr = new byte[] {0, 0, 0, -17, 0, 0, 0, 2, 18, 61, 108, 18, 6, 1, 2, 100, 101, 102, 97, 117, 108, 116, 0, 0, 0, 48, 49, 48, 48, 48, 48, 48, 48, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 48, 54, 53, 57, 50, 48, 53, 50, 51, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 49, 56, 48, 52, 54, 50, 55, 57, 48, 56, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 90, 5, 0, 3, 52, 1, 1, 48, 16, 78, 45, 83, -97, -108, -10, -120, 76, 48, 17, 92, 10, -115, 53, 118, -124, 91, -94, 98, 55, 0, 44, 0, 32, 98, 17, -120, 76, 95, -82, 79, -31, -108, -10, -120, 76, 87, 48, 87, 64, 78, 58, -1, 26, -1, 12, 78, 45, 83, -97, -108, -10, -120, 76, 122, -19, -117, -38, 78, 58, 96, -88, 103, 13, 82, -95, -1, 12, 121, 93, 96, -88, 117, 31, 109, 59, 97, 9, 95, -21, -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
		ByteBuf data = Unpooled.wrappedBuffer(arr);
		try {
			SMGPSubmitMessage result = decode(data);
		}catch(Exception ex) {
			Assert.assertTrue(ex instanceof DecoderException);
		}
		
		
	}
}
