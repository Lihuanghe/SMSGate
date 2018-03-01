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
import io.netty.util.internal.StringUtil;

import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.PduUtil;

/**
 * SMPP cancel_sm implementation.
 *
 * @author chris.matthews <idpromnut@gmail.com>
 */
public class CancelSm extends PduRequest<CancelSmResp> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7249768577373486011L;
	protected String serviceType;
    protected String messageId;
    protected Address sourceAddress;
    protected Address destAddress;

    public CancelSm() {
        super(SmppConstants.CMD_ID_CANCEL_SM, "cancel_sm");
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String value) {
        this.serviceType = value;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(String value) {
        this.messageId = value;
    }

    public Address getSourceAddress() {
        return this.sourceAddress;
    }

    public void setSourceAddress(Address value) {
        this.sourceAddress = value;
    }

    public Address getDestAddress() {
        return this.destAddress;
    }

    public void setDestAddress(Address value) {
        this.destAddress = value;
    }


    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.serviceType = ByteBufUtil.readNullTerminatedString(buffer);
        this.messageId = ByteBufUtil.readNullTerminatedString(buffer);
        this.sourceAddress = ByteBufUtil.readAddress(buffer);
        this.destAddress = ByteBufUtil.readAddress(buffer);
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.serviceType);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.messageId);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.destAddress);
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.serviceType);
        ByteBufUtil.writeNullTerminatedString(buffer, this.messageId);
        ByteBufUtil.writeAddress(buffer, this.sourceAddress);
        ByteBufUtil.writeAddress(buffer, this.destAddress);
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("(serviceType [");
        buffer.append((this.serviceType));
        buffer.append("] messageId [");
        buffer.append((this.messageId));
        buffer.append("] sourceAddr [");
        buffer.append((this.sourceAddress));
        buffer.append("] destAddr [");
        buffer.append((this.destAddress));
        buffer.append("])");

    }

    @Override
    public CancelSmResp createResponse() {
        CancelSmResp resp = new CancelSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<CancelSmResp> getResponseClass() {
        return CancelSmResp.class;
    }

}
