package com.zx.sms;

import java.io.Serializable;

public interface BaseMessage extends Serializable{
	public boolean isRequest();
	public boolean isResponse();
}
