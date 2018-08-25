package com.zx.sms;

import org.marre.sms.SmsMessage;

import com.zx.sms.codec.cmpp.msg.LongMessageFrame;

public interface LongSMSMessage<T> {
	public LongMessageFrame generateFrame();
	public T generateMessage(LongMessageFrame frame) throws Exception;
	public SmsMessage getSmsMessage();
}
