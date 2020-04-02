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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;

import io.netty.util.CharsetUtil;

/**
 * Various functions to encode and decode strings
 * 
 * @author Markus Eriksson
 */
public final class SmsPduUtil
{
     /**
     * This class isn't intended to be instantiated
     */
    private SmsPduUtil()
    {
    }
    private static String gsmstr = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà^{}\\[~]|€";
    
    public static boolean hasUnGsmchar(String content) {
    	
		for (int i = 0; i < content.length(); i++) {
			if(gsmstr.indexOf(content.charAt(i)) < 0)
				return true;
		}
    	return false;
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
	 * Convert a list of septet values into an octet stream, with a number of empty bits at the start.
	 * @param septets
	 * @param skipBits
	 * @return
	 */
	public static byte[] septetStream2octetStream(byte[] septets) {
		int octetLength = (int) Math.ceil(((septets.length * 7)) / 8.0);
		byte[] octets = new byte[octetLength];
		
		for (int i = 0; i < septets.length; i++) {
			for (int j = 0; j < 7; j++) {
				if ((septets[i] & (1 << j)) != 0) {
					int bitIndex = (i * 7) + j;
					octets[bitIndex >>> 3] |= 1 << (bitIndex & 7);
				}
			}
		}
		
		return octets;
		
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
    	
    	byte[] bytes = stringToUnencodedSeptets(msg);
    	os.write(septetStream2octetStream(bytes));
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
    public static String readSeptets(byte[] data,int charlength)
    {
    
        if (data == null)
        {
            return null;
        }

       byte[] ba= LongMessageFrameHolder.octetStream2septetStream(data,charlength);
       return unencodedSeptetsToString(ba);
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
     * Converts a unicode string to GSM charset
     * 
     * @param str
     *            String to convert
     * @return The string GSM encoded
     */

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
	private static final char[][] grcAlphabetRemapping = { { '\u0386', '\u0041' }, // GREEK CAPITAL LETTER ALPHA WITH TONOS
		{ '\u0388', '\u0045' }, // GREEK CAPITAL LETTER EPSILON WITH TONOS
		{ '\u0389', '\u0048' }, // GREEK CAPITAL LETTER ETA WITH TONOS
		{ '\u038A', '\u0049' }, // GREEK CAPITAL LETTER IOTA WITH TONOS
		{ '\u038C', '\u004F' }, // GREEK CAPITAL LETTER OMICRON WITH TONOS
		{ '\u038E', '\u0059' }, // GREEK CAPITAL LETTER UPSILON WITH TONOS
		{ '\u038F', '\u03A9' }, // GREEK CAPITAL LETTER OMEGA WITH TONOS
		{ '\u0390', '\u0049' }, // GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
		{ '\u0391', '\u0041' }, // GREEK CAPITAL LETTER ALPHA
		{ '\u0392', '\u0042' }, // GREEK CAPITAL LETTER BETA
		{ '\u0393', '\u0393' }, // GREEK CAPITAL LETTER GAMMA
		{ '\u0394', '\u0394' }, // GREEK CAPITAL LETTER DELTA
		{ '\u0395', '\u0045' }, // GREEK CAPITAL LETTER EPSILON
		{ '\u0396', '\u005A' }, // GREEK CAPITAL LETTER ZETA
		{ '\u0397', '\u0048' }, // GREEK CAPITAL LETTER ETA
		{ '\u0398', '\u0398' }, // GREEK CAPITAL LETTER THETA
		{ '\u0399', '\u0049' }, // GREEK CAPITAL LETTER IOTA
		{ '\u039A', '\u004B' }, // GREEK CAPITAL LETTER KAPPA
		{ '\u039B', '\u039B' }, // GREEK CAPITAL LETTER LAMDA
		{ '\u039C', '\u004D' }, // GREEK CAPITAL LETTER MU
		{ '\u039D', '\u004E' }, // GREEK CAPITAL LETTER NU
		{ '\u039E', '\u039E' }, // GREEK CAPITAL LETTER XI
		{ '\u039F', '\u004F' }, // GREEK CAPITAL LETTER OMICRON
		{ '\u03A0', '\u03A0' }, // GREEK CAPITAL LETTER PI
		{ '\u03A1', '\u0050' }, // GREEK CAPITAL LETTER RHO
		{ '\u03A3', '\u03A3' }, // GREEK CAPITAL LETTER SIGMA
		{ '\u03A4', '\u0054' }, // GREEK CAPITAL LETTER TAU
		{ '\u03A5', '\u0059' }, // GREEK CAPITAL LETTER UPSILON
		{ '\u03A6', '\u03A6' }, // GREEK CAPITAL LETTER PHI
		{ '\u03A7', '\u0058' }, // GREEK CAPITAL LETTER CHI
		{ '\u03A8', '\u03A8' }, // GREEK CAPITAL LETTER PSI
		{ '\u03A9', '\u03A9' }, // GREEK CAPITAL LETTER OMEGA
		{ '\u03AA', '\u0049' }, // GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
		{ '\u03AB', '\u0059' }, // GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
		{ '\u03AC', '\u0041' }, // GREEK SMALL LETTER ALPHA WITH TONOS
		{ '\u03AD', '\u0045' }, // GREEK SMALL LETTER EPSILON WITH TONOS
		{ '\u03AE', '\u0048' }, // GREEK SMALL LETTER ETA WITH TONOS
		{ '\u03AF', '\u0049' }, // GREEK SMALL LETTER IOTA WITH TONOS
		{ '\u03B0', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
		{ '\u03B1', '\u0041' }, // GREEK SMALL LETTER ALPHA
		{ '\u03B2', '\u0042' }, // GREEK SMALL LETTER BETA
		{ '\u03B3', '\u0393' }, // GREEK SMALL LETTER GAMMA
		{ '\u03B4', '\u0394' }, // GREEK SMALL LETTER DELTA
		{ '\u03B5', '\u0045' }, // GREEK SMALL LETTER EPSILON
		{ '\u03B6', '\u005A' }, // GREEK SMALL LETTER ZETA
		{ '\u03B7', '\u0048' }, // GREEK SMALL LETTER ETA
		{ '\u03B8', '\u0398' }, // GREEK SMALL LETTER THETA
		{ '\u03B9', '\u0049' }, // GREEK SMALL LETTER IOTA
		{ '\u03BA', '\u004B' }, // GREEK SMALL LETTER KAPPA
		{ '\u03BB', '\u039B' }, // GREEK SMALL LETTER LAMDA
		{ '\u03BC', '\u004D' }, // GREEK SMALL LETTER MU
		{ '\u03BD', '\u004E' }, // GREEK SMALL LETTER NU
		{ '\u03BE', '\u039E' }, // GREEK SMALL LETTER XI
		{ '\u03BF', '\u004F' }, // GREEK SMALL LETTER OMICRON
		{ '\u03C0', '\u03A0' }, // GREEK SMALL LETTER PI
		{ '\u03C1', '\u0050' }, // GREEK SMALL LETTER RHO
		{ '\u03C2', '\u03A3' }, // GREEK SMALL LETTER FINAL SIGMA
		{ '\u03C3', '\u03A3' }, // GREEK SMALL LETTER SIGMA
		{ '\u03C4', '\u0054' }, // GREEK SMALL LETTER TAU
		{ '\u03C5', '\u0059' }, // GREEK SMALL LETTER UPSILON
		{ '\u03C6', '\u03A6' }, // GREEK SMALL LETTER PHI
		{ '\u03C7', '\u0058' }, // GREEK SMALL LETTER CHI
		{ '\u03C8', '\u03A8' }, // GREEK SMALL LETTER PSI
		{ '\u03C9', '\u03A9' }, // GREEK SMALL LETTER OMEGA
		{ '\u03CA', '\u0049' }, // GREEK SMALL LETTER IOTA WITH DIALYTIKA
		{ '\u03CB', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH DIALYTIKA
		{ '\u03CC', '\u004F' }, // GREEK SMALL LETTER OMICRON WITH TONOS
		{ '\u03CD', '\u0059' }, // GREEK SMALL LETTER UPSILON WITH TONOS
		{ '\u03CE', '\u03A9' } // GREEK SMALL LETTER OMEGA WITH TONOS
};

private static final char[] extAlphabet = {
		'\u000c', //\n FORM FEED
		'\u005e', //^ CIRCUMFLEX ACCENT
		'\u007b', //{ LEFT CURLY BRACKET
		'\u007d', //} RIGHT CURLY BRACKET
		'\\',     //\ REVERSE SOLIDUS
		'\u005b', //[ LEFT SQUARE BRACKET
		'\u007e', //~ TILDE
		'\u005d', //] RIGHT SQUARE BRACKET
		'\u007c', //| VERTICAL LINES
		'\u20ac', //€ EURO SIGN
};

private static final String[] extBytes = { "1b0a", // FORM FEED
		"1b14", // CIRCUMFLEX ACCENT
		"1b28", // LEFT CURLY BRACKET
		"1b29", // RIGHT CURLY BRACKET
		"1b2f", // REVERSE SOLIDUS
		"1b3c", // LEFT SQUARE BRACKET
		"1b3d", // TILDE
		"1b3e", // RIGHT SQUARE BRACKET
		"1b40", // VERTICAL LINES
		"1b65", // EURO SIGN
};

private static final char[] stdAlphabet = {
		'\u0040', //0 @ COMMERCIAL AT
		'\u00A3', //1 £ POUND SIGN
		'\u0024', //2 $ DOLLAR SIGN
		'\u00A5', //3 ¥ YEN SIGN
		'\u00E8', //4 è LATIN SMALL LETTER E WITH GRAVE
		'\u00E9', //5 é LATIN SMALL LETTER E WITH ACUTE
		'\u00F9', //6 ù LATIN SMALL LETTER U WITH GRAVE
		'\u00EC', //7 ì LATIN SMALL LETTER I WITH GRAVE
		'\u00F2', //8 ò LATIN SMALL LETTER O WITH GRAVE
		'\u00C7', //9 Ç LATIN CAPITAL LETTER C WITH CEDILLA
		'\n',     //a LINE FEED
		'\u00D8', //b Ø LATIN CAPITAL LETTER O WITH STROKE
		'\u00F8', //c ø LATIN SMALL LETTER O WITH STROKE
		'\r',     //d CARRIAGE RETURN
		'\u00C5', //e Å LATIN CAPITAL LETTER A WITH RING ABOVE
		'\u00E5', //f å LATIN SMALL LETTER A WITH RING ABOVE
		'\u0394', //10 Δ GREEK CAPITAL LETTER DELTA
		'\u005F', //11 _ LOW LINE
		'\u03A6', //12 Φ GREEK CAPITAL LETTER PHI
		'\u0393', //13 Γ GREEK CAPITAL LETTER GAMMA
		'\u039B', //14 Λ GREEK CAPITAL LETTER LAMDA
		'\u03A9', //15 Ω GREEK CAPITAL LETTER OMEGA
		'\u03A0', //16 Π GREEK CAPITAL LETTER PI
		'\u03A8', //17 Ψ GREEK CAPITAL LETTER PSI
		'\u03A3', //18 Σ GREEK CAPITAL LETTER SIGMA
		'\u0398', //19 Θ GREEK CAPITAL LETTER THETA
		'\u039E', //1a Ξ GREEK CAPITAL LETTER XI
		'\u00A0', //1b ESCAPE TO EXTENSION TABLE (or displayed as NBSP, see
		// note
		// above)
		'\u00C6', //1c Æ LATIN CAPITAL LETTER AE
		'\u00E6', //1d æ LATIN SMALL LETTER AE
		'\u00DF', //1e ß LATIN SMALL LETTER SHARP S (German)
		'\u00C9', //1f É LATIN CAPITAL LETTER E WITH ACUTE
		'\u0020', //20 SPACE
		'\u0021', //! 21 EXCLAMATION MARK
		'\u0022', //" 22 QUOTATION MARK
		'\u0023', //# 23 NUMBER SIGN
		'\u00A4', //¤ 24 CURRENCY SIGN
		'\u0025', //% 25 PERCENT SIGN
		'\u0026', //& 26 AMPERSAND
		'\'',     //' 27 APOSTROPHE
		'\u0028', //( 28 LEFT PARENTHESIS
		'\u0029', //) 29 RIGHT PARENTHESIS
		'\u002A', //* 2a ASTERISK
		'\u002B', //+ 2b PLUS SIGN
		'\u002C', //, 2c COMMA
		'\u002D', //- HYPHEN-MINUS
		'\u002E', //. FULL STOP
		'\u002F', /// SOLIDUS
		'\u0030', //0 DIGIT ZERO
		'\u0031', //1 DIGIT ONE
		'\u0032', //2 DIGIT TWO
		'\u0033', //3 DIGIT THREE
		'\u0034', //4 DIGIT FOUR
		'\u0035', //5 DIGIT FIVE
		'\u0036', //6 DIGIT SIX
		'\u0037', //7 DIGIT SEVEN
		'\u0038', //8 DIGIT EIGHT
		'\u0039', //9 DIGIT NINE
		'\u003A', //: COLON
		'\u003B', //; SEMICOLON
		'\u003C', //< LESS-THAN SIGN
		'\u003D', //= EQUALS SIGN
		'\u003E', //> GREATER-THAN SIGN
		'\u003F', //? 3f QUESTION MARK
		'\u00A1', //¡ 40 INVERTED EXCLAMATION MARK
		'\u0041', // LATIN CAPITAL LETTER A
		'\u0042', // LATIN CAPITAL LETTER B
		'\u0043', // LATIN CAPITAL LETTER C
		'\u0044', // LATIN CAPITAL LETTER D
		'\u0045', // LATIN CAPITAL LETTER E
		'\u0046', // LATIN CAPITAL LETTER F
		'\u0047', // LATIN CAPITAL LETTER G
		'\u0048', // LATIN CAPITAL LETTER H
		'\u0049', // LATIN CAPITAL LETTER I
		'\u004A', // LATIN CAPITAL LETTER J
		'\u004B', // LATIN CAPITAL LETTER K
		'\u004C', // LATIN CAPITAL LETTER L
		'\u004D', // LATIN CAPITAL LETTER M
		'\u004E', // LATIN CAPITAL LETTER N
		'\u004F', // LATIN CAPITAL LETTER O
		'\u0050', // LATIN CAPITAL LETTER P
		'\u0051', // LATIN CAPITAL LETTER Q
		'\u0052', // LATIN CAPITAL LETTER R
		'\u0053', // LATIN CAPITAL LETTER S
		'\u0054', // LATIN CAPITAL LETTER T
		'\u0055', // LATIN CAPITAL LETTER U
		'\u0056', // LATIN CAPITAL LETTER V
		'\u0057', // LATIN CAPITAL LETTER W
		'\u0058', // LATIN CAPITAL LETTER X
		'\u0059', // LATIN CAPITAL LETTER Y
		'\u005A', // LATIN CAPITAL LETTER Z
		'\u00C4', //Ä 5b LATIN CAPITAL LETTER A WITH DIAERESIS
		'\u00D6', //Ö 5c LATIN CAPITAL LETTER O WITH DIAERESIS
		'\u00D1', //Ñ 5d LATIN CAPITAL LETTER N WITH TILDE
		'\u00DC', //Ü 5e LATIN CAPITAL LETTER U WITH DIAERESIS
		'\u00A7', //§ 5f SECTION SIGN
		'\u00BF', //¿ 60 INVERTED QUESTION MARK
		'\u0061', // LATIN SMALL LETTER A
		'\u0062', // LATIN SMALL LETTER B
		'\u0063', // LATIN SMALL LETTER C
		'\u0064', // LATIN SMALL LETTER D
		'\u0065', // LATIN SMALL LETTER E
		'\u0066', // LATIN SMALL LETTER F
		'\u0067', // LATIN SMALL LETTER G
		'\u0068', // LATIN SMALL LETTER H
		'\u0069', // LATIN SMALL LETTER I
		'\u006A', // LATIN SMALL LETTER J
		'\u006B', // LATIN SMALL LETTER K
		'\u006C', // LATIN SMALL LETTER L
		'\u006D', // LATIN SMALL LETTER M
		'\u006E', // LATIN SMALL LETTER N
		'\u006F', // LATIN SMALL LETTER O
		'\u0070', // LATIN SMALL LETTER P
		'\u0071', // LATIN SMALL LETTER Q
		'\u0072', // LATIN SMALL LETTER R
		'\u0073', // LATIN SMALL LETTER S
		'\u0074', // LATIN SMALL LETTER T
		'\u0075', // LATIN SMALL LETTER U
		'\u0076', // LATIN SMALL LETTER V
		'\u0077', // LATIN SMALL LETTER W
		'\u0078', // LATIN SMALL LETTER X
		'\u0079', // LATIN SMALL LETTER Y
		'\u007A', // LATIN SMALL LETTER Z
		'\u00E4', //ä 7b LATIN SMALL LETTER A WITH DIAERESIS
		'\u00F6', //ö 7c LATIN SMALL LETTER O WITH DIAERESIS
		'\u00F1', //ñ 7d LATIN SMALL LETTER N WITH TILDE
		'\u00FC', //ü 7e LATIN SMALL LETTER U WITH DIAERESIS
		'\u00E0', //à 7f LATIN SMALL LETTER A WITH GRAVE
};
	// from Java String to uncompressed septets (GSM characters)
public static byte[] stringToUnencodedSeptets(String s)
{
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	int i, j, k, index;
	char ch;
	k = 0;
	for (i = 0; i < s.length(); i++)
	{
		ch = s.charAt(i);
		index = -1;
		for (j = 0; j < extAlphabet.length; j++)
			if (extAlphabet[j] == ch)
			{
				index = j;
				break;
			}
		if (index != -1) // An extended char...
		{
			baos.write((byte) Integer.parseInt(extBytes[index].substring(0, 2), 16));
			k++;
			baos.write((byte) Integer.parseInt(extBytes[index].substring(2, 4), 16));
			k++;
		}
		else
		// Maybe a standard char...
		{
			index = -1;
			for (j = 0; j < stdAlphabet.length; j++)
				if (stdAlphabet[j] == ch)
				{
					index = j;
					baos.write((byte) j);
					k++;
					break;
				}
			if (index == -1) // Maybe a Greek Char...
			{
				for (j = 0; j < grcAlphabetRemapping.length; j++)
					if (grcAlphabetRemapping[j][0] == ch)
					{
						index = j;
						ch = grcAlphabetRemapping[j][1];
						break;
					}
				if (index != -1)
				{
					for (j = 0; j < stdAlphabet.length; j++)
						if (stdAlphabet[j] == ch)
						{
							index = j;
							baos.write((byte) j);
							k++;
							break;
						}
				}
				else
				// Unknown char replacement...
				{
					baos.write((byte) ' ');
					k++;
				}
			}
		}
	}
	return baos.toByteArray();
}
	
	// from GSM characters to java string
	public static String unencodedSeptetsToString(byte[] bytes)
	{
		StringBuffer text;
		String extChar;
		int i, j;
		text = new StringBuffer();
		for (i = 0; i < bytes.length; i++)
		{
			if (bytes[i] == 0x1b)
			{
				extChar = "1b" + Integer.toHexString(bytes[++i]);
				for (j = 0; j < extBytes.length; j++)
					if (extBytes[j].equalsIgnoreCase(extChar)) text.append(extAlphabet[j]);
			}
			else
			{
				if(bytes[i] >= 0 && bytes[i] < stdAlphabet.length)
					text.append(stdAlphabet[bytes[i]]);
				else
					text.append(new String(new byte[] {bytes[i]},CharsetUtil.ISO_8859_1));

			}
		}
		return text.toString();
	}
	
    public static int countGSMescapechar(String msg){
    	int i = 0;
    	for(int j = 0;j<msg.length();j++){
    		char ch = msg.charAt(j);
    		for (int k = 0; k < extAlphabet.length; k++){
    			if (extAlphabet[k] == ch)
    			{
    				i++;
    				break;
    			}
    		}
    	}
    	return i;
    }
}
