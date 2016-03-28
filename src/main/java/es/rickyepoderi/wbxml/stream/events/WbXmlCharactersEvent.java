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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;

/**
 * <p>Implementation of the Characters event in the wbxml-stream library.</p>
 * @author ricky
 */
public class WbXmlCharactersEvent extends WbXmlEvent implements Characters {

    /**
     * The text in the characters info.
     */
    private String text = null;
    
    /**
     * If this characters are a whitespace.
     */
    private boolean whitespace = false;
    
    /**
     * If the event was a CDATA or simple CAHRACTERS
     */
    private boolean cdata = false;
    
    /**
     * Constructor based on the stream reader position.
     * @param stream The stream in the CHARACTERS event.
     * @throws XMLStreamException Some error reading the data
     */
    public WbXmlCharactersEvent(WbXmlStreamReader stream) throws XMLStreamException {
        super(stream);
        if (getEventType() != XMLStreamConstants.CHARACTERS &&
                getEventType() != XMLStreamConstants.CDATA) {
            throw new IllegalStateException("Not at CHARACTERS or CDATA position!");
        }
        this.text = stream.getText();
        this.whitespace = stream.isWhiteSpace();
        this.cdata = stream.getEventType() == XMLStreamConstants.CDATA;
    }
    
    /**
     * Get the character data of this event
     * @return the text of the data
     */
    @Override
    public String getData() {
        return text;
    }

    /**
     * Returns true if this set of Characters is all whitespace. Whitespace 
     * inside a document is reported as CHARACTERS. This method allows checking 
     * of CHARACTERS events to see if they are composed of only whitespace 
     * characters
     * @return true if it is whitespace characters
     */
    @Override
    public boolean isWhiteSpace() {
        return whitespace;
    }

    /**
     * Returns true if this is a CData section. If this event is CData its event 
     * type will be CDATA If javax.xml.stream.isCoalescing is set to true CDATA 
     * Sections that are surrounded by non CDATA characters will be reported as 
     * a single Characters event. This method will return false in this case.
     * @return true if the event waqs a CDATA
     */
    @Override
    public boolean isCData() {
        return cdata;
    }

    /**
     * Return true if this is ignorableWhiteSpace. If this event is 
     * ignorableWhiteSpace its event type will be SPACE.
     * @return true if it is ignorableWhiteSpace
     */
    @Override
    public boolean isIgnorableWhiteSpace() {
        return whitespace;
    }
    
    /**
     * The event representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return new StringBuilder("Characters: ")
                .append(this.text)
                .toString();
    }
}
