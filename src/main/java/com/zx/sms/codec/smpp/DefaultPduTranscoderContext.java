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
 * Provides a default context for a PduTranscoder by looking everything up
 * using SMPP constants or default settings.  An "override" context can be
 * supplied in the constructor.  By default, this context will then attempt
 * to call the overridden context and any null value returned will then be
 * looked up using standard rules.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class DefaultPduTranscoderContext implements PduTranscoderContext {

    private final PduTranscoderContext overrideContext;

    public DefaultPduTranscoderContext() {
        this(null);
    }

    public DefaultPduTranscoderContext(PduTranscoderContext overrideContext) {
        this.overrideContext = overrideContext;
    }
    
    @Override
    public String lookupResultMessage(int commandStatus) {
        String resultMessage = null;
        if (overrideContext != null) {
            resultMessage = overrideContext.lookupResultMessage(commandStatus);
        }
        if (resultMessage == null) {
            resultMessage = SmppConstants.STATUS_MESSAGE_MAP.get(commandStatus);
        }
        return resultMessage;
    }

    @Override
    public String lookupTlvTagName(short tag) {
        String tagName = null;
        if (overrideContext != null) {
            tagName = overrideContext.lookupTlvTagName(tag);
        }
        if (tagName == null) {
            tagName = SmppConstants.TAG_NAME_MAP.get(tag);
        }
        return tagName;
    }
    
}