package com.zx.sms.codec.smpp.msg;

import com.zx.sms.BaseMessage;


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

public abstract class PduRequest<R extends PduResponse> extends Pdu {

    public PduRequest(int commandId, String name) {
        super(commandId, name, true);
    }

    abstract public R createResponse();

    abstract public Class<R> getResponseClass();

    public GenericNack createGenericNack(int commandStatus) {
        GenericNack nack = new GenericNack();
        nack.setCommandStatus(commandStatus);
        nack.setSequenceNumber(this.getSequenceNumber());
        return nack;
    }
    
	public PduRequest clone() throws CloneNotSupportedException {
		return (PduRequest) super.clone();
	}
	
	@Override
	public void setRequest(BaseMessage message) {
	}

	@Override
	public BaseMessage getRequest() {
		return null;
	}

}