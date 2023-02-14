package com.zx.sms.codec.smpp;

/**
 * 消息payLoad是放在UD里还是 OptionParameter里
 */
public enum SmppSplitType {
	UDHPARAM, //smpp3.4支持
	UDH,  //smpp3.3支持
	PAYLOAD, //smpp3.4支持
	PAYLOADPARAM //smpp3.4支持
	;
	
}
