package com.zx.sms.wbxml;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.marre.sms.SmsUserData;
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
		byte[] b = Hex.decodeHex("6211768460c551b55f887d27602530028bf75e2e5e2e62113000".toCharArray());
		System.out.println(new String(b,Charset.forName("ISO-10646-UCS-2")));
	}
}
