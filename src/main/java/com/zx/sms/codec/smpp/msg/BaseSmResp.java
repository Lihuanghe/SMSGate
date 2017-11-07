package com.zx.sms.codec.smpp.msg;

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
import io.netty.util.internal.StringUtil;

import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.PduUtil;

/**
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public abstract class BaseSmResp extends PduResponse {

    private String messageId;

    public BaseSmResp(int commandId, String name) {
        super(commandId, name);
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(String value) {
        this.messageId = value;
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        // the body may or may not contain a messageId -- the helper utility
        // method will take care of returning null if there aren't any readable bytes
        this.messageId = ByteBufUtil.readNullTerminatedString(buffer);
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.messageId);
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        // when this PDU was parsed, it's possible it was missing the messageId instead
        // of having a NULL messageId. If that's the case, the commandLength will be just
        // enough for the headers (and theoretically any optional TLVs). Don't try to
        // write the NULL byte for that case.
        // See special note in 4.4.2 of SMPP 3.4 spec
        if (!((buffer.writableBytes() == 0) && (this.messageId == null))) {
            ByteBufUtil.writeNullTerminatedString(buffer, this.messageId);
        }
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("(messageId [");
        buffer.append((this.messageId));
        buffer.append("])");
    }
}