package com.zx.sms.common.util;

import java.nio.charset.Charset;

import org.marre.sms.SmsAlphabet;
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
		
			return new SmsTextMessage(text);
		
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
	public static Charset switchCharset(SmsAlphabet type) {
		switch (type) {
		case ASCII:
			return StandardCharsets.ISO_8859_1;// 7bit编码
		case LATIN1:
			return StandardCharsets.ISO_8859_1;// 8bit编码,通常用于发送数据消息，比如图片和铃声等；
		case UCS2:
			return StandardCharsets.UTF_16BE;// 16bit编码
		case RESERVED:
			return StandardCharsets.GBK;// 预留
		default:
			return GlobalConstance.defaultTransportCharset;
		}
	}

}
