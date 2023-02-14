package com.zx.sms.codec.cmpp.wap;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.LongSMSMessage;

public class SmsMessageHolder {
	SmsMessage smsMessage;
	LongSMSMessage msg;
	SmsMessageHolder(SmsMessage smsMessage,LongSMSMessage msg){
		this.msg = msg;
		this.smsMessage = smsMessage;
	}
	public SmsMessage getSmsMessage() {
		return smsMessage;
	}
	public LongSMSMessage getMsg() {
		return msg;
	}
	
}
