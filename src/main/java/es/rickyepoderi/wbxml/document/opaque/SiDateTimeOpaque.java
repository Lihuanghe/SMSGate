/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *    
 * Linking this library statically or dynamically with other modules 
 * is making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *    
 * As a special exception, the copyright holders of this library give 
 * you permission to link this library with independent modules to 
 * produce an executable, regardless of the license terms of these 
 * independent modules, and to copy and distribute the resulting 
 * executable under terms of your choice, provided that you also meet, 
 * for each linked independent module, the terms and conditions of the 
 * license of that module.  An independent module is a module which 
 * is not derived from or based on this library.  If you modify this 
 * library, you may extend this exception to your version of the 
 * library, but you are not obligated to do so.  If you do not wish 
 * to do so, delete this exception statement from your version.
 *
 * Project: github.com/rickyepoderi/wbxml-stream
 * 
 */
package es.rickyepoderi.wbxml.document.opaque;

import es.rickyepoderi.wbxml.document.OpaqueAttributePlugin;
import es.rickyepoderi.wbxml.document.WbXmlAttribute;
import es.rickyepoderi.wbxml.document.WbXmlElement;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import es.rickyepoderi.wbxml.document.WbXmlParser;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * <p>The WAP Service Indication (SI) Specification describes that the
 * %Datetime; entity should be encoded as an opaque attribute. This value
 * is originally an ISO8601 date time string value. The specification
 * in the chapter <em>8.2.2. Encoding of %Datetime;</em> says the following:</p>
 * 
 * <p>%Datetime; data MUST be encoded as OPAQUE data with each number in the 
 * string represented by its 4-bit binary value. Any non-numerical characters 
 * ("T", "Z", "-", and ":") are discarded. Trailing zeros (from right to left) 
 * MUST be pair-wise omitted.</p>
 * 
 * <p>For example, "1999-04-30T06:40:00Z" is encoded into six octets as follows:<br/>
 * Number "1" "9" "9" "9" "0" "4" "3" "0" "0" "6" "4" "0" "0" "0"</br>
 * Binary value 0001 1001 1001 1001 0000 0100 0011 0000 0000 0110 0100 0000 0000 0000</br>
 * Octet (hex) 00011001 (19) 10011001 (99) 00000100 (04) 00110000 (30) 00000110 (06) 01000000 (40) omitted
 * </p>
 * 
 * @author ricky
 */
public class SiDateTimeOpaque implements OpaqueAttributePlugin {

    /**
     * Utility method that transforms a an ascii array representing hexa
     * data in a real hexa array. The resulting array is half the size of
     * the one passed as parameter.
     * @param array The array representing an hexa array but using ascii
     * @return The real hexa array (half sized)
     */
    static public byte[] charArrayToHexArray(char[] array) {
        // convert ascii into numeric values
        byte[] ascii = new byte[(array.length % 2 == 0)? array.length : array.length + 1];
        for (int i = 0; i < ascii.length; i++) {
            if (i < ascii.length) {
                if ('A' <= array[i] && array[i] <= 'F') {
                    ascii[i] = (byte) (array[i] - 'A');
                } else if ('a' <= array[i] && array[i] <= 'f') {
                    ascii[i] = (byte) (array[i] - 'a');
                } else if ('0' <= array[i] && array[i] <= '9') {
                    ascii[i] = (byte) (array[i] - '0');
                } else {
                    // bad hexa data
                    ascii[i] = 0x0;
                }
            } else {
                ascii[i] = 0x0;
            }
        }
        // convert numeric to hexa
        byte[] result = new byte[array.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) ((ascii[i * 2] * 16) | ascii[i * 2 + 1]);
        }
        // delete trailing zeros
        int i;
        for (i = result.length - 1; i >= 0 && result[i] == 0; i--);
        if (i < result.length - 1) {
            result = Arrays.copyOf(result, i+1);
        }
        return result;
    }
    
    
    
    /**
     * Utility method that transforms a hexa byte array into an ascii array.
     * Obviously the resulting array is twice the length of the input 
     * array (a byte in hexa is represented by two digits "0-9A-F" in the
     * ascii resulting array).
     * @param array The hexa (normal) byte array to convert in ascii
     * @return The ascii array with the hexa values in characters
     */
    static public char[] hexArrayToCharArray(byte[] array) {
        // duplicate the byte array
        char[] buffer = new char[array.length * 2];
        for (int i = array.length - 1; i >= 0; i--) {
            byte b = (byte) ((array[i] & 0x0F) % 16);
            char c = (b < 10)? (char) ((byte) ('0' + b)) :
                    (char) ((byte) ('A' + b - 10));
            buffer[i * 2 + 1] = c;
            b = (byte) (((array[i] & 0xF0) >> 4) % 16);
            c = (b < 10)? (char) ((byte) ('0' + b)) :
                    (char) ((byte) ('A' + b - 10));
            buffer[i * 2] = c;
        }
        return buffer;
    }
    
    /**
     * Method to encode the date time as specified in the SI specification. The
     * string is cleaned from non digit values and then passed to hex ascii
     * values.
     * @param encoder The encoder which is performing the encoding process
     * @param element The element which attribute is being encoded 
     * @param attr The attribute which value is being encoded
     * @param value The value (it should be a string content)
     * @throws IOException Some error writing to the stream
     */
    @Override
    public void encode(WbXmlEncoder encoder, WbXmlElement element, 
            WbXmlAttribute attr, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            return;
        }
        // iterate over the value and remove the string
        // Format: YYYY-MM-DDTHH:mm:ssZ
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!Character.isDigit(ch)) {
                if (ch != 'T' && ch != 'Z' && ch != '-' && ch != ':') {
                    throw new IOException(String.format("Invalid date format '%s'!", value));
                }
            } else {
                sb.append(ch);
            }
        }
        // write the data in ASCII charset (only 0-9 data)
        byte[] bytes = charArrayToHexArray(sb.toString().toCharArray());
        encoder.writeOpaque(bytes);
    }

    /**
     * Method that parses the binary data to reconstruct the ISO8601 date time
     * string. The method just gets the bunch of bytes and reconstryct the
     * string.
     * @param parser The parser which is performing the decoding/parsing process
     * @param data The data read from the opaque value
     * @return The string content representing the date time
     * @throws IOException
     */
    @Override
    public String parse(WbXmlParser parser, byte[] data) throws IOException {
        if (data == null) {
            return null;
        }
        String value = new String(hexArrayToCharArray(data));
        // The value can be YYYYMMDD, YYYYMMDDHH, YYYYMMDDHHmm or YYYYMMDDHHmmss
        String year = "0000";
        if (value.length() >= 4) {
            year = value.substring(0, 4);
        }
        String month = "00";
        if (value.length() >= 6) {
            month = value.substring(4, 6);
        }
        String day = "00";
        if (value.length() >= 8) {
            day = value.substring(6, 8);
        }
        String hour = "00";
        if (value.length() >= 10) {
            hour = value.substring(8, 10);
        }
        String minute = "00";
        if (value.length() >= 12) {
            minute = value.substring(10, 12);
        }
        String second = "00";
        if (value.length() >= 14) {
            second = value.substring(12, 14);
        }
        return new StringBuilder(year)
                .append("-")
                .append(month)
                .append("-")
                .append(day)
                .append("T")
                .append(hour)
                .append(":")
                .append(minute)
                .append(":")
                .append(second)
                .append("Z")
                .toString();
    }
    
}
