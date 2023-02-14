/**
 * 
 */
package com.zx.sms.common.util;

import com.zx.sms.codec.smgp.util.ByteUtil;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
public final class DefaultMsgIdUtil {
	
	private DefaultMsgIdUtil() {
		
	}

	public static byte[] msgId2Bytes(MsgId msgId) {
		long result = 0;
		if (msgId != null) {
			result |= (long) msgId.getMonth() << 60L;
			result |= (long) msgId.getDay() << 55L;
			result |= (long) msgId.getHour() << 50L;
			result |= (long) msgId.getMinutes() << 44L;
			result |= (long) msgId.getSeconds() << 38L;
			result |= (long) msgId.getGateId() << 16L;
			result |= (long) msgId.getSequenceId() & 0xffffL;
		}
		return ByteUtil.long2byte(result);

	}

	public static MsgId bytes2MsgId(byte[] bytes) {
		long result = ByteUtil.byte2long(bytes);
		return long2MsgId(result);
	}

	public static MsgId long2MsgId(long originLong) {
		long result = originLong;
		MsgId msgId = new MsgId();
		msgId.setMonth((int) ((result >>> 60) & 0xf));
		msgId.setDay((int) ((result >>> 55) & 0x1f));
		msgId.setHour((int) ((result >>> 50) & 0x1f));
		msgId.setMinutes((int) ((result >>> 44) & 0x3f));
		msgId.setSeconds((int) ((result >>> 38) & 0x3f));
		msgId.setGateId((int) ((result >>> 16) & 0x3fffff));
		msgId.setSequenceId((int) (result & 0xffff));
		return msgId;
	}
}
