package com.zx.sms.codec.cmpp.wap;

import org.marre.sms.SmsMessage;

import com.zx.sms.LongSMSMessage;

class SmsMessageHolder {
	SmsMessage smsMessage;
	LongSMSMessage msg;
	SmsMessageHolder(SmsMessage smsMessage,LongSMSMessage msg){
		this.msg = msg;
		this.smsMessage = smsMessage;
	}
}
