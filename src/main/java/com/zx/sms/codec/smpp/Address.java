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

import java.io.Serializable;

import org.apache.commons.codec.binary.Hex;

import com.zx.sms.common.util.ByteBufUtil;
import com.zx.sms.common.util.PduUtil;

/**
 * Simple representation of an Address in SMPP.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class Address implements Serializable{

    private byte ton;
    private byte npi;
    private String address;

    public Address() {
        this((byte)0, (byte)0, (String)null);
    }

    public Address(byte ton, byte npi, String address) {
        this.ton = ton;
        this.npi = npi;
        this.address = address;
    }

    public byte getTon() {
        return this.ton;
    }

    public void setTon(byte value) {
        this.ton = value;
    }

    public byte getNpi() {
        return this.npi;
    }

    public void setNpi(byte value) {
        this.npi = value;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String value) {
        this.address = value;
    }

    public int calculateByteSize() {
        return 2 + PduUtil.calculateByteSizeOfNullTerminatedString(this.address);
    }

    public void read(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.ton = buffer.readByte();
        this.npi = buffer.readByte();
        this.address = ByteBufUtil.readNullTerminatedString(buffer);
    }

    public void write(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        buffer.writeByte(this.ton);
        buffer.writeByte(this.npi);
        ByteBufUtil.writeNullTerminatedString(buffer, this.address);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(40);
        buffer.append("0x");
        buffer.append(Hex.encodeHexString(new byte[]{this.ton}));
        buffer.append(" 0x");
        buffer.append(Hex.encodeHexString(new byte[]{this.npi}));
        buffer.append(" [");
        buffer.append(this.address);
        buffer.append("]");
        return buffer.toString();
    }
}
