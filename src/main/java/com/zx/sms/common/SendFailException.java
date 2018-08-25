package com.zx.sms.common;

public class SendFailException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7953994102678445678L;

    public SendFailException(String message) {
        super(message);
    }
}
