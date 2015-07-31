package com.zx.sms.common.util;

import java.nio.charset.Charset;

import com.google.common.base.Preconditions;
import com.zx.sms.common.GlobalConstance;

public final class CMPPCommonUtil {

	public static Charset switchCharset(short type){
		switch(type){
		  case 0:
              return Charset.forName("US-ASCII");
          case 3:
        	  return Charset.forName("US-ASCII");
          case 4:
        	  return Charset.forName("US-ASCII");
          case 8:
        	  return Charset.forName("ISO-10646-UCS-2");
          case 15:
        	  return Charset.forName("GBK");
          default:
              return GlobalConstance.defaultTransportCharset;
		}
	}
	
	
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
}
