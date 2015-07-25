package com.zx.sms.common.util;

import java.nio.charset.Charset;

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
}
