package com.zx.sms.concat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmsConcatMessage;
import com.chinamobile.cmos.sms.SmsException;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.connect.manager.TestConstants;
public class TestSmsConcatMessage {

	
	@Test
	public void test1() throws SmsException {
		//1分钟内，同一个号码小于255条长短信，生成的长短信seqreqNo不能重复
		String text = TestConstants.testSmsContent;
		String[] phones = new String[] {"13805138000","13805138001","13805138002"};
		Map<String ,Integer> checkMap = new HashMap();
		boolean checkExists = false;
		List<CmppSubmitRequestMessage> smsS = new ArrayList<CmppSubmitRequestMessage>();
		for(int i = 0;i<256*phones.length;i++) {
			SmsTextMessage sms = new SmsTextMessage(text);
			String phone = phones[i%phones.length];
			CmppSubmitRequestMessage submitMsg = CmppSubmitRequestMessage.create(phone, "10000", text);
			smsS.add(submitMsg);
		}
		
		Collections.shuffle(smsS);
		
		for(CmppSubmitRequestMessage submitMsg:smsS) {
			//如果是长短信类型，记录序列号key
			String phone = submitMsg.getDestterminalId()[0];
			SmsMessage smsMessage = submitMsg.getSmsMessage();
			
			if(smsMessage instanceof SmsConcatMessage) {
				((SmsConcatMessage)smsMessage).setSeqNoKey(submitMsg.getSrcIdAndDestId());
			}
			
			List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(smsMessage);
			Integer pkseq = Integer.valueOf(frameList.get(0).getPkseq());
			Integer oldpkseq = checkMap.putIfAbsent(phone+pkseq,pkseq);
			if(oldpkseq!=null) {
				checkExists = true;
				System.out.println(phone+"=="+pkseq+"==="+oldpkseq);
				break;
			}
		}
		Assert.assertTrue(!checkExists);
	}
	
	@Test
	public void  testRemoveConcatUDHie() throws DecoderException {
		byte[] contents = Hex.decodeHex("050003870301a1f208".toCharArray());
		byte[] last = LongMessageFrameHolder.INS.removeConcatUDHie(contents);
		System.out.println(new String(  Hex.encodeHex(last)));
		Assert.assertArrayEquals(Hex.decodeHex("a1f208".toCharArray()), last);
		
		contents = Hex.decodeHex("0b0003870301050401020304a1f208".toCharArray());
		last = LongMessageFrameHolder.INS.removeConcatUDHie(contents);
		System.out.println(new String(  Hex.encodeHex(last)));
		Assert.assertArrayEquals(Hex.decodeHex("06050401020304a1f208".toCharArray()), last);
	
		contents = Hex.decodeHex("0c080400870301050401020304a1f208".toCharArray());
		last = LongMessageFrameHolder.INS.removeConcatUDHie(contents);
		System.out.println(new String(  Hex.encodeHex(last)));
		Assert.assertArrayEquals(Hex.decodeHex("06050401020304a1f208".toCharArray()), last);
	
	}
}
