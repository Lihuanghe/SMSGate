package com.zx.sms.codec.sgip;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmsDcs;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.AbstractSGIPTestMessageCodec;
import com.zx.sms.codec.sgip12.msg.SgipDefaultMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipTraceInfo;
import com.zx.sms.codec.sgip12.msg.SgipTraceRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipTraceResponseMessage;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.common.util.SequenceNumber;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestSGIPcodec extends AbstractSGIPTestMessageCodec<SgipDefaultMessage> {

	@Test
	public void test() throws DecoderException{
		
		System.out.println(new SmsDcs((byte)8).getAlphabet());
		System.out.println(new SequenceNumber());
		ByteBuf buffer = Unpooled.wrappedBuffer(Hex.decodeHex("000000a8000000030000000011f333f26ebb10dd31303635353931323530323800000000000000000000000000000000000000000000000000000000000001383631383635373131383633360000000000000000363131313800000000000000000000020000000000000000000000000000090000000000000000000000000000000000000000000000000000000000000000010000000000000004323333610000000000000000".toCharArray()));
		SgipSubmitRequestMessage msg = (SgipSubmitRequestMessage)decode(buffer);
		 Assert.assertNotNull(msg);
		 System.out.println(msg);
		 Assert.assertEquals("106559125028", msg.getSpnumber());
		 Assert.assertEquals("233a", msg.getMsgContent());
	}
	
	
	@Test
	public void testTraceRequest () throws DecoderException{
		SgipTraceRequestMessage tracereq = new SgipTraceRequestMessage();
		tracereq.setSequenceId(new SequenceNumber(new MsgId()));
		System.out.println(tracereq.getSequenceId());
		tracereq.setUsernumber("8613800138000");
		tracereq.setReserve("00000000");
		testlongCodec(tracereq);
	}
	
	@Test
	public void testTraceResponse () throws DecoderException{
		SgipTraceResponseMessage traceres = new SgipTraceResponseMessage();

		SgipTraceInfo[] traceInfos = new SgipTraceInfo[2] ;
		SgipTraceInfo item1 = new SgipTraceInfo();
		item1.setResult(0);
		item1.setNodeId("100001");
		item1.setReceiveTime("230810235959");
		item1.setSendTime("230810235959");
		item1.setReserve("12345678");
		
		SgipTraceInfo item2 = new SgipTraceInfo();
		item2.setResult(1);
		item2.setNodeId("100002");
		item2.setReceiveTime("230810235959");
		item2.setSendTime("230810235959");
		item2.setReserve("87654321");
		traceInfos[0] = item1;
		traceInfos[1] = item2;
		traceres.setTraceInfos(traceInfos);
		testlongCodec(traceres);
	}
	
	@Test
	public void testSequenceNumber() throws InterruptedException {
		for(int i=0;i<10;i++) {
			SequenceNumber seq = new SequenceNumber(new MsgId());
			 System.out.println("==="+seq.toString());
			 Thread.sleep(1000);
			Assert.assertEquals(seq.toString(),new SequenceNumber(seq.toString()).toString());
			byte[] bytes = DefaultSequenceNumberUtil.sequenceN2Bytes(new SequenceNumber(seq.toString()));
			SequenceNumber tTo = DefaultSequenceNumberUtil.bytes2SequenceN(bytes);
			Assert.assertEquals(seq.toString(), tTo.toString());
		}

	}
	@Test
	public void testMTLongMsg() {
		SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage();
		requestMessage.setSpnumber("10000");
		requestMessage.setUsernumber("13800138000");
		requestMessage.setMsgContent("12345678901AssBC56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abcdefgh");
		requestMessage.setReportflag((short)0);
		//140字纯文字符，拆分为1条
		Assert.assertEquals(1,testlongCodec(requestMessage));
		requestMessage.setMsgContent(requestMessage.getMsgContent()+"1");
		//超过140字纯文字符，拆分为2条
		Assert.assertEquals(2,testlongCodec(requestMessage));
		//70字带汉文字符，拆分为1条
		requestMessage.setMsgContent("12345678901AssBC56789012345678901234567890123456789012345678901234567中");
		Assert.assertEquals(1,testlongCodec(requestMessage));
		
		requestMessage.setMsgContent(requestMessage.getMsgContent()+"1");
		//超过140字纯文字符，拆分为2条
		Assert.assertEquals(2,testlongCodec(requestMessage));
	}
	
	@Test
	public void testMOLongMsg() {
		SgipDeliverRequestMessage requestMessage = new SgipDeliverRequestMessage();
		requestMessage.setSpnumber("10000");
		requestMessage.setUsernumber("13800138000");
		
		requestMessage.setMsgContent("12345678901AssBC56789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abcdefgh");
		//140字纯文字符，拆分为1条
		Assert.assertEquals(1,testlongCodec(requestMessage));
		requestMessage.setMsgContent(requestMessage.getMsgContent()+"1");
		//超过140字纯文字符，拆分为2条
		Assert.assertEquals(2,testlongCodec(requestMessage));
		//70字带汉文字符，拆分为1条
		requestMessage.setMsgContent("12345678901AssBC56789012345678901234567890123456789012345678901234567中");
		Assert.assertEquals(1,testlongCodec(requestMessage));
		
		requestMessage.setMsgContent(requestMessage.getMsgContent()+"1");
		//超过140字纯文字符，拆分为2条
		Assert.assertEquals(2,testlongCodec(requestMessage));
	}
	
	
	public int testlongCodec(SgipDefaultMessage msg)
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
			Assert.assertEquals(msg.getHeader().getCommandId(), buf.readInt());
			
			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    SgipDefaultMessage result = decode(copybuf);
		Assert.assertNotNull(result);
		System.out.println(result);
		if(result instanceof LongSMSMessage)
			Assert.assertNotNull(((LongSMSMessage)result).getUniqueLongMsgId().getId());
		if(msg instanceof SgipSubmitRequestMessage) {
			SgipSubmitRequestMessage mt = (SgipSubmitRequestMessage)msg;
			Assert.assertEquals(mt.getMsgContent(), ((SgipSubmitRequestMessage)result).getMsgContent());
		}else if(msg instanceof SgipDeliverRequestMessage){
			SgipDeliverRequestMessage mo = (SgipDeliverRequestMessage)msg;
			Assert.assertEquals(mo.getMsgContent(), ((SgipDeliverRequestMessage)result).getMsgContent());
		}
		
		return frameLength;
	}
}
