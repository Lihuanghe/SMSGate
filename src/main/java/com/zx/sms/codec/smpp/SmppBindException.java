package com.zx.sms.codec.smpp;

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

import com.zx.sms.codec.smpp.msg.BaseBindResp;
import com.zx.sms.common.util.HexUtil;

/**
 * Thrown only when a "bind" attempt fails and a BaseBindResp was returned.
 * This exception can only be created from the BaseBindResp instance which is
 * used to create the error message as well.
 * <BR>
 * If no BindResp was received and we timed out while waiting, then an
 * SmppTimeoutException or SmppConnectionException would have occurred, not this
 * specific error.  The underlying status code and mapped message can be figured
 * out as well.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class SmppBindException extends UnrecoverablePduException {
    static final long serialVersionUID = 1L;

    private final BaseBindResp bindResponse;
    
    public SmppBindException(BaseBindResp bindResponse) {
        super(buildErrorMessage(bindResponse));
        this.bindResponse = bindResponse;
    }

    public BaseBindResp getBindResponse() {
        return this.bindResponse;
    }

    static public String buildErrorMessage(BaseBindResp bindResponse) {
        if (bindResponse == null) {
            return "Bind request failed (response was null)";
        } else {
            return "Unable to bind [error: 0x" + HexUtil.toHexString(bindResponse.getCommandStatus()) + " \"" + bindResponse.getResultMessage() + "\"]";
        }
    }

}
