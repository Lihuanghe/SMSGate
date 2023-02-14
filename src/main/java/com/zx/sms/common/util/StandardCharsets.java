package com.zx.sms.common.util;

import java.nio.charset.Charset;

public class StandardCharsets {
	public static final Charset GBK =  init("GBK");
	public static final Charset UTF_8 =  init("UTF8");
	public static final Charset US_ASCII =  init("ASCII");
	public static final Charset ISO_8859_1 =  init("ISO_8859_1");
	public static final Charset UTF_16BE =  init("UTF_16BE");
	
	private static Charset init(String name){
		try{
			Charset charset =  Charset.forName(name);
		
			return charset;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}