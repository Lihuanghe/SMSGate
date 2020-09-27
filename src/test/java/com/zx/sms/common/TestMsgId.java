package com.zx.sms.common;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.MsgId;

public class TestMsgId {
	@Test
	public void testmsgid() throws DecoderException {
		//迈远格式的msgId与标准格式互转
		String maiyunMsgid = "9BD88980F32D1C3E";
//		String maiyunMsgid = "53265100001FA118";
		MsgId msgid = DefaultMsgIdUtil.bytes2MsgId(Hex.decodeHex(maiyunMsgid.toCharArray()));
		System.out.println(msgid);
		
		Assert.assertEquals(maiyunMsgid, msgid.toHexString(false));

	}
	
	@Test
	public void testperformance() {
		System.out.println(new MsgId());
		long start = System.currentTimeMillis();
		for(int i = 0 ;i<1000000;i++) {
			new MsgId().toString();
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}
