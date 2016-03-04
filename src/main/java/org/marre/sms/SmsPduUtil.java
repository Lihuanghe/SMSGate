/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Boris von Loesch
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.marre.sms;

import java.io.*;

/**
 * Various functions to encode and decode strings
 * 
 * @author Markus Eriksson
 */
public final class SmsPduUtil
{
    public static final char EXT_TABLE_PREFIX = 0x1B;

    /**
     * Default alphabet table according to GSM 03.38.
     * 
     * See http://www.unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT
     */
    public static final char[] GSM_DEFAULT_ALPHABET_TABLE = {
            //  0 '@', '?', '$', '?', '?', '?', '?', '?',
            '@', 163, '$', 165, 232, 233, 249, 236,
            //  8 '?', '?', LF, '?', '?', CR, '?', '?',
            242, 199, 10, 216, 248, 13, 197, 229,
            // 16 'delta', '_', 'phi', 'gamma', 'lambda', 'omega', 'pi', 'psi',
            0x394, '_', 0x3a6, 0x393, 0x39b, 0x3a9, 0x3a0, 0x3a8,
            // 24 'sigma', 'theta', 'xi', 'EXT', '?', '?', '?', '?',
            0x3a3, 0x398, 0x39e, 0xa0, 198, 230, 223, 201,
            // 32 ' ', '!', '"', '#', '?', '%', '&', ''',
            ' ', '!', '"', '#', 164, '%', '&', '\'',
            // 40 '(', ')', '*', '+', ',', '-', '.', '/',
            '(', ')', '*', '+', ',', '-', '.', '/',
            // 48 '0', '1', '2', '3', '4', '5', '6', '7',
            '0', '1', '2', '3', '4', '5', '6', '7',
            // 56 '8', '9', ':', ';', '<', '=', '>', '?',
            '8', '9', ':', ';', '<', '=', '>', '?',
            // 64 '?', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            161, 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            // 72 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            // 80 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            // 88 'X', 'Y', 'Z', '?', '?', '?', '?', '?',
            'X', 'Y', 'Z', 196, 214, 209, 220, 167,
            // 96 '?', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            191, 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            // 104 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            // 112 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            // 120 'x', 'y', 'z', '?', '?', '?', '?', '?',
            'x', 'y', 'z', 228, 246, 241, 252, 224};

    /**
     * Some alternative character encodings.
     * 
     * The table is encoded as pairs with unicode value and gsm charset value.
     * <br>
     * Ex:
     * 
     * <pre>
     * char unicode = GSM_DEFAULT_ALPHABET_ALTERNATIVES[i * 2];char gsm = GSM_DEFAULT_ALPHABET_ALTERNATIVES[i*2+1];
     *  
     * </pre>
     * 
     * See http://www.unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT
     */
    public static final char[] GSM_DEFAULT_ALPHABET_ALTERNATIVES = {
            // LATIN CAPITAL LETTER C WITH CEDILLA (see note above)
            0x00c7, 0x09,
            // GREEK CAPITAL LETTER ALPHA
            0x0391, 0x41,
            // GREEK CAPITAL LETTER BETA
            0x0392, 0x42,
            // GREEK CAPITAL LETTER ETA
            0x0397, 0x48,
            // GREEK CAPITAL LETTER IOTA
            0x0399, 0x49,
            // GREEK CAPITAL LETTER KAPPA
            0x039a, 0x4b,
            // GREEK CAPITAL LETTER MU
            0x039c, 0x4d,
            // GREEK CAPITAL LETTER NU
            0x039d, 0x4e,
            // GREEK CAPITAL LETTER OMICRON
            0x039f, 0x4f,
            // GREEK CAPITAL LETTER RHO
            0x03a1, 0x50,
            // GREEK CAPITAL LETTER TAU
            0x03a4, 0x54,
            // GREEK CAPITAL LETTER UPSILON
            0x03a5, 0x55,
            // GREEK CAPITAL LETTER CHI
            0x03a7, 0x58,
            // GREEK CAPITAL LETTER ZETA
            0x0396, 0x5a};

    /**
     * This class isn't intended to be instantiated
     */
    private SmsPduUtil()
    {
    }

    /**
     * Pack the given string into septets
     *  
     */
    public static byte[] getSeptets(String msg)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(140);

        try
        {
            writeSeptets(baos, msg);
            baos.close();
        }
        catch (IOException ex)
        {
            // Should not happen...
            throw new RuntimeException(ex);
        }

        return baos.toByteArray();
    }

    /**
     * Pack the given string into septets.
     * 
     * @param os
     *            Write the septets into this stream
     * @param msg
     *            The message to encode
     * @throws IOException
     *             Thrown when failing to write to os
     */
    public static void writeSeptets(OutputStream os, String msg) throws IOException
    {
        int data = 0;
        int nBits = 0;

        for (int i = 0; i < msg.length(); i++)
        {
            byte gsmChar = toGsmCharset(msg.charAt(i));

            data |= (gsmChar << nBits);
            nBits += 7;

            while (nBits >= 8)
            {
                os.write((char) (data & 0xff));

                data >>>= 8;
                nBits -= 8;
            } // while
        } // for

        // Write remaining byte
        if (nBits > 0)
        {
            os.write(data);
        }
    }

    /**
     * Decodes a 7-bit encoded string from the given byte array
     * 
     * @param data
     *            The byte array to read from
     * @param length
     *            Number of decoded chars to read from the stream
     * @return The decoded string
     */
    public static String readSeptets(byte[] data, int length)
    {
        if (data == null)
        {
            return null;
        }

        try
        {
            return readSeptets(new ByteArrayInputStream(data), length);
        }
        catch (IOException ex)
        {
            // Shouldn't happen since we are reading from a bytearray...
            return null;
        }
    }
    
    /**
     * Decodes a 7-bit encoded string from the stream
     * 
     * @param is
     *            The stream to read from
     * @param length
     *            Number of decoded chars to read from the stream
     * @return The decoded string
     * @throws IOException
     *             when failing to read from is
     */
    public static String readSeptets(InputStream is, int length) throws IOException
    {
        StringBuilder msg = new StringBuilder(160);

        int rest = 0;
        int restBits = 0;

        while (msg.length() < length)
        {
            int data = is.read();

            if (data == -1) 
            { 
                throw new IOException("Unexpected end of stream"); 
            }

            rest |= (data << restBits);
            restBits += 8;

            while ((msg.length() < length) && (restBits >= 7))
            {
                msg.append(fromGsmCharset((byte) (rest & 0x7f)));

                rest >>>= 7;
                restBits -= 7;
            }
        } // for

        return msg.toString();
    }

    /**
     * Writes the given phonenumber to the stream (BCD coded)
     * 
     * @param os
     *            Stream to write to
     * @param number
     *            Number to convert
     * @throws IOException
     *             when failing to write to os
     */
    public static void writeBcdNumber(OutputStream os, String number) throws IOException
    {
        int bcd = 0x00;
        int n = 0;

        // First convert to a "half octet" value
        for (int i = 0; i < number.length(); i++)
        {
            switch (number.charAt(i))
            {
            case '0':
                bcd |= 0x00;
                break;
            case '1':
                bcd |= 0x10;
                break;
            case '2':
                bcd |= 0x20;
                break;
            case '3':
                bcd |= 0x30;
                break;
            case '4':
                bcd |= 0x40;
                break;
            case '5':
                bcd |= 0x50;
                break;
            case '6':
                bcd |= 0x60;
                break;
            case '7':
                bcd |= 0x70;
                break;
            case '8':
                bcd |= 0x80;
                break;
            case '9':
                bcd |= 0x90;
                break;
            case '*':
                bcd |= 0xA0;
                break;
            case '#':
                bcd |= 0xB0;
                break;
            case 'a':
                bcd |= 0xC0;
                break;
            case 'b':
                bcd |= 0xE0;
                break;
            }

            n++;

            if (n == 2)
            {
                os.write(bcd);
                n = 0;
                bcd = 0x00;
            }
            else
            {
                bcd >>= 4;
            }
        }

        if (n == 1)
        {
            bcd |= 0xF0;
            os.write(bcd);
        }
    }

    /**
     * Converts bytes to BCD format
     * 
     * @param is
     *            The byte InputStream
     * @param length
     *            how many
     * @return Decoded number
     */
    public static String readBcdNumber(InputStream is, int length) throws IOException
    {
        byte[] arr = new byte[length];
        is.read(arr, 0, length);
        return readBcdNumber(arr, 0, length);
    }

    /**
     * Converts bytes to BCD format
     * 
     * @param data
     *            bytearray
     * @param length
     *            how many
     * @param offset
     * @return Decoded number
     */
    public static String readBcdNumber(byte[] data, int offset, int length)
    {
        StringBuilder out = new StringBuilder();
        for (int i = offset; i < offset + length; i++)
        {
            int arrb = data[i];
            if ((data[i] & 15) <= 9)
            {
                out.append("").append(data[i] & 15);
            }
            if ((data[i] & 15) == 0xA)
            {
                out.append("*");
            }
            if ((data[i] & 15) == 0xB)
            {
                out.append("#");
            }
            arrb = (arrb >>> 4);
            if ((arrb & 15) <= 9)
            {
                out.append("").append(arrb & 15);
            }
            if ((arrb & 15) == 0xA)
            {
                out.append("*");
            }
            if ((arrb & 15) == 0xB)
            {
                out.append("#");
            }
        }
        return out.toString();
    }

    /**
     * Convert from the GSM charset to a unicode char
     * 
     * @param gsmChar
     *            The gsm char to convert
     * @return Unicode representation of the given gsm char
     */
    public static char fromGsmCharset(byte gsmChar)
    {
        return GSM_DEFAULT_ALPHABET_TABLE[gsmChar];
    }

    /**
     * Converts a unicode string to GSM charset
     * 
     * @param str
     *            String to convert
     * @return The string GSM encoded
     */
    public static byte[] toGsmCharset(String str)
    {
        byte[] gsmBytes = new byte[str.length()];

        for (int i = 0; i < gsmBytes.length; i++)
        {
            gsmBytes[i] = toGsmCharset(str.charAt(i));
        }

        return gsmBytes;
    }

    /**
     * Convert a unicode char to a GSM char
     * 
     * @param ch
     *            The unicode char to convert
     * @return GSM representation of the given unicode char
     */
    public static byte toGsmCharset(char ch)
    {
        // First check through the GSM charset table
        for (int i = 0; i < GSM_DEFAULT_ALPHABET_TABLE.length; i++)
        {
            if (GSM_DEFAULT_ALPHABET_TABLE[i] == ch)
            {
                // Found the correct char
                return (byte) i;
            }
        }

        // Alternative chars.
        for (int i = 0; i < GSM_DEFAULT_ALPHABET_ALTERNATIVES.length / 2; i += 2)
        {
            if (GSM_DEFAULT_ALPHABET_ALTERNATIVES[i * 2] == ch) 
            { 
                return (byte) (GSM_DEFAULT_ALPHABET_ALTERNATIVES[i * 2 + 1] & 0x7f); 
            }
        }

        // Couldn't find a valid char
        return '?';
    }

    /**
     * 
     * @param src
     * @param srcStart
     * @param dest
     * @param destStart
     * @param destBitOffset
     * @param lengthInBits
     *            In bits
     */
    public static void arrayCopy(byte[] src, int srcStart, 
            byte[] dest, int destStart,int destBitOffset, 
            int lengthInBits)
    {
        int c = 0;
        int nBytes = lengthInBits / 8;
        int nRestBits = lengthInBits % 8;

        for (int i = 0; i < nBytes; i++)
        {
            c |= ((src[srcStart + i] & 0xff) << destBitOffset);
            dest[destStart + i] |= (byte) (c & 0xff);
            c >>>= 8;
        }

        if (nRestBits > 0)
        {
            c |= ((src[srcStart + nBytes] & (0xff >> (8-nRestBits))) << destBitOffset);
        }
        if ((nRestBits + destBitOffset) > 0)
        {
            dest[destStart + nBytes] |= c & 0xff;
        }
    }
}
