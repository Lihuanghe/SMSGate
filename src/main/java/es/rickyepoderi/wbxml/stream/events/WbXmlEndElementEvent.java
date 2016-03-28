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

import es.rickyepoderi.wbxml.document.WbXmlElement;
import es.rickyepoderi.wbxml.stream.WbXmlStreamReader;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.EndElement;

/**
 * <p>Implementation of the EndElement event in the wbxml-stream library.</p>
 * @author ricky
 */
public class WbXmlEndElementEvent extends WbXmlEvent implements EndElement {
    
    /**
     * The element at the EndElement.
     */
    WbXmlElement element = null;
    
    /**
     * Constructor based on the stream reader which is at a EndELement position.
     * @param stream The stream being read and positioned at EndElement
     */
    public WbXmlEndElementEvent(WbXmlStreamReader stream) {
        super(stream);
        if (getEventType() != XMLStreamConstants.END_ELEMENT) {
            throw new IllegalStateException("Not at END_ELEMENT position!");
        }
        element = stream.getCurrentElement();
    }

    /**
     * Get the name of this event
     * @return the qualified name of this event
     */
    @Override
    public QName getName() {
        QName name;
        if (element.isPrefixed()) {
            String namespaceUri = this.getDefinition().getNamespaceURIWithLinked(element.getTagPrefix());
            name = new QName(namespaceUri, element.getTagWithoutPrefix(), element.getTagPrefix());
        } else {
            name = new QName(element.getTag());
        }
        return name;
    }

    /**
     * Returns an Iterator of namespaces that have gone out of scope. Returns 
     * an empty iterator if no namespaces have gone out of scope. It always
     * return the empty list.
     * @return an Iterator over Namespace interfaces, or an empty iterator
     */
    @Override
    public Iterator getNamespaces() {
        return Collections.emptyIterator();
    }
    
    /**
     * The event representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return new StringBuilder("EndElement: ")
                .append(this.element)
                .toString();
    }
}
