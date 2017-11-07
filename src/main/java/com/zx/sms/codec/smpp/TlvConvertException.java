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


/**
 * Thrown when attempting to cast a TLV value into a different type such as a
 * byte, string, etc. 
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class TlvConvertException extends RecoverablePduException {
    static final long serialVersionUID = 1L;
    
    public TlvConvertException(String msg) {
        super(msg);
    }

    public TlvConvertException(String typeName, String extraMsg) {
        super("Unable to cast TLV value into " + typeName + ": " + extraMsg);
    }

    public TlvConvertException(String msg, Throwable t) {
        super(msg, t);
    }
}