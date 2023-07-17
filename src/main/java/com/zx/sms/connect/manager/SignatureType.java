package com.zx.sms.connect.manager;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class SignatureType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3432422856512506571L;
	
	private boolean tail = false;
	private String sign="";
	private Pattern pSign = null;
	public SignatureType(boolean tail, String sign) {
		this.tail = tail;
		this.sign = sign;
	}
	public SignatureType(boolean tail, Pattern pSign) {
		this.tail = tail;
		this.pSign = pSign;
	}
	public boolean isTail() {
		return tail;
	}
	public String getSign() {
		return sign;
	}
	public Pattern getpSign() {
		return pSign;
	}
	public void setpSign(Pattern pSign) {
		this.pSign = pSign;
	}
	
	public String fetchSign(String smsContent) {
		if(StringUtils.isBlank(smsContent)) 
			return "";
		
		smsContent =  smsContent.trim();
		if(StringUtils.isNotBlank(sign)) {
			boolean removeSign = (tail && smsContent.endsWith(sign))
			|| (!tail && smsContent.startsWith(sign));
			return sign;
		}
		
		if(pSign!=null) {
			
			Matcher m = pSign.matcher(smsContent);
			int start = -1;
			int end = -1;
			String  t_sign = "";
			while(m.find()) {

				if(tail) {
					t_sign = m.group();
					start = m.start();
					end = m.end();
				}else {
					t_sign = m.group();
					start = m.start();
					end = m.end();
					break;
				}
			}
			
			if(tail) {
				if(end == smsContent.length())
					return t_sign;
			}else {
				if(start == 0)
					return t_sign;
			}
		}

		return "";		
		
	}
	
	
}
