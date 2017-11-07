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
 * Interface for providing a "context" to the transcoding process for SMPP PDUs.
 * For example, custom status messages, sequence number validators, or custom
 * TLV tag names, and other logic in the future.
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public interface PduTranscoderContext {

    /**
     * Lookup a "command_status" value and returns a String that represents a result
     * message (description) of what the value means.  A way to add helpful
     * debugging information into logfiles or management interfaces.  This value
     * is printed out via the toString() method for a PDU response.
     *
     * @param commandStatus The command_status field to lookup
     * @return A String representing a short description of what the command_status
     *      value represents.  For example, a command_status of 0 usually means "OK"
     */
    public String lookupResultMessage(int commandStatus);

    /**
     * Lookup a name for the tag of a TLV and returns a String that represents a
     * name for it.  A way to add helpful debugging information into logfiles or
     * management interfaces.
     *
     * @param tag The TLV's tag value to lookup
     * @return A String representing a short name of what the tag
     *      value represents.  For example, a tag of 0x001E usually means "receipted_message_id"
     */
    public String lookupTlvTagName(short tag);

}
