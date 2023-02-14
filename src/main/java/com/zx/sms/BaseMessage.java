package com.zx.sms;

import java.io.Serializable;

public interface BaseMessage extends Serializable{
	 boolean isRequest();
	 boolean isResponse();
	 boolean isTerminated();
     void setRequest(BaseMessage message);
     BaseMessage getRequest();
     int getSequenceNo();
     void setSequenceNo(int seq);
     long getTimestamp();
}
