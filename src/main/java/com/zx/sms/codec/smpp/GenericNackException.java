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

import com.zx.sms.codec.smpp.msg.GenericNack;
import com.zx.sms.common.util.HexUtil;

/**
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class GenericNackException extends RecoverablePduException {
    static final long serialVersionUID = 1L;
    
    public GenericNackException(GenericNack nack) {
        super(buildErrorMessage(nack));
    }

    static public String buildErrorMessage(GenericNack nack) {
        return "Negative acknowledgement for request [error: 0x" + HexUtil.toHexString(nack.getCommandStatus()) + " \"" + nack.getResultMessage() + "\"]";
    }
}