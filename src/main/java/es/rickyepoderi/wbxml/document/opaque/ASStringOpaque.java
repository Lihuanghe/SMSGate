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
 * <p>Microsoft Exchange ActiveSync uses opaque for two elements:</p>
 * 
 * <ul>
 * <li><a href="http://msdn.microsoft.com/en-us/library/ee159339%28EXCHG.80%29.aspx">ConversationId</a></li>
 * <li><a href="http://msdn.microsoft.com/en-us/library/ee203955%28EXCHG.80%29.aspx">ConversationIndex</a></li>
 * </ul>
 * 
 * <p>It seems that in the XSD are just string type. So these elements are going
 * to be treated just as simple strings.</a>
 * 
 * @author ricky
 */
public class ASStringOpaque implements OpaqueContentPlugin {

    /**
     * The encoding is just passing the string bytes as an opaque.
     * @param encoder The encoder being used
     * @param ewlement The element which content or attribute is being encoded 
     * @param content The content to add to the WBXML document
     * @throws IOException IOException Some error
     */
    @Override
    public void encode(WbXmlEncoder encoder, WbXmlElement ewlement, WbXmlContent content) throws IOException {
        if (!content.isString()) {
            throw new IOException("The content is not a String!");
        }
        String value = content.getString();
        if (value != null) {
            encoder.writeOpaque(value.getBytes(encoder.getIanaCharset().getCharset()));
        }
    }

    /**
     * Method that parses the string.
     * 
     * @param parser The parser which is performing the decoding/parsing process
     * @param data The data read from the opaque value
     * @return The string content representing the date time
     * @throws IOException
     */
    @Override
    public WbXmlContent parse(WbXmlParser parser, byte[] data) throws IOException {
        String value =  new String(data, parser.getDocument().getCharset().getCharset());
        WbXmlContent content = new WbXmlContent(value);
        //System.err.println(value);
        return content;
    }
    
}
