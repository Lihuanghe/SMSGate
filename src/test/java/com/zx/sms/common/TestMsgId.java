package com.zx.sms.common;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.marre.sms.SmsPduUtil;

import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.common.util.StandardCharsets;

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
