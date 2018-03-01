package com.zx.sms.codec.smpp.msg;

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

public class PartialPdu extends EmptyBody<GenericNack> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -257762394313889568L;

	public PartialPdu(int commandId) {
        super(commandId, "partial_pdu");
    }

    @Override
    public GenericNack createResponse() {
        GenericNack resp = new GenericNack();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<GenericNack> getResponseClass() {
        return GenericNack.class;
    }
    
}