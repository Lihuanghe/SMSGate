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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import com.zx.sms.codec.smpp.msg.Pdu;

/**
 * Interface for encoding/decoding PDUs to/from ByteBufs.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public interface PduTranscoder {

    /**
     * Encodes a PDU into a new ByteBuf.
     * @param pdu The PDU to convert into a buffer
     * @return The new ByteBuf ready to send on a Channel
     * @throws UnrecoverablePduException Thrown if there is an unrecoverable
     *      error while encoding the buffer.  Recommended action is to rebind
     *      the session.
     * @throws RecoverablePduException Thrown if there is recoverable
     *      error while encoding the buffer. A good example is an optional parameter
     *      that is invalid or a terminating null byte wasn't found.
     */
    public ByteBuf encode(Pdu pdu,ByteBufAllocator allocator) throws UnrecoverablePduException, RecoverablePduException;

    /**
     * Decodes a ByteBuf into a new PDU.
     * @param buffer The buffer to read data from
     * @return The new PDU created from the data
     * @throws UnrecoverablePduException Thrown if there is an unrecoverable
     *      error while decoding the buffer.  Recommended action is to rebind
     *      the session.
     * @throws RecoverablePduException Thrown if there is recoverable
     *      error while decoding the buffer. A good example is an optional parameter
     *      that is invalid or a terminating null byte wasn't found.
     */
    public Pdu decode(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException;
    
}
