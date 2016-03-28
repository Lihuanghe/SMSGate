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
package es.rickyepoderi.wbxml.stream.events;

import es.rickyepoderi.wbxml.stream.WbXmlStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.StartDocument;

/**
 * <p>Implementation of the StartDocument event in the wbxml-stream library.</p>
 * 
 * @author ricky
 */
public class WbXmlStartDocumentEvent extends WbXmlEvent implements StartDocument {
    
    /**
     * version of the document.
     */
    private String version = null;
    
    /**
     * encoding of the document.
     */
    private String encoding = null;
    
    /**
     * standalone.
     */
    private boolean standalone = false;
    
    /**
     * standaloneSet.
     */
    private boolean standaloneSet = false;
    
    /**
     * Constructor that creates the event from a stream positioned in the
     * START_DOCUMENT.
     * @param stream The stream positioned at START_DOCUMENT.
     */
    public WbXmlStartDocumentEvent(WbXmlStreamReader stream) {
        super(stream);
        if (getEventType() != XMLStreamConstants.START_DOCUMENT) {
            throw new IllegalStateException("Not at START_DOCUMENT position!");
        }
        this.version = stream.getVersion();
        this.encoding = stream.getEncoding();
        this.standalone = stream.isStandalone();
        this.standaloneSet = stream.standaloneSet();
    }
    
    /**
     * Returns the system ID of the XML data-
     * @return the system ID, defaults to ""
     */
    @Override
    public String getSystemId() {
        return this.getDefinition().getXmlPublicId();
    }

    /**
     * Returns the encoding style of the XML data. It returns the charset
     * defined in the WBXML document.
     * @return the character encoding, defaults to "UTF-8"
     */
    @Override
    public String getCharacterEncodingScheme() {
        return encoding;
    }

    /**
     * Returns true if CharacterEncodingScheme was set in the encoding 
     * declaration of the document
     * @return always true
     */
    @Override
    public boolean encodingSet() {
        return true;
    }

    /**
     * Returns if this XML is standalone
     * @return Always return false
     */
    @Override
    public boolean isStandalone() {
        return standalone;
    }

    /**
     * Returns true if the standalone attribute was set in the encoding 
     * declaration of the document.
     * @return always return false
     */
    @Override
    public boolean standaloneSet() {
        return standaloneSet;
    }

    /**
     * Returns the version of XML of this XML stream. It always return "1.0"
     * no matter the version of the wbxml document.
     * @return the version of XML, defaults to "1.0"
     */
    @Override
    public String getVersion() {
        return version;
    }
    
    /**
     * The event representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return new StringBuilder("StartDocument: ")
                .append(this.encoding)
                .toString();
    }
}
