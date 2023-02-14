package com.zx.sms.codec.smpp;

import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.AbstractSMPPTestMessageCodec;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestBaseSmDefaultASCIICodec extends AbstractSMPPTestMessageCodec<BaseSm> {
    
	private String gsmstr = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|€";

	
	//设置SMPP端口默认编码为ASCII
	protected EndpointEntity buildEndpointEntity() {
		SMPPClientEndpointEntity en = new SMPPClientEndpointEntity();
		en.setDefauteSmsAlphabet(SmsAlphabet.ASCII);
		en.setId("Test");
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
}
