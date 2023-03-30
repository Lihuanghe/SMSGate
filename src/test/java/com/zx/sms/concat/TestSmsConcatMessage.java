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

import com.chinamobile.cmos.sms.AbstractSmsDcs;
import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.chinamobile.cmos.sms.SmsConcatMessage;
import com.chinamobile.cmos.sms.SmsDcs;
import com.chinamobile.cmos.sms.SmsException;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsMsgClass;
import com.chinamobile.cmos.sms.SmsPduUtil;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.connect.manager.SignatureType;
import com.zx.sms.connect.manager.TestConstants;

public class TestSmsConcatMessage {

	@Test
	public void test1() throws SmsException {

		// 1分钟内，同一个号码小于255条长短信，生成的长短信seqreqNo不能重复
		String text = TestConstants.testSmsContent;

		String[] phones = new String[] { "13805138000", "13805138001", "13805138002" };
		Map<String, Integer> checkMap = new HashMap();
		boolean checkExists = false;
		List<CmppSubmitRequestMessage> smsS = new ArrayList<CmppSubmitRequestMessage>();
		for (int i = 0; i < 256 * phones.length; i++) {
			SmsTextMessage sms = new SmsTextMessage(text);
			String phone = phones[i % phones.length];
			CmppSubmitRequestMessage submitMsg = CmppSubmitRequestMessage.create(phone, "10000", text);
			smsS.add(submitMsg);
		}

		Collections.shuffle(smsS);

		for (CmppSubmitRequestMessage submitMsg : smsS) {
			// 如果是长短信类型，记录序列号key
			String phone = submitMsg.getDestterminalId()[0];
			SmsMessage smsMessage = submitMsg.getSmsMessage();

			if (smsMessage instanceof SmsConcatMessage) {
				((SmsConcatMessage) smsMessage).setSeqNoKey(submitMsg.getSrcIdAndDestId());
			}

			List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(smsMessage);
			Integer pkseq = Integer.valueOf(frameList.get(0).getPkseq());
			Integer oldpkseq = checkMap.putIfAbsent(phone + pkseq, pkseq);
			if (oldpkseq != null) {
				checkExists = true;
				System.out.println(phone + "==" + pkseq + "===" + oldpkseq);
				break;
			}
		}
		Assert.assertTrue(!checkExists);
	}

	@Test
	public void testRemoveConcatUDHie() throws DecoderException {
		byte[] contents = Hex.decodeHex("050003870301a1f208".toCharArray());
		byte[] last = LongMessageFrameHolder.INS.removeConcatUDHie(contents);
		System.out.println(new String(Hex.encodeHex(last)));
		Assert.assertArrayEquals(Hex.decodeHex("a1f208".toCharArray()), last);

		contents = Hex.decodeHex("0b0003870301050401020304a1f208".toCharArray());
		last = LongMessageFrameHolder.INS.removeConcatUDHie(contents);
		System.out.println(new String(Hex.encodeHex(last)));
		Assert.assertArrayEquals(Hex.decodeHex("06050401020304a1f208".toCharArray()), last);

		contents = Hex.decodeHex("0c080400870301050401020304a1f208".toCharArray());
		last = LongMessageFrameHolder.INS.removeConcatUDHie(contents);
		System.out.println(new String(Hex.encodeHex(last)));
		Assert.assertArrayEquals(Hex.decodeHex("06050401020304a1f208".toCharArray()), last);

	}
	
	@Test 
	public void testSignatureTypeHead() throws SmsException
	{
		String sign = "【中信信用卡】";
		String text = sign + "限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请，或点 zxcard.cn/GHD 申请（结果实时审批为准），成功后本期仅需还人民币0.00元。如已还款请忽略，回TD退订-【中信信用卡】限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请，或点 zxcard.cn/GHD 申请（结果实时审";
		SignatureType st = new SignatureType(false,sign);
		testSignatureType(st,(AbstractSmsDcs)SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.RESERVED, SmsMsgClass.CLASS_UNKNOWN),text);
		testSignatureType(st,(AbstractSmsDcs)SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN),text);
	}
	
	@Test 
	public void testSignatureTypeTail() throws SmsException
	{
		String sign = "【中信信用卡】";
		String text = "限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请，或点 zxcard.cn/GHD 申请（结果实时审批为准），成功后本期仅需还人民币0.00元。如已还款请忽略，回TD退订-【中信信用卡】限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请，或点 zxcard.cn/GHD 申请（结果实时审";
		text += sign;
		SignatureType st = new SignatureType(true,sign);
		testSignatureType(st,(AbstractSmsDcs)SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.RESERVED, SmsMsgClass.CLASS_UNKNOWN),text);
		testSignatureType(st,(AbstractSmsDcs)SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN),text);
	}
	
	@Test 
	public void testSignatureTypeGSMTail() throws SmsException
	{
		String sign = "[cmss-ChinaMobile-cmos]";
		String text = SmsPduUtil.gsmstr + SmsPduUtil.gsmstr;
		text += sign;
		SignatureType st = new SignatureType(true,sign);
		testSignatureType(st,(AbstractSmsDcs)SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN),text);
	}
	
	@Test 
	public void testSignatureTypeGSMHead() throws SmsException
	{
		String sign = "[cmss-ChinaMobile-cmos]";
		String text = sign +SmsPduUtil.gsmstr + SmsPduUtil.gsmstr;
		SignatureType st = new SignatureType(false,sign);
		testSignatureType(st,(AbstractSmsDcs)SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM, SmsMsgClass.CLASS_UNKNOWN),text);
	}
	
	private void testSignatureType(SignatureType st ,AbstractSmsDcs dcs,String content) throws SmsException{
		for(boolean use8bit : new boolean[] {true,false}) {
			dcs.setUse8bit(use8bit);
			SmsTextMessage text = new SmsTextMessage(content,dcs);
		
			int signByteLength = (new SmsTextMessage(st.getSign(), dcs)).getUserData().getLength();
			List<LongMessageFrame> list = LongMessageFrameHolder.INS.splitmsgcontent(text, st);
			int i = 0;
			StringBuilder sb = new StringBuilder();
			System.out.println("======="+signByteLength+":"+st.getSign().length()+"======");
			for(LongMessageFrame frame : list) {
				String part = LongMessageFrameHolder.INS.getPartTextMsg(frame);
				System.out.println(i+":\t"+frame.getMsgLength()+":\t"+part.length()+"\t"+part);
				i++;
				sb.append(part);
			}
			if(st.isTail()) {
				Assert.assertEquals(text.getText().substring(0,text.getText().length()-st.getSign().length()), sb.toString());
			}else {
				Assert.assertEquals(text.getText().substring(st.getSign().length()), sb.toString());
			}
		}
	}
}
