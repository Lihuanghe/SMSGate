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

import es.rickyepoderi.wbxml.document.WbXmlAttribute;
import es.rickyepoderi.wbxml.document.WbXmlElement;
import es.rickyepoderi.wbxml.stream.WbXmlStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * <p>Implementation of the StartElement event in the wbxml-stream library.</p>
 * @author ricky
 */
public class WbXmlStartElementEvent extends WbXmlEvent implements StartElement {
    
    /**
     * The element at the StartElement.
     */
    private WbXmlElement element = null;
    
    /**
     * The namecontext read from the stream.
     */
    private NamespaceContext nsctx = null;
    
    /**
     * The attributes read from the stream.
     */
    List<WbXmlAttributeEvent> attrs = null;
    
    /**
     * Constructor based in the stream reader which is positioned at StartElement.
     * @param stream The stream reader being read.
     */
    public WbXmlStartElementEvent(WbXmlStreamReader stream) {
        super(stream);
        if (getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new IllegalStateException("Not at START_ELEMENT position!");
        }
        element = stream.getCurrentElement();
        nsctx = stream.getNamespaceContext();
        attrs = new ArrayList<WbXmlAttributeEvent>(element.getAttributes().size());
        for (WbXmlAttribute attr: element.getAttributes()) {
            WbXmlAttributeEvent event = new WbXmlAttributeEvent(attr, stream);
            attrs.add(event);
        }
    }
    
    /**
     * Get the name of this event. It is obtained from the WbXmlElement and
     * the definition.
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
     * Returns an Iterator of non-namespace declared attributes declared on this 
     * START_ELEMENT, returns an empty iterator if there are no attributes. The 
     * iterator must contain only implementations of the javax.xml.stream.Attribute 
     * interface. Attributes are fundamentally unordered and may not be reported 
     * in any order.
     * @return a readonly Iterator over Attribute interfaces, or an empty iterator
     */
    @Override
    public Iterator getAttributes() {
        return attrs.iterator();
    }

    /**
     * Returns an Iterator of namespaces declared on this element. This Iterator 
     * does not contain previously declared namespaces unless they appear on the 
     * current START_ELEMENT. Therefore this list may contain redeclared namespaces 
     * and duplicate namespace declarations. Use the getNamespaceContext() method 
     * to get the current context of namespace declarations.
     * The iterator must contain only implementations of the 
     * javax.xml.stream.Namespace interface.
     * A Namespace isA Attribute. One can iterate over a list of namespaces as 
     * a list of attributes. However this method returns only the list of 
     * namespaces declared on this START_ELEMENT and does not include the 
     * attributes declared on this START_ELEMENT. Returns an empty iterator 
     * if there are no namespaces.
     * This implementation always return empty list cos the elements are always
     * prefixed and with namespaces defined.
     * @return a readonly Iterator over Namespace interfaces, or an empty iterator
     */
    @Override
    public Iterator getNamespaces() {
        return Collections.emptyIterator();
    }

    /**
     * Returns the attribute referred to by this name.
     * @param qname the qname of the desired name
     * @return the attribute corresponding to the name value or null
     */
    @Override
    public Attribute getAttributeByName(QName qname) {
        Iterator<WbXmlAttributeEvent> i = this.getAttributes();
        while (i.hasNext()) {
            WbXmlAttributeEvent event = i.next();
            if (qname.equals(event.getName())) {
                return event;
            }
        }
        return null;
    }

    /**
     * Gets a read-only namespace context. If no context is available this 
     * method will return an empty namespace context. The NamespaceContext 
     * contains information about all namespaces in scope for this StartElement.
     * @return the current namespace context
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        return nsctx;
    }

    /**
     * Gets the value that the prefix is bound to in the context of this element. 
     * Returns null if the prefix is not bound in this context
     * @param namespaceURI the prefix to lookup
     * @return the uri bound to the prefix or null
     */
    @Override
    public String getNamespaceURI(String namespaceURI) {
        return nsctx.getPrefix(namespaceURI);
    }
    
    /**
     * The event representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return new StringBuilder("StartElement: ")
                .append(this.element)
                .toString();
    }
}
