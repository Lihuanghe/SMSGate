package com.zx.sms.common.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.marre.sms.SmsAlphabet;
import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMsgClass;
import org.marre.sms.SmsPduUtil;
import org.marre.sms.SmsTextMessage;

import com.google.common.base.Preconditions;
import com.zx.sms.common.GlobalConstance;

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
		
			return new SmsTextMessage(text, SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN));
		
	}
	// 处理GSM协议TP-DCS数据编码方案
	/*
	 * 数据编码方案 TP-DCS（TP-Data-Coding-Scheme）长度1byte
	 * 
	 * Bit No.7 与Bit No.6 :一般设置为00； Bit No.5： 0—文本未压缩， 1—文本用GSM 标准压缩算法压缩 Bit
	 * No.4： 0—表示Bit No.1、Bit No.0 为保留位，不含信息类型信息， 1—表示Bit No.1、Bit No.0 含有信息类型信息
	 * Bit No.3 与Bit No.2： 00—默认的字母表(7bit 编码) 01—8bit， 10—USC2（16bit）编码 11—预留
	 * Bit No.1 与Bit No.0： 00—Class 0， 01—Class 1， 10—Class 2（SIM 卡特定信息），
	 * 11—Class 3//写卡
	 */
	public static Charset switchCharset(short type) {
		switch (type) {
		case 0:
			return Charset.forName("US-ASCII");// 7bit编码
		case 3:
			return Charset.forName("US-ASCII");// 7bit编码
		case 4:
			return Charset.forName("US-ASCII");// 8bit编码,通常用于发送数据消息，比如图片和铃声等；
		case 8:
			return Charset.forName("ISO-10646-UCS-2");// 16bit编码
		case 15:
			return Charset.forName("GBK");// 预留
		default:
			return GlobalConstance.defaultTransportCharset;
		}
	}
	
	public static SmsTextMessage buildTextMessagefromGSMchar(byte[] bytes,short msgfmt){
		String text = null;
		if(msgfmt == 0){
			
			text = SmsPduUtil.unencodedSeptetsToString(bytes);
		}else{
			text = new String(bytes,switchCharset(msgfmt));
		}
		return new SmsTextMessage(text, SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN));
	}
	@Deprecated
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
