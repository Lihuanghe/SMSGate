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

import com.zx.sms.codec.smpp.msg.Pdu;

/**
 * Thrown when a recoverable PDU decoding error occurs.  A recoverable PDU
 * error includes the partially decoded PDU in order to generate a negative
 * acknowledgement response (if needed).
 *
 * A good example is that the PDU header was read, but the body of the PDU
 * failed to parse correctly.
 *
 * The recommended action for "recoverable" errors is to return a NACK.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class RecoverablePduException extends Exception {
    static final long serialVersionUID = 1L;
    
    private Pdu partialPdu;

    public RecoverablePduException(String msg) {
        super(msg);
    }

    public RecoverablePduException(String msg, Throwable t) {
        super(msg, t);
    }

    public RecoverablePduException(Pdu partialPdu, String msg) {
        super(msg);
        this.partialPdu = partialPdu;
    }

    public RecoverablePduException(Pdu partialPdu, String msg, Throwable t) {
        super(msg, t);
        this.partialPdu = partialPdu;
    }

    public void setPartialPdu(Pdu pdu) {
        this.partialPdu = pdu;
    }

    public Pdu getPartialPdu() {
        return this.partialPdu;
    }
}