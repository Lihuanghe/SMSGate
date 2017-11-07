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

import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.NotEnoughDataInBufferException;
import com.zx.sms.codec.smpp.RecoverablePduException;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.UnrecoverablePduException;
import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.HexUtil;
import com.zx.sms.common.util.PduUtil;

/**
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public abstract class BaseBind<R extends PduResponse> extends PduRequest<R> {

    private String systemId;
    private String password;
    private String systemType;
    private byte interfaceVersion;
    private Address addressRange;

    public BaseBind(int commandId, String name) {
        super(commandId, name);
    }

    public void setSystemId(String value) {
        this.systemId = value;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public String getPassword() {
        return this.password;
    }

    public void setSystemType(String value) {
        this.systemType = value;
    }

    public String getSystemType() {
        return this.systemType;
    }

    public void setInterfaceVersion(byte value) {
        this.interfaceVersion = value;
    }

    public byte getInterfaceVersion() {
        return this.interfaceVersion;
    }

    public Address getAddressRange() {
        return this.addressRange;
    }

    public void setAddressRange(Address value) {
        this.addressRange = value;
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.systemId = ByteBufUtil.readNullTerminatedString(buffer);
        this.password = ByteBufUtil.readNullTerminatedString(buffer);
        this.systemType = ByteBufUtil.readNullTerminatedString(buffer);
        // at this point, we should have at least 3 bytes left
        if (buffer.readableBytes() < 3) {
            throw new NotEnoughDataInBufferException("After parsing systemId, password, and systemType", buffer.readableBytes(), 3);
        }
        this.interfaceVersion = buffer.readByte();
        this.addressRange = ByteBufUtil.readAddress(buffer);
    }
    
    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.systemId);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.password);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.systemType);
        bodyLength += 1; // interface version
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.addressRange);
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.systemId);
        ByteBufUtil.writeNullTerminatedString(buffer, this.password);
        ByteBufUtil.writeNullTerminatedString(buffer, this.systemType);
        buffer.writeByte(this.interfaceVersion);
        ByteBufUtil.writeAddress(buffer, this.addressRange);
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("systemId [");
        buffer.append(this.systemId);
        buffer.append("] password [");
        buffer.append(this.password);
        buffer.append("] systemType [");
        buffer.append(this.systemType);
        buffer.append("] interfaceVersion [0x");
        buffer.append(HexUtil.toHexString(this.interfaceVersion));
        buffer.append("] addressRange (");
        if (this.addressRange == null) {
            buffer.append(SmppConstants.EMPTY_ADDRESS.toString());
        } else {
            buffer.append(this.addressRange);
        }
        buffer.append(")");
    }
}