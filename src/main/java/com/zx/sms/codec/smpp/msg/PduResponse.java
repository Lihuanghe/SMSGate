package com.zx.sms.codec.smpp.msg;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.Message;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public abstract class PduResponse extends Pdu {

    private String resultMessage;
    private PduRequest request;
    
    public PduResponse(int commandId, String name) {
        super(commandId, name, false);
    }

    public void setResultMessage(String value) {
        this.resultMessage = value;
    }

    public String getResultMessage() {
        return this.resultMessage;
    }
    
	@Override
	public void setRequest(BaseMessage message) {
		this.request = (PduRequest)message;
	}

	@Override
	public BaseMessage getRequest() {
		return this.request;
	}
    
}