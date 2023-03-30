package com.zx.sms.connect.manager;

import java.io.Serializable;

public class SignatureType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3432422856512506571L;
	
	private boolean tail = false;
	private String sign="";
	public SignatureType(boolean tail, String sign) {
		this.tail = tail;
		this.sign = sign;
	}
	public boolean isTail() {
		return tail;
	}
	public String getSign() {
		return sign;
	}
}
