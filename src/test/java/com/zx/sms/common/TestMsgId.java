package com.zx.sms.common;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.chinamobile.cmos.sms.SmsPduUtil;
import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.MsgId;

public class TestMsgId {
	@Test
	public void testmsgid() throws DecoderException, UnsupportedEncodingException {
		//迈远格式的msgId与标准格式互转
		String maiyunMsgid = "9BD88980F32D1C3E";
//		String maiyunMsgid = "53265100001FA118";
		MsgId msgid = DefaultMsgIdUtil.bytes2MsgId(Hex.decodeHex(maiyunMsgid.toCharArray()));
		System.out.println(msgid);
		
		String messageid = "A1527684";
		
		System.out.println(String.format("%010d",ByteArrayUtil.toUnsignedInt(Hex.decodeHex(messageid.toCharArray()))));
		byte[] arr = ByteArrayUtil.toByteArray(Long.valueOf("2706536068"));
		byte[] arr4j = new byte[4];
		System.arraycopy(arr, 4, arr4j, 0, 4);
		System.out.println(String.valueOf(Hex.encodeHex(arr4j,true)));
		
		Assert.assertEquals(maiyunMsgid, msgid.toHexString(false));
		System.out.println(Hex.encodeHex(SmsPduUtil.getSeptets("Hello world")));
		
		System.out.println(new MsgId(12345L).toString());
		String str = "16AA8D411FFA9B3A";
		MsgId longMsgid = DefaultMsgIdUtil.long2MsgId(1633273125724134202L);
		MsgId longMsgidHex = DefaultMsgIdUtil.bytes2MsgId(Hex.decodeHex(str.toCharArray()));
		System.out.println("==" + longMsgid + "\n==" + longMsgidHex);
		Assert.assertEquals(longMsgid, longMsgidHex);
	}
	
	@Test
	public void testperformance() {
		
		long start = System.currentTimeMillis();
		for(int i = 0 ;i<1000000;i++) {
			new MsgId().toString();
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		System.out.println(new MsgId());
	}
	@Test 
	public void errMsgid() {
		try {
			new MsgId(10000000);
			Assert.assertTrue(false);
		}catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			//确保会抛异常到这里
			Assert.assertTrue(true);
		}
		try {
			new MsgId(-1);
			Assert.assertTrue(false);
		}catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			//确保会抛异常到这里
			Assert.assertTrue(true);
		}
	}
}
