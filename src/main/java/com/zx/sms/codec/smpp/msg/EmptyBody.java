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

import io.netty.buffer.ByteBuf;

import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;

public abstract class EmptyBody<R extends PduResponse> extends PduRequest<R> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 392308948896546615L;

	public EmptyBody(int commandId, String name) {
        super(commandId, name);
    }

    @Override
    public void readBody(ByteBuf buffer,SMPPEndpointEntity entity) throws UnrecoverablePduException, RecoverablePduException {
        // no body
    }

    @Override
    public int calculateByteSizeOfBody() {
        return 0;   // no body
    }

    @Override
    public void writeBody(ByteBuf buffer,SMPPEndpointEntity entity) throws UnrecoverablePduException, RecoverablePduException {
        /// no body
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        // no body
    }

}