package com.zx.sms.common.util;

import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsAlphabet;
import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMsgClass;
import org.marre.sms.SmsTextMessage;

import com.google.common.base.Preconditions;

public final class CMPPCommonUtil {
	
	public static byte[] ensureLength(byte array[], int minLength, int padding) {
		Preconditions.checkArgument(minLength >= 0, "Invalid minLength: %s", new Object[] { Integer.valueOf(minLength) });
		Preconditions.checkArgument(padding >= 0, "Invalid padding: %s", new Object[] { Integer.valueOf(padding) });
		if(array.length == minLength) return array;
		return array.length > minLength ? copyOf(array, minLength) : copyOf(array, minLength + padding);
	}

	private static byte[] copyOf(byte original[], int length) {
		byte copy[] = new byte[length];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
		return copy;
	}
	public static SmsTextMessage buildTextMessage(String text){
		if(haswidthChar(text)){
			return new SmsTextMessage(text, SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN));
		}else{
			return new SmsTextMessage(text);
		}
		
	}
	private static boolean haswidthChar(String content) {
		if (StringUtils.isEmpty(content))
			return false;

		byte[] bytes = content.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			// 判断最高位是否为1
			if ((bytes[i] & (byte) 0x80) == (byte) 0x80) {
				return true;
			}
		}
		return false;
	}
}
