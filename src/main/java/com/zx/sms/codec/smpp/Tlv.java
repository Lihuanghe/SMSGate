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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.common.util.HexUtil;

/**
 * Tag-Length-Value optional parameter in SMPP.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class Tlv implements Serializable{
    
    private final short tag;
    private final byte[] value;     // length is stored in array
    private String tagName;      // short description of this tag

    public Tlv(short tag, byte[] value) {
        this(tag, value, null);
    }

    /**
     * The tagName is only for logging/debugging by showing up in the toString() method.
     * @param tag
     * @param value
     * @param tagName
     */
    public Tlv(short tag, byte[] value, String tagName) {
        this.tag = tag;
        this.value = value;
        this.tagName = tagName;
    }

    public String getTagName() {
        return this.tagName;
    }

    public void setTagName(String value) {
        this.tagName = value;
    }

    public short getTag() {
        return this.tag;
    }

    /**
     * Gets an "unsigned" version of this TLV's length of its value.  A TLV
     * has a 16-bit integer that is encoded as a short.  Since Java only has
     * "signed" shorts, any length > 32K would end up being a negative value.
     * This method returns the "unsigned" version as an int.
     * @return The "unsigned" length of this TLV's value
     */
    public int getUnsignedLength() {
        return (value == null ? 0 : value.length);
    }

    public short getLength() {
        return (short)getUnsignedLength();
    }

    public byte[] getValue() {
        return this.value;
    }

    /**
     * Returns the size of this TLV in bytes.  Basically, its always 4 bytes
     * plus the length of the value.  Two bytes for tag and two byte for length
     * and then the length of the value itself.  Primarily used for encoding
     * the PDU.
     * @return
     */
    public int calculateByteSize() {
        return 4 + getUnsignedLength();
    }

    public String getValueAsString() throws TlvConvertException {
        return getValueAsString("ISO-8859-1");
    }

    public String getValueAsString(String charsetName) throws TlvConvertException {
        if (this.value == null) {
            return null;
        }
        if (this.value.length == 0) {
            return "";
        }
        // default the position to be the entire byte array
        int len = this.value.length;
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] == 0x00) {
                len = i;
                break;
            }
        }

        try {
            return new String(this.value, 0, len, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new TlvConvertException("String", "unsupported charset " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(40);
        if (tagName != null) {
            buffer.append(this.tagName);
        } else {
            buffer.append("tlv");
        }
        buffer.append(": 0x");
        buffer.append(HexUtil.toHexString(this.tag));
        buffer.append(" 0x");
        buffer.append(HexUtil.toHexString((short)getUnsignedLength()));
        buffer.append(" [");
        HexUtil.appendHexString(buffer, this.value);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Attempts to get the underlying value as a byte.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 1, then it cannot be
     * converted to a byte.
     * @return The byte array value as a byte
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to a byte.
     */
    public byte getValueAsByte() throws TlvConvertException {
        try {
            return ByteArrayUtil.toByte(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("byte", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an unsigned byte.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 1, then it cannot be
     * converted to an unsigned byte.
     * @return The byte array value as an unsigned byte
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an unsigned byte.
     */
    public short getValueAsUnsignedByte() throws TlvConvertException {
        try {
            return ByteArrayUtil.toUnsignedByte(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("unsigned byte", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as a short.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 2, then it cannot be
     * converted to a short.
     * @return The byte array value as a short
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to a short.
     */
    public short getValueAsShort() throws TlvConvertException {
        try {
            return ByteArrayUtil.toShort(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("short", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an unsigned short.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 2, then it cannot be
     * converted to an unsigned short.
     * @return The byte array value as an unsigned short
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an unsigned short.
     */
    public int getValueAsUnsignedShort() throws TlvConvertException {
        try {
            return ByteArrayUtil.toUnsignedShort(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("unsigned short", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an int.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 4, then it cannot be
     * converted to an int.
     * @return The byte array value as an int
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an int.
     */
    public int getValueAsInt() throws TlvConvertException {
        try {
            return ByteArrayUtil.toInt(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("int", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as an unsigned int.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 4, then it cannot be
     * converted to an unsigned int.
     * @return The byte array value as an unsigned int
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to an unsigned int.
     */
    public long getValueAsUnsignedInt() throws TlvConvertException {
        try {
            return ByteArrayUtil.toUnsignedInt(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("unsigned int", e.getMessage());
        }
    }

    /**
     * Attempts to get the underlying value as a long.  If the underlying byte
     * array cannot be converted, then an exception is thrown.  For example,
     * if the underlying byte array isn't a length of 8, then it cannot be
     * converted to a long.
     * @return The byte array value as a long
     * @throws TlvConvertException Thrown if the underlying byte array cannot
     *      be converted to a long.
     */
    public long getValueAsLong() throws TlvConvertException {
        try {
            return ByteArrayUtil.toLong(this.value);
        } catch (IllegalArgumentException e) {
            throw new TlvConvertException("long", e.getMessage());
        }
    }

    /**
     * Checks if another Tlv object is equal to this Tlv object. Two tlv objects
     * are considered equal if they have the same tag and the same value.
     * 
     * @param t
     * @return true if this and t have the same tag and value, false otherwise.
     */
    @Override
    public boolean equals(Object t){
        if (t == null){
            return false;
        } else if(this == t){
            return true;
        }
        Tlv other = (Tlv) t;
        return other.tag == this.tag && Arrays.equals(other.value, this.value);
    }

    /**
     * Computes a valid hash code for this Tlv object.
     * 
     * @return int hash code for this object.
     */
    @Override
    public int hashCode(){
        int hash = 42;
        hash = 31 * hash + ((int) this.tag);
        hash = 31 * hash + Arrays.hashCode(this.value);
        return hash;
    }
}
