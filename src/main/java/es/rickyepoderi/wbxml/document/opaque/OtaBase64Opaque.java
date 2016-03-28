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
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * <p>OTA definition has a property which is an icon (binary image). In
 * XML the Base64 representation of the binary is used.</p>
 * 
 * @author ricky
 */
public class OtaBase64Opaque implements OpaqueAttributePlugin {

    /**
     * Encode method that encodes a opaque data. The Base64 is decoded in case 
     * of ICON in order to save into the WBXML the binary image.
     * 
     * @param encoder The encoder used in the encoding process
     * @param element The element which content or attribute is being encoded 
     * @param attr The attribute which is being encoded
     * @param value The value string is being encoded
     * @throws IOException Some error in the encoding process
     */
    @Override
    public void encode(WbXmlEncoder encoder, WbXmlElement element, 
            WbXmlAttribute attr, String value) throws IOException {
        boolean isIcon = false;
        // only value for <PARM NAME="ICON" VALUE="xxx"/> should be encoded
        if (element.getTag().equals("PARM")) {
            WbXmlAttribute name = element.getAttribute("NAME");
            if (name != null && name.getValue().equals("ICON")) {
                isIcon = true;
                BASE64Decoder dec = new BASE64Decoder();
                encoder.writeOpaque(dec.decodeBuffer(value));
            }
        }
        if (!isIcon) {
            // not encode just common attribute encode
            encoder.encodeAttributeValue(value);
        }
    }

    /**
     * Parse method that parses an opaque data. The binary is encoded into
     * Base64 (the new lines are removed).
     * 
     * @param parser The parser doing the parsing
     * @param data The data read from the opaque
     * @return The string value the opaque represents
     * @throws IOException Some error in the parsing
     */
    @Override
    public String parse(WbXmlParser parser, byte[] data) throws IOException {
        // get the data and show it as Base64
        BASE64Encoder enc = new BASE64Encoder();
        return enc.encode(data).replaceAll(System.getProperty("line.separator"), "");
    }
    
}
