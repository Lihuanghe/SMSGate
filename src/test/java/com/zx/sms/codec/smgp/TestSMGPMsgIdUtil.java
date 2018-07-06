package com.zx.sms.codec.smgp;


import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.smgp.msg.MsgId;
import com.zx.sms.codec.smgp.msg.SMGPReportData;
import com.zx.sms.codec.smgp.util.SMGPMsgIdUtil;
import com.zx.sms.common.util.SequenceNumber;

public class TestSMGPMsgIdUtil {
	
	@Test
	public void testMsgid() throws Exception{
			MsgId m = new MsgId();
			byte[] arr = SMGPMsgIdUtil.msgId2Bytes(m);
			Assert.assertEquals(m, SMGPMsgIdUtil.bytes2MsgId(arr));
			
		arr = Hex.decodeHex("69643a6495c36ac4ea6b00000b7375623a30303120646c7672643a303031207375626d69745f646174653a3138303730353232303720646f6e655f646174653a3138303730353232303720737461743a44454c49565244206572723a303030207478743ad6d0b9fa306138616535".toCharArray());
		System.out.println(new String(Hex.decodeHex("20737461743a206572723a207478743a".toCharArray())));
	
		SMGPReportData tmpreport = new SMGPReportData();
		tmpreport.fromBytes(arr);
		System.out.println(new SequenceNumber());
	}

}
