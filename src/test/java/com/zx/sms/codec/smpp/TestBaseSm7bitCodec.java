package com.zx.sms.codec.smpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsPduUtil;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.AbstractSMPPTestMessageCodec;
import com.zx.sms.codec.smpp.android.gsm.GsmAlphabet;
import com.zx.sms.codec.smpp.msg.BaseSm;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestBaseSm7bitCodec extends AbstractSMPPTestMessageCodec<BaseSm> {
    
	//设置SMPP端口默认编码为GSM
	protected EndpointEntity buildEndpointEntity() {
		SMPPClientEndpointEntity en = new SMPPClientEndpointEntity();
		en.setId("Test");
		en.setDefauteSmsAlphabet(SmsAlphabet.GSM);
		en.setUse7bitPack(true);
		return en;
	}
	
	@Test
	public void testGSMcode()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	pdu.setSmsMsg(new SmsTextMessage("^{}\\[~]|1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", new SmppSmsDcs((byte)0,SmsAlphabet.GSM)));
     	testlongCodec(pdu);
	}
	
	@Test
	public void testGSMcode2()
	{
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	String text = "1@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà012345678901234567890012^567890a";
    	pdu.setSmsMsg(new SmsTextMessage(text, new SmppSmsDcs((byte)0,SmsAlphabet.GSM)));
     	testlongCodec(pdu);
	}

	@Test
	public void testseptetAll() throws Exception {
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	pdu.setSmsMsg(new SmsTextMessage(shuffle(SmsPduUtil.gsmstr), new SmppSmsDcs((byte)0,SmsAlphabet.GSM)));
    	testlongCodec(pdu);
	}
	
	@Test
	public void testseptetAllLong() throws Exception {
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	pdu.setSmsMsg(new SmsTextMessage(shuffle(SmsPduUtil.gsmstr+SmsPduUtil.gsmstr), new SmppSmsDcs((byte)0,SmsAlphabet.GSM)));
    	testlongCodec(pdu);
	}
	@Test
	public void testseptetShort() throws Exception {
		SubmitSm pdu = new SubmitSm();
    	pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
    	pdu.setSourceAddress(new Address((byte)0,(byte)0,"10658987"));
    	pdu.setSmsMsg(new SmsTextMessage("12345FGHIJKLMNOPQRSTUV69988", new SmppSmsDcs((byte)0,SmsAlphabet.GSM)));
    	testlongCodec(pdu);
	}
	
	private String shuffle(String str) {
		char[] char_arr = str.toCharArray();
		List<Character> char_list = new ArrayList<Character>();
		for (char t : char_arr)
			char_list.add(t);

		Collections.shuffle(char_list);
		StringBuffer sb = new StringBuffer();
		for (Object c : char_list) {
			sb.append(c.toString());
		}

		return sb.toString();
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
