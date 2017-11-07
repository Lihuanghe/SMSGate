package com.zx.sms.common.util;

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

import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.SmppConstants;

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class PduUtil {

    /**
     * Calculates size of a "C-String" by returning the length of the String
     * plus 1 (for the NULL byte).  If the parameter is null, will return 1.
     * @param value
     * @return
     */
    static public int calculateByteSizeOfNullTerminatedString(String value) {
        if (value == null) {
            return 1;
        }
        return value.length() + 1;
    }

    /**
     * Calculates the byte size of an Address.  Safe to call with nulls as a
     * parameter since this will then just return the size of an empty address.
     * @param value
     * @return
     */
    static public int calculateByteSizeOfAddress(Address value) {
        if (value == null) {
            return SmppConstants.EMPTY_ADDRESS.calculateByteSize();
        } else {
            return value.calculateByteSize();
        }
    }

    static public boolean isRequestCommandId(int commandId) {
        // if the 31st bit is not set, this is a request
        return ((commandId & SmppConstants.PDU_CMD_ID_RESP_MASK) == 0);
    }

    static public boolean isResponseCommandId(int commandId) {
        // if the 31st bit is not set, this is a request
        return ((commandId & SmppConstants.PDU_CMD_ID_RESP_MASK) == SmppConstants.PDU_CMD_ID_RESP_MASK);
    }
}
