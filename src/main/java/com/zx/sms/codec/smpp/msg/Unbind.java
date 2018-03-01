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

import com.zx.sms.codec.smpp.SmppConstants;

public class Unbind extends EmptyBody<UnbindResp> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1777566735396989201L;

	public Unbind() {
        super(SmppConstants.CMD_ID_UNBIND, "unbind");
    }

    @Override
    public UnbindResp createResponse() {
        UnbindResp resp = new UnbindResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<UnbindResp> getResponseClass() {
        return UnbindResp.class;
    }
    
}