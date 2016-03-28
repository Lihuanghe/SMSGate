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

import es.rickyepoderi.wbxml.document.OpaqueContentPlugin;
import es.rickyepoderi.wbxml.document.WbXmlContent;
import es.rickyepoderi.wbxml.document.WbXmlElement;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import es.rickyepoderi.wbxml.document.WbXmlParser;
import java.io.IOException;

/**
 *
 * <p>The WV language encodes using opaque data the DateTime tags. The problem
 * is the datetime can be opaque or not (normal string). In theory the 
 * string is a ISO8601 DateTime string but in the binary format it has a 
 * quite strange final byte. For this reason my plugin do not encode as
 * opaque the DateTimes (it just go through normal string processing) but
 * I try to parse/decode opaque data in documents.</p>
 * 
 * <p>Date and time can be encoded as OPAQUE data or as a string as specified 
 * in [ISO8601]. For string encoding, refer to section 6.4 String on page 29.</p>
 * 
 * <p>OPAQUE encoding: the OPAQUE global token (0xC3) is followed by an 
 * mb_u_int32 that contains the length of the date bytes, then the actual 
 * bytes of the encoded date. The time is always in 24-hour format; there is no 
 * need to indicate a.m. or p.m.</p>
 * 
 * <p>The encoding of the date bytes is as follows:</p>
 * 
 * <ul>
 * <li>The first 2 bits are reserved, and both must be 0.</li>
 * <li>Year is encoded by 12 bits (0 to 4095)</li>
 * <li>Month is encoded by 4 bits (1 to 12)</li>
 * <li>Day is encoded by 5 bits (1 to 31)</li>
 * <li>Hour is encoded by 5 bits (0 to 23)</li>
 * <li>Minute is encoded by 6 bits (0 to 59)</li>
 * <li>Second is encoded by 6 bits (0 to 59)</li>
 * <li>Time zone is encoded in 1 byte [ISO8601]????</li>
 * </ul>
 * 
 * <p>This takes exactly 6 bytes (48 bits).
 * Example: The date and time at 16:58:59 on the 25th of September in 2001 in time zone ‘Z’ would be encoded as:
 * 0x06 0x1F 0x46 0x73 0x0E 0xBB 0x5A</p>
 * 
 * @author ricky
 */
public class WVDateTimeOpaque implements OpaqueContentPlugin {

    /**
     * Parse the data getting the bytes as specified in the WV documentation.
     * The letter is just appended at the end (it is not known if A is +01
     * or what.
     * @param data The 6 bytes of the WV encoding
     * @return The string in ISO8601 but with a letter (only Z is commented in the iso)
     */
    private String parseBytes(byte[] data) {
        int year = (((data[0] & 0x3F) << 6) + ((data[1] >> 2) & 0x3F));
        int month = (((data[1] & 0x03) << 2) | ((data[2] >> 6) & 0x03));
        int day = ((data[2] >> 1) & 0x1F);
        int hour = (((data[2] & 0x01) << 4) | ((data[3] >> 4) & 0x0F));
        int min = (((data[3] & 0x0F) << 2) | ((data[4] >> 6) & 0x03));
        int sec = (data[4] & 0x3F);
        String timezone = "";
        char c = (char) data[5];
        if (Character.isUpperCase(c)) {
            timezone = new StringBuilder(c).toString();
        }
        String res = String.format("%04d%02d%02dT%02d%02d%02d%s", year, month, day, hour, min, sec, timezone);
        //System.err.println("RICKKKKKKKY: " + c);
        //System.err.println(WbXmlDefinition.formatUInt8Char(data[5]));
        //System.err.println("RICKKKKKKKY: " + res);
        return res;
    }
 
    /**
     * Encoding is never done. The string is passed as it is (no cheking)
     * @param encoderType The encoder being used
     * @param element The element which content or attribute is being encoded 
     * @param content The content to add to the WBXML document
     * @throws IOException Some error
     */
    @Override
    public void encode(WbXmlEncoder encoderType, WbXmlElement element, WbXmlContent content) throws IOException {
        if (!content.isString()) {
            throw new IOException("The content is not a String!");
        }
        // always like a string
        // it is a fucking nightmare understanding what to do
        encoderType.encode(content);
    }

    /**
     * Parse the data getting the bytes as specified in the WV documentation.
     * The letter is just appended at the end (it is not known if A is +01
     * or what.
     * @param parser The parse doing the parsing/decoding process
     * @param data The 6 bytes of the WV encoding
     * @return The string content with the date in ISO8601
     * @throws IOException  Some error
     */
    @Override
    public WbXmlContent parse(WbXmlParser parser, byte[] data) throws IOException {
        if (data.length != 6) {
            throw new IOException("A WV opaque DateTime is always 6 byte in length!");
        }
        return new WbXmlContent(parseBytes(data));
    }
}
