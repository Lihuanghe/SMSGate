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
 * Exception that represents more data is required in order to parse an SMPP PDU.
 * In as many cases as possible, this exception should include an
 * estimate of how many bytes are needed/missing in order to succeed in parsing.
 * An estimate of -1 represents an uknown amount of data.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class NotEnoughDataInBufferException extends RecoverablePduException {
    static final long serialVersionUID = 1L;
    
    private int available;
    private int expected;

    /**
     * Constructs an instance of <code>AtNotEnoughDataInBufferException</code>
     * with the specified detail message and estimated number of bytes required.
     * An estimate of -1 represents an unknown amount.
     * @param msg the detail message.
     * @param available Number of bytes that were available
     * @param expected Number of bytes expected or -1 if unknown
     */
    public NotEnoughDataInBufferException(int available, int expected) {
        this(null, available, expected);
    }

    /**
     * Constructs an instance of <code>AtNotEnoughDataInBufferException</code>
     * with the specified detail message and estimated number of bytes required.
     * An estimate of -1 represents an unknown amount.
     * @param msg the detail message.
     * @param available Number of bytes that were available
     * @param expected Number of bytes expected or -1 if unknown
     */
    public NotEnoughDataInBufferException(String msg, int available, int expected) {
        super("Not enough data in byte buffer to complete encoding/decoding [expected: " + expected + ", available: " + available + "]" + (msg == null ? "" : ": " + msg));
        this.available = available;
        this.expected = expected;
    }

    public int getAvailable() {
        return available;
    }

    public int getExpected() {
        return expected;
    }
}