package com.zx.sms.common;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.util.DefaultMsgIdUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;

public class TestMsgId {
	@Test
	public void testmsgid() throws DecoderException {
		//迈远格式的msgId与标准格式互转
		String maiyunMsgid = "9BD88980F32D1C3E";
		MsgId msgid = DefaultMsgIdUtil.bytes2MsgId(Hex.decodeHex(maiyunMsgid.toCharArray()));
		System.out.println(msgid);
		
		Assert.assertEquals(maiyunMsgid, msgid.toHexString(false));

	}
}
