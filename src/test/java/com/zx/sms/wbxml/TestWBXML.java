package com.zx.sms.wbxml;

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

}
