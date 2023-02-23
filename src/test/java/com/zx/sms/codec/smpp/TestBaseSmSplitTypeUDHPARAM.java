package com.zx.sms.codec.smpp;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.chinamobile.cmos.wap.push.SmsWapPushMessage;
import com.chinamobile.cmos.wap.push.WapSLPush;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.AbstractSMPPTestMessageCodec;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestBaseSmSplitTypeUDHPARAM extends AbstractSMPPTestMessageCodec<BaseSm> {
    
	//设置SMPP端口默认编码为ASCII
	protected EndpointEntity buildEndpointEntity() {
		SMPPClientEndpointEntity en = new SMPPClientEndpointEntity();
		en.setId("Test");
		en.setInterfaceVersion((byte)52);
		en.setSplitType(SmppSplitType.UDHPARAM);
		return en;
	}
	
	@Test
	public void testASCIIcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	pdu.setSmsMsg(new SmsTextMessage("^{}\\[~]|1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", new SmppSmsDcs((byte)0,SmsAlphabet.ASCII)));
     	testlongCodec(pdu);
	}
	
	@Test
	public void testcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	pdu.setSmsMsg("中^{}\\[~]|1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
     	testlongCodec(pdu);
	}
	
	private void testlongCodec(BaseSm msg)
	{
		channel().writeOutbound(msg);
		ByteBuf buf =(ByteBuf)channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
	    while(buf!=null){
	    	copybuf.writeBytes(buf);
			int length = buf.readableBytes();   
			buf =(ByteBuf)channel().readOutbound();
	    }
	    
	    BaseSm result = decode(copybuf);
		
		System.out.println(result);
		Assert.assertNotNull(((LongSMSMessage)result).getUniqueLongMsgId().toString());
		Assert.assertEquals(((SmsTextMessage)msg.getSmsMessage()).getText(), ((SmsTextMessage)result.getSmsMessage()).getText());
	}
	@Test
	public void testWapcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    		
    	String origin = "http://www.baidu.com?123这个链接的访问记录，跟普通链ssage(content,SmsDcs.getGeneralDataC"
				+ RandomUtils.nextInt();
		origin = RandomUtils.nextBoolean() ? origin : (origin + origin);
		origin = RandomUtils.nextBoolean() ? origin : (origin + origin);
		WapSLPush sl = new WapSLPush(origin);
		SmsMessage wap = new SmsWapPushMessage(sl);
		pdu.setSmsMsg(wap);
	     
		channel().writeOutbound(pdu);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		while (buf != null) {
			copybuf.writeBytes(buf);
			int length = buf.readableBytes();
			buf = (ByteBuf) channel().readOutbound();
		}

		BaseSm result = decode(copybuf);

		System.out.println(result);
		Assert.assertNotNull(((LongSMSMessage) result).getUniqueLongMsgId().getId());
		System.out.println(((LongSMSMessage) result).getUniqueLongMsgId());
		Assert.assertEquals(origin, ((WapSLPush) ((SmsWapPushMessage) result.getSmsMessage()).getWbxml()).getUri());
		System.out.println();
	}
}
