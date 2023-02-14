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
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class LoggingOptions {

    public static final int LOG_PDU = 0x00000001;
    public static final int LOG_BYTES = 0x00000002;
    public static final int DEFAULT_LOG_OPTION = LOG_PDU;

    private int option;

    public LoggingOptions() {
        this.option = DEFAULT_LOG_OPTION;
    }

    public void setLogPdu(boolean value) {
        if (value) {
            this.option |= LOG_PDU;
        } else {
            this.option &= ~LOG_PDU;
        }
    }

    public boolean isLogPduEnabled() {
        return ((this.option & LOG_PDU) > 0);
    }

    public void setLogBytes(boolean value) {
        if (value) {
            this.option |= LOG_BYTES;
        } else {
            this.option &= ~LOG_BYTES;
        }
    }

    public boolean isLogBytesEnabled() {
        return ((this.option & LOG_BYTES) > 0);
    }
}
