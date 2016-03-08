package com.zx.sms.wbxml;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.marre.sms.SmsAddress;
import org.marre.sms.SmsPdu;
import org.marre.sms.SmsTextMessage;
import org.marre.sms.SmsUserData;
import org.marre.sms.transport.gsm.GsmEncoder;
import org.marre.sms.transport.gsm.commands.PduSendMessageReq;
import org.marre.sms.transport.gsm.commands.PduSendMessageRsp;
import org.marre.wap.push.SmsMmsNotificationMessage;


public class TestWBXML {

	@Test
	public void testdecoder() throws Exception{
				
		SmsMmsNotificationMessage msg = new SmsMmsNotificationMessage("http://www.baidu.com",50*1024L);
		SmsUserData sms = msg.getUserData();
		byte[] binary = sms.getData();
		
		
		System.out.println(Hex.encodeHexString(binary));
	}

	@Test
	public void testerror() throws Exception{
		 
		 System.out.println(Math.ceil((54 * 7 ) / 8.0));
		byte[] b = Hex.decodeHex("6211768460c551b55f887d27602530028bf75e2e5e2e62113000".toCharArray());
		System.out.println(new String(b,Charset.forName("ISO-10646-UCS-2")));
		SmsTextMessage msg = new SmsTextMessage("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		SmsAddress dest = new SmsAddress("abc");
		SmsAddress sender = new SmsAddress("def");
		SmsPdu[] msgPdu = msg.getPdus();
        for (SmsPdu aMsgPdu : msgPdu) {
            byte[] data = GsmEncoder.encodePdu(aMsgPdu, dest, sender);
            System.out.println(Hex.encodeHexString(data));
        }
	}
}
