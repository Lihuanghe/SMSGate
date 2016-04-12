package com.zx.sms.codec.cmpp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsMessage;
import org.marre.sms.SmsPort;
import org.marre.sms.SmsPortAddressedTextMessage;
import org.marre.sms.SmsTextMessage;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;

import com.zx.sms.common.util.CMPPCommonUtil;

public class TestSerializeSmsMessage {

	@Test
	public void test() throws IOException, ClassNotFoundException{
		List<SmsMessage> list = new ArrayList<SmsMessage>();
		list.add(CMPPCommonUtil.buildTextMessage("test"));
		list.add(new SmsPortAddressedTextMessage(SmsPort.NOKIA_CLI_LOGO,SmsPort.NOKIA_IAC,"testporttext"));
		list.add(new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd",50*1024));
		WapSIPush si = new WapSIPush("http://www.baidu.com","baidu");
		SmsMessage siwap = new SmsWapPushMessage(si);
		list.add(siwap);
		WapSLPush sl = new WapSLPush("http://www.baidu.com");
		SmsMessage slwap = new SmsWapPushMessage(sl);
		list.add(slwap);
		
		  ByteArrayOutputStream bos = new ByteArrayOutputStream();     
	        ObjectOutputStream out = new ObjectOutputStream(bos);     
	        out.writeObject(list);
	        byte[] b = bos.toByteArray();
	        System.out.println(b.length);
	        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));     
	        List<SmsMessage> result = (List<SmsMessage>)in.readObject();

	        Assert.assertEquals(((SmsTextMessage)list.get(0)).getText(), ((SmsTextMessage)result.get(0)).getText());
	        
	        Assert.assertEquals(((SmsPortAddressedTextMessage)list.get(1)).getText(), ((SmsPortAddressedTextMessage)result.get(1)).getText());
	        Assert.assertEquals(((SmsPortAddressedTextMessage)list.get(1)).getDcs().getValue(), ((SmsPortAddressedTextMessage)result.get(1)).getDcs().getValue());
	        Assert.assertEquals(((SmsPortAddressedTextMessage)list.get(1)).getDestPort_(), ((SmsPortAddressedTextMessage)result.get(1)).getDestPort_());
	        Assert.assertEquals(((SmsPortAddressedTextMessage)list.get(1)).getOrigPort_(), ((SmsPortAddressedTextMessage)result.get(1)).getOrigPort_());
	        
	        Assert.assertEquals(((SmsMmsNotificationMessage)list.get(2)).getContentLocation_(), ((SmsMmsNotificationMessage)result.get(2)).getContentLocation_());
	        Assert.assertEquals(((SmsMmsNotificationMessage)list.get(2)).getDestPort_(), ((SmsMmsNotificationMessage)result.get(2)).getDestPort_());
	        Assert.assertEquals(((SmsMmsNotificationMessage)list.get(2)).getOrigPort_(), ((SmsMmsNotificationMessage)result.get(2)).getOrigPort_());
	      
	        Assert.assertEquals(((SmsWapPushMessage)list.get(3)).getDestPort_(), ((SmsWapPushMessage)result.get(3)).getDestPort_());
	        Assert.assertEquals(((SmsWapPushMessage)list.get(3)).getOrigPort_(), ((SmsWapPushMessage)result.get(3)).getOrigPort_());
	        Assert.assertEquals(((WapSIPush)((SmsWapPushMessage)list.get(3)).getWbxml()).getUri(), ((WapSIPush)((SmsWapPushMessage)result.get(3)).getWbxml()).getUri());
	        
	        Assert.assertEquals(((SmsWapPushMessage)list.get(4)).getDestPort_(), ((SmsWapPushMessage)result.get(4)).getDestPort_());
	        Assert.assertEquals(((SmsWapPushMessage)list.get(4)).getOrigPort_(), ((SmsWapPushMessage)result.get(4)).getOrigPort_());
	        Assert.assertEquals(((WapSLPush)((SmsWapPushMessage)list.get(4)).getWbxml()).getUri(), ((WapSLPush)((SmsWapPushMessage)result.get(4)).getWbxml()).getUri());
	}
}
