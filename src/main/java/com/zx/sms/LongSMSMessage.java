package com.zx.sms;

import com.zx.sms.codec.cmpp.msg.LongMessageFrame;

public interface LongSMSMessage<T> {
	public LongMessageFrame generateFrame();
	public T generateMessage(LongMessageFrame frame) throws Exception;
}
