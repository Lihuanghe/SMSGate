package com.zx.sms.codec.smpp.msg;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2013 Cloudhopper by Twitter
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

import org.marre.util.StringUtil;

import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.HexUtil;
import com.zx.sms.common.util.PduUtil;

/**
 * SMPP query_sm_resp implementation.
 *
 * @author chris.matthews <idpromnut@gmail.com>
 */
public class QuerySmResp extends PduResponse {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1930567419405498513L;
	private String messageId;
    private String finalDate;
    private byte messageState;
    private byte errorCode;

    public QuerySmResp() {
        super(SmppConstants.CMD_ID_QUERY_SM_RESP, "query_sm_resp");
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(final String iMessageId) {
        messageId = iMessageId;
    }

    public String getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(final String iFinalDate) {
        finalDate = iFinalDate;
    }

    public byte getMessageState() {
        return messageState;
    }

    public void setMessageState(final byte iMessageState) {
        messageState = iMessageState;
    }

    public byte getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final byte iErrorCode) {
        errorCode = iErrorCode;
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.messageId = ByteBufUtil.readNullTerminatedString(buffer);
        this.finalDate = ByteBufUtil.readNullTerminatedString(buffer);
        this.messageState = buffer.readByte();
        this.errorCode = buffer.readByte();
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.messageId);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.finalDate);
        bodyLength += 2;    // messageState, errorCode
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.messageId);
        ByteBufUtil.writeNullTerminatedString(buffer, this.finalDate);
        buffer.writeByte(this.messageState);
        buffer.writeByte(this.errorCode);
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("(messageId [");
        buffer.append((this.messageId));
        buffer.append("] finalDate [");
        buffer.append((this.finalDate));
        buffer.append("] messageState [0x");
        buffer.append(HexUtil.toHexString(this.messageState));
        buffer.append("] errorCode [0x");
        buffer.append(HexUtil.toHexString(this.errorCode));
        buffer.append("])");
    }
}
