package com.zx.sms.common.util;


/*
 * #%L
 * ch-commons-util
 * %%
 * Copyright (C) 2012 Cloudhopper by Twitter
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
 * Utility class for working with byte arrays such as converting between
 * byte arrays and numbers.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class ByteArrayUtil {

    protected static void checkBytesNotNull(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array was null");
        }
    }

    /**
     * Helper method for validating if an offset and length are valid for a given
     * byte array. Checks if the offset or length is negative or if the offset+length
     * would cause a read past the end of the byte array.
     * @param bytesLength The length of the byte array to validate against
     * @param offset The offset within the byte array
     * @param length The length to read starting from the offset
     * @throws java.lang.IllegalArgumentException If any of the above conditions
     *      are violated.
     */
    static protected void checkOffsetLength(int bytesLength, int offset, int length)
            throws IllegalArgumentException {
        // offset cannot be negative
        if (offset < 0) {
            throw new IllegalArgumentException("The byte[] offset parameter cannot be negative");
        }
        // length cannot be negative either
        if (length < 0) {
            throw new IllegalArgumentException("The byte[] length parameter cannot be negative");
        }
        // is it a valid offset?  Must be < bytes.length if non-zero
        // if its zero, then the check below will validate if it would cause
        // a read past the length of the byte array
        if (offset != 0 && offset >= bytesLength) {
            throw new IllegalArgumentException("The byte[] offset (" + offset + ") must be < the length of the byte[] length (" + bytesLength + ")");
        }
        if (offset+length > bytesLength) {
            throw new IllegalArgumentException("The offset+length (" + (offset+length) + ") would read past the end of the byte[] (length=" + bytesLength + ")");
        }
    }
    
    protected static void checkBytes(byte[] bytes, int offset, int length, int expectedLength) {
        checkBytesNotNull(bytes);
        checkOffsetLength(bytes.length, offset, length);
        if (length != expectedLength) {
            throw new IllegalArgumentException("Unexpected length of byte array [expected=" + expectedLength + ", actual=" + length + "]");
        }
    }

    public static byte[] toByteArray(byte value) {
        return new byte[] { value };
    }

    public static byte[] toByteArray(short value) {
        byte[] buf = new byte[2];
        buf[1] = (byte)(value & 0xFF);
        buf[0] = (byte)((value >>> 8) & 0xFF);
        return buf;
    }

    public static byte[] toByteArray(int value) {
        byte[] buf = new byte[4];
        buf[3] = (byte)(value & 0xFF);
        buf[2] = (byte)((value >>> 8) & 0xFF);
        buf[1] = (byte)((value >>> 16) & 0xFF);
        buf[0] = (byte)((value >>> 24) & 0xFF);
        return buf;
    }

    public static byte[] toByteArray(long value) {
        byte[] buf = new byte[8];
        buf[7] = (byte)(value & 0xFF);
        buf[6] = (byte)((value >>> 8) & 0xFF);
        buf[5] = (byte)((value >>> 16) & 0xFF);
        buf[4] = (byte)((value >>> 24) & 0xFF);
        buf[3] = (byte)((value >>> 32) & 0xFF);
        buf[2] = (byte)((value >>> 40) & 0xFF);
        buf[1] = (byte)((value >>> 48) & 0xFF);
        buf[0] = (byte)((value >>> 56) & 0xFF);
        return buf;
    }

    public static byte toByte(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toByte(bytes, 0, bytes.length);
    }

    public static byte toByte(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 1);
        return bytes[offset];
    }

    public static short toUnsignedByte(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toUnsignedByte(bytes, 0, bytes.length);
    }

    public static short toUnsignedByte(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 1);
        short v = 0;
        v |= bytes[offset] & 0xFF;
        return v;
    }

    public static short toShort(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toShort(bytes, 0, bytes.length);
    }

    public static short toShort(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 2);
        short v = 0;
        v |= bytes[offset] & 0xFF;
        v <<= 8;
        v |= bytes[offset+1] & 0xFF;
        return v;
    }

    public static int toUnsignedShort(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toUnsignedShort(bytes, 0, bytes.length);
    }

    public static int toUnsignedShort(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 2);
        int v = 0;
        v |= bytes[offset] & 0xFF;
        v <<= 8;
        v |= bytes[offset+1] & 0xFF;
        return v;
    }

    public static int toInt(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toInt(bytes, 0, bytes.length);
    }

    public static int toInt(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 4);
        int v = 0;
        v |= bytes[offset] & 0xFF;
        v <<= 8;
        v |= bytes[offset+1] & 0xFF;
        v <<= 8;
        v |= bytes[offset+2] & 0xFF;
        v <<= 8;
        v |= bytes[offset+3] & 0xFF;
        return v;
    }

    public static long toUnsignedInt(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toUnsignedInt(bytes, 0, bytes.length);
    }

    public static long toUnsignedInt(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 4);
        long v = 0;
        v |= bytes[offset] & 0xFF;
        v <<= 8;
        v |= bytes[offset+1] & 0xFF;
        v <<= 8;
        v |= bytes[offset+2] & 0xFF;
        v <<= 8;
        v |= bytes[offset+3] & 0xFF;
        return v;
    }

    public static long toLong(byte[] bytes) {
        checkBytesNotNull(bytes);
        return toLong(bytes, 0, bytes.length);
    }

    public static long toLong(byte[] bytes, int offset, int length) {
        checkBytes(bytes, offset, length, 8);
        long v = 0;
        v |= bytes[offset] & 0xFF;
        v <<= 8;
        v |= bytes[offset+1] & 0xFF;
        v <<= 8;
        v |= bytes[offset+2] & 0xFF;
        v <<= 8;
        v |= bytes[offset+3] & 0xFF;
        v <<= 8;
        v |= bytes[offset+4] & 0xFF;
        v <<= 8;
        v |= bytes[offset+5] & 0xFF;
        v <<= 8;
        v |= bytes[offset+6] & 0xFF;
        v <<= 8;
        v |= bytes[offset+7] & 0xFF;
        return v;
    }
}