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
package es.rickyepoderi.wbxml.stream;

import com.sun.org.apache.xerces.internal.util.XMLChar;
import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.definition.WbXmlNamespaceDef;
import es.rickyepoderi.wbxml.document.WbXmlAttribute;
import es.rickyepoderi.wbxml.document.WbXmlContent;
import es.rickyepoderi.wbxml.document.WbXmlElement;
import es.rickyepoderi.wbxml.document.WbXmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * <p>The XMLStreamReader interface allows forward, read-only access to XML. 
 * It is designed to be the lowest level and most efficient way to read XML data
 * and the interface is explained in 
 * <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/stream/XMLStreamReader.html">Java SE javadoc</a>.</p>
 * 
 * <p>The implementation in WBXML always creates initially a WbXmlDocument
 * java object in memory (it can be said that first the java/memory representation
 * is created in the constructor part  and then the java object reprsentation
 * is iterated. The parser is used to implement the XMLStreamReader.</p>
 * 
 * <p>The iteration over the parser is maintained with a event (the current
 * event the reader is right now) and a little ElementIndex which is
 * just the current element the reader is parsing and an index (the index 
 * marks the content inside the element we are treating). A queue of parent
 * elements is also maintained to iterate over all the elements in document 
 * parsed.</p>
 * 
 * <p>Implementing the WbXmlEventReader it was discovered that ATTRIBUTE 
 * event type should be returned as many times as attributes are in the
 * element. Currently it is only returned once (it was thought that). Besides
 * it was discovered that underlaying implentations never use this event. They
 * when START_ELEMENT is received get all the attributes using getAttributeXXX
 * methods. For that ATTRIBUTE event type is never returned. Please use
 * START_ELEMENT instead.</p>
 * 
 * @author ricky
 */
public class WbXmlStreamReader implements XMLStreamReader {

    /**
     * Logger of the class.
     */
    protected static final Logger log = Logger.getLogger(WbXmlStreamReader.class.getName());
    
    /**
     * Namespace context that is initialized with the context used for every
     * language definition used by the parser.
     */
    private WbXmlNamespaceContext nsctx;
    
    /**
     * The event the reader is right now.
     */
    private int event;
    
    /**
     * The parser resulted after the parsing of the stream.
     */
    private WbXmlParser parser;
    
    /**
     * Queue of elements to ietarte over all childs.
     */
    private Deque<ElementIndex> parents;
    
    /**
     * Current element index (current element and index of the content).
     */
    private ElementIndex elementIndex;
    
    /**
     * The input stream which is parsed with the parser.
     */
    private InputStream is;
    
    /**
     * Little class to maintain the position of the reader. The position is
     * the current element and the current index in the contents of that element.
     */
    private class ElementIndex {
        WbXmlElement currentElement;
        Integer index;
    }
    
    /**
     * Constructor using the parser. This is used when the parser already has
     * parsed a stream. The constructor set the event type to START:DOCUMENT.
     * @param parser The parser which already has a parsed document inside
     */
    public WbXmlStreamReader(WbXmlParser parser) {
        this.is = null;
        this.elementIndex = new ElementIndex();
        this.parents = new ArrayDeque<ElementIndex>();
        this.parser = parser;
        this.nsctx = new WbXmlNamespaceContext();
        // add namespaces
        for (WbXmlNamespaceDef nsDef : parser.getDefinition().getNamespaces()) {
            nsctx.addPrefix(nsDef.getPrefix(), nsDef.getNamespace());
        }
        for (WbXmlDefinition def: parser.getDefinition().getLinkedDefs()) {
            for (WbXmlNamespaceDef nsDef: def.getNamespaces()) {
                nsctx.addPrefix(nsDef.getPrefix(), nsDef.getNamespace());
            }
        }
        event = START_DOCUMENT;
    }
    
    /**
     * Constructor using the input stream. The parser is created and the
     * stream is parsed inside the constructor. The constructor set the event 
     * type to START:DOCUMENT.
     * @param is The input stream with the WBXML document
     * @param definition The fixed definition to use
     * @throws IOException Some error parsing the stream
     */
    public WbXmlStreamReader(InputStream is, WbXmlDefinition definition) throws IOException {
        this.is = is;
        this.elementIndex = new ElementIndex();
        this.parents = new ArrayDeque<ElementIndex>();
        parser = new WbXmlParser(this.is);
        parser.parse(definition);
        this.nsctx = new WbXmlNamespaceContext();
        // add namespaces
        for (WbXmlNamespaceDef nsDef : parser.getDefinition().getNamespaces()) {
            nsctx.addPrefix(nsDef.getPrefix(), nsDef.getNamespace());
        }
        for (WbXmlDefinition def: parser.getDefinition().getLinkedDefs()) {
            for (WbXmlNamespaceDef nsDef: def.getNamespaces()) {
                nsctx.addPrefix(nsDef.getPrefix(), nsDef.getNamespace());
            }
        }
        event = START_DOCUMENT;
    }
    
    /**
     * Constructor using the input stream. Created via input stream and 
     * not specifying a fixed definition.
     * @param is The input stream with the WBXML document
     * @throws IOException Some error parsing the stream
     */
    public WbXmlStreamReader(InputStream is) throws IOException {
        this(is, null);
    }
    
    /**
     * Getter the parser for event readers.
     * @return The parser used for reading
     */
    public WbXmlParser getParser() {
        return parser;
    }
    
    /**
     * Getter for the current element being parsed
     * @return The current element being parsed
     */
    public WbXmlElement getCurrentElement() {
        return this.elementIndex.currentElement;
    }
    
    /**
     * Getter for the namespace context
     * @return The namespace context created for the parsing
     */
    public WbXmlNamespaceContext getContext() {
        return this.nsctx;
    }
    
    
    //
    // XMLStreamReader methods
    //

    /**
     * Returns true if the cursor points to a character data event. In the
     * WbXmlStreamReader only CHARACTERS, ENTITY_REFERENCE (no CDATA is managed).
     * @return true if the cursor points to character data, false otherwise
     */
    @Override
    public boolean isCharacters() {
        log.log(Level.FINE, "isCharacters(): {0}", event == CHARACTERS);
        return event == CHARACTERS;
    }

    /**
     * Returns true if the cursor points to a start tag (otherwise false)
     * @return true if the cursor points to a start tag, false otherwise
     */
    @Override
    public boolean isStartElement() {
        log.log(Level.FINE, "isStartElement(): {0}", event == START_ELEMENT);
        return event == START_ELEMENT;
    }

    /**
     * Returns true if the cursor points to an end tag (otherwise false)
     * @return true if the cursor points to an end tag, false otherwise
     */
    @Override
    public boolean isEndElement() {
        log.log(Level.FINE, "isEndElement(): {0}", event == END_ELEMENT);
        return event == END_ELEMENT;
    }

    /**
     * Returns true if the cursor points to a character data event that consists of all whitespace.
     * NOTE: XMLChar.isSpace(char) is used which is a internal method.
     * @return true if the cursor points to all whitespace, false otherwise
     */
    @Override
    public boolean isWhiteSpace() {
        log.log(Level.FINE, "isWhiteSpace():");
        if (event == CHARACTERS) {
            char[] ch = getTextCharacters();
            final int start = this.getTextStart();
            final int end = start + this.getTextLength();
            for (int i = start; i < end; i++) {
                if (!XMLChar.isSpace(ch[i])) {
                    log.log(Level.FINE, "isWhiteSpace(): {0}", false);
                    return false;
                }
            }
            log.log(Level.FINE, "isWhiteSpace(): {0}", true);
            return true;
        } else {
            throw new IllegalStateException("Not in CHARACTERS state");
        }
    }

    /**
     * Returns a boolean which indicates if this attribute was created by default.
     * It returns true if current element has an attribute at this index
     * @param index the position of the attribute
     * @return true if this is a default attribute
     */
    @Override
    public boolean isAttributeSpecified(int index) {
        log.log(Level.FINE, "isAttributeSpecified({0})", index);
        if (event != ATTRIBUTE && event != START_ELEMENT) {
            throw new IllegalStateException("isAttributeSpecified called in another state!");
        } else {
            return elementIndex.currentElement.attributesSize() > index;
        }
    }
    
    /**
     * Private method that searches the next element inside the current element.
     * If the index is null we are right now in the start element and the next
     * event could be attributes of the first content. If the index is not null
     * we advance in the next content. That content could be a string, entity,
     * pi or another element. If the content is an element the parent is added
     * to the queue and start element is returned. If it is the other possibilities
     * just the correspondent event is treated.
     * 
     * @param attributes boolean that marks if we want to read attributes or not
     * @return The next element following the commented process
     */
    private int nextInElement(boolean attributes) {
        if (attributes && !elementIndex.currentElement.isAttributesEmpty()) {
            // attributes
            elementIndex.index = null;
            event = ATTRIBUTE;
        } else if (elementIndex.index != null &&
                elementIndex.currentElement.contentsSize() > elementIndex.index) {
            // get the index content and guess event type
            WbXmlContent content = elementIndex.currentElement.getContent(elementIndex.index);
            if (content.isEntity()) {
                event = ENTITY_REFERENCE;
            } else if (content.isString()) {
                event = CHARACTERS;
            } else if (content.isPi()) {
                event = PROCESSING_INSTRUCTION;
            } else {
                parents.push(elementIndex);
                elementIndex = new ElementIndex();
                elementIndex.currentElement = content.getElement();
                elementIndex.index = null;
                event = START_ELEMENT;
            }
        } else {
            event = END_ELEMENT;
        }
        return event;
    }

    /**
     * Get next parsing event - a processor may return all contiguous character 
     * data in a single chunk, or it may split it into several chunks. If the 
     * property javax.xml.stream.isCoalescing is set to true element content 
     * must be coalesced and only one CHARACTERS event must be returned for 
     * contiguous element content or CDATA Sections. By default entity 
     * references must be expanded and reported transparently to the application. 
     * An exception will be thrown if an entity reference cannot be expanded. 
     * If element content is empty (i.e. content is "") then no CHARACTERS 
     * event will be reported.
     * 
     * <p>This method marks the current element and index using the
     * elementIndex structure. Besides a queue of parents element index is
     * maintained to cross over all element hierarchy.</p>
     * 
     * <p>The WbXMLStreamReader only manages the following states:</p>
     * 
     * <ul>
     * <li>START_DOCUMENT</li>
     * <li>PROCESSING_INSTRUCTION</li>
     * <li>START_ELEMENT</li>
     * <li>ATTRIBUTE</li>
     * <lI>CHARACTERS</li>
     * <li>END_ELEMENT</li>
     * <li>SPACE</li>
     * <li>END_DOCUMENT</li>
     * <li>ENTITY_REFERENCE</li>
     * </ul>
     * 
     * <p>Therefore the following element does no matter in this stream reader:</p>
     * 
     * <ul>
     * <li>CDATA (CHARACTERS are used always).</li>
     * <li>COMMENT (no comments in WBXML).</li>
     * <li>DTD (no DTD section)</li>
     * <li>ENTITY_DECLARATION</li>
     * <li>NAMESPACE</li>
     * <li>NOTATION DECLARATION</li>
     * </ul>
     * 
     * @return the integer code corresponding to the current parse event
     * @throws XMLStreamException if there is an error processing the underlying XML source
     */
    @Override
    public int next() throws XMLStreamException {
        log.fine("next()");
        // possible states: START_DOCUMENT PROCESSING_INSTRUCTION 
        //                  START_ELEMENT ATTRIBUTE CHARACTERS END_ELEMENT
        //                  SPACE  END_DOCUMENT 
        //                  ENTITY_REFERENCE
        if (event == START_DOCUMENT) {
            // point to the first element in the doc or pi
            event = START_ELEMENT;
            elementIndex.currentElement = parser.getDocument().getBody().getElement();
        } else if (event == START_ELEMENT) {
            // the next can be END_ELEMENT, ATTRIBUTE, CAHARACTERS, PROCESSING_INSTRUCTION
            elementIndex.index = 0;
            // Never return ATTRIBUTE
            event = nextInElement(false);
        } else if (event == ATTRIBUTE) {
            // the nest event can be CHARACTERS PROCESSING_INSTRUCTION
            elementIndex.index = 0;
            event = nextInElement(false);
        } else if (event == CHARACTERS) {
            elementIndex.index++;
            event = nextInElement(false);
        } else if (event == SPACE) {
            elementIndex.index++;
            event = nextInElement(false);
        } else if (event == ENTITY_REFERENCE) {
            elementIndex.index++;
            event = nextInElement(false);
        } else if (event == PROCESSING_INSTRUCTION) {
            elementIndex.index++;
            event = nextInElement(false);
        } else if (event == END_ELEMENT) {
            // get the saved element in the queue
            if (parents.isEmpty()) {
                event = END_DOCUMENT;
            } else {
                elementIndex = parents.pop();
                elementIndex.index++;
                event = nextInElement(false);
            }
        } else if (event == END_DOCUMENT) {
            throw new XMLStreamException("End of coument reached!");
        } else {
            throw new XMLStreamException("Invalid event state!");
        }
        log.log(Level.FINE, "next(): {0}", event);
        return event;
    }
    
    /**
     * Get the value of a feature/property from the underlying implementation.
     * NOTE: always returns null cos no properties defined til the moment
     * @param name The name of the property, may not be null
     * @return The value of the property
     * @throws IllegalArgumentException 
     */
    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        log.log(Level.FINE, "getProperty({0})", name);
        if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
            // is namespace aware if namespaces are defined
            return this.parser.getDefinition().getNamespaces().size() > 0;
        } else {
            return null;
        }
    }

    /**
     * Test if the current event is of the given type and if the namespace 
     * and name match the current namespace and name of the current event. 
     * If the namespaceURI is null it is not checked for equality, if the 
     * localName is null it is not checked for equality.
     * 
     * NOTE: Not implemented, it throws an exception
     * 
     * @param type the event type
     * @param namespaceURI the uri of the event, may be null
     * @param localName the localName of the event, may be null
     * @throws XMLStreamException if the required values are not matched.
     */
    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     <p>* Reads the content of a text-only element, an exception is thrown if 
     * this is not a text-only element. Regardless of value of 
     * javax.xml.stream.isCoalescing this method always returns coalesced 
     * content.</p>
     * <p>Precondition: the current event is START_ELEMENT. </p>
     * <p>Postcondition: the current event is the corresponding END_ELEMENT.</p>
     * 
     * NOTE: It is just implemented like it is commented in the interface!!!
     * 
     * @return The string of element texts
     * @throws XMLStreamException if the current event is not a START_ELEMENT or if a non text element is encountered
     */
    @Override
    public String getElementText() throws XMLStreamException {
        log.log(Level.FINE, "getElementText()");
        if (getEventType() != START_ELEMENT) {
            throw new XMLStreamException(
                    "parser must be on START_ELEMENT to read next text");
        }
        int eventType = next();
        StringBuilder buf = new StringBuilder();
        while (eventType != END_ELEMENT) {
            if (eventType == CHARACTERS
                    || eventType == CDATA
                    || eventType == SPACE
                    || eventType == ENTITY_REFERENCE) {
                buf.append(getText());
            } else if (eventType == PROCESSING_INSTRUCTION
                    || eventType == COMMENT) {
                // skipping
            } else if (eventType == END_DOCUMENT) {
                throw new XMLStreamException(
                        "unexpected end of document when reading element text content");
            } else if (eventType == START_ELEMENT) {
                throw new XMLStreamException(
                        "element text content may not contain START_ELEMENT");
            } else {
                throw new XMLStreamException(
                        "Unexpected event type " + eventType);
            }
            eventType = next();
        }
        log.log(Level.FINE, "getElementText(): {0}", buf.toString());
        return buf.toString();
    }

    /**
     * <p>Skips any white space (isWhiteSpace() returns true), COMMENT, or 
     * PROCESSING_INSTRUCTION, until a START_ELEMENT or END_ELEMENT is reached. 
     * If other than white space characters, COMMENT, PROCESSING_INSTRUCTION, 
     * START_ELEMENT, END_ELEMENT are encountered, an exception is thrown. 
     * This method should be used when processing element-only content seperated 
     * by white space.</p>
     * <p>Precondition: none </p>
     * <p>Postcondition: the current event is START_ELEMENT or END_ELEMENT and 
     * cursor may have moved over any whitespace event.</p>
     * 
     * NOTE: Implemented like commented in the interface
     * 
     * @return the event type of the element read (START_ELEMENT or END_ELEMENT)
     * @throws XMLStreamException if the current event is not white space, 
     * PROCESSING_INSTRUCTION, START_ELEMENT or END_ELEMENT
     */
    @Override
    public int nextTag() throws XMLStreamException {
        log.log(Level.FINE, "nextTag()");
        int eventType = next();
        while ((eventType == CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == CDATA && isWhiteSpace()) // skip whitespace
                || eventType == SPACE
                || eventType == PROCESSING_INSTRUCTION
                || eventType == COMMENT) {
            eventType = next();
        }
        if (eventType != START_ELEMENT && eventType != END_ELEMENT) {
            throw new XMLStreamException(String.format("expected start or end tag and found %d", eventType));
        }
        log.log(Level.FINE, "nextTag(): {0}", eventType);
        return eventType;
    }

    /**
     * Returns true if there are more parsing events and false if there are no 
     * more events. This method will return false if the current state of the 
     * XMLStreamReader is END_DOCUMENT
     * @return true if there are more events, false otherwise
     * @throws XMLStreamException if there is a fatal error detecting the next state
     */
    @Override
    public boolean hasNext() throws XMLStreamException {
        log.log(Level.FINE, "hasNext(): {0}", event != END_DOCUMENT);
        return event != END_DOCUMENT;
    }

    /**
     * Frees any resources associated with this Reader. This method does not 
     * close the underlying input source.
     * @throws XMLStreamException if there are errors freeing associated resources
     */
    @Override
    public void close() throws XMLStreamException {
        this.parser = null;
        //try {
        //    if (is != null) {
        //        is.close();
        //    }
        //} catch (IOException e) {
        //    throw new XMLStreamException(e);
        //}
    }

    /**
     * Return the uri for the given prefix. The uri returned depends on the 
     * current state of the processor. In this stream reader 
     * implementation all the prefixes for the languages used in the
     * parser are added into the NamespaceContext property.
     * 
     * <p>NOTE:The 'xml' prefix is bound as defined in Namespaces in XML 
     * specification to "http://www.w3.org/XML/1998/namespace"</p>
     * <p>NOTE: The 'xmlns' prefix must be resolved to following namespace 
     * http://www.w3.org/2000/xmlns/</p>
     * @param prefix The prefix to lookup, may not be null
     */
    @Override
    public String getNamespaceURI(String prefix) {
        String namespaceURI = nsctx.getNamespaceURI(prefix);
        log.log(Level.FINE, "getNamespaceURI(): {0}", namespaceURI);
        return namespaceURI;
    }

    /**
     * Returns the normalized attribute value of the attribute with the 
     * namespace and localName If the namespaceURI is null the namespace is 
     * not checked for equality.
     * 
     * @param namespaceURI the namespace of the attribute
     * @param localName the local name of the attribute, cannot be null
     * @return returns the value of the attribute , returns null if not found
     */
    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        log.log(Level.FINE, "getAttributeValue({0}, {1})", 
                new Object[]{namespaceURI, localName});
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        // locate the prefix if used
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            String prefix = parser.getDefinition().getPrefixWithLinked(namespaceURI);
            localName = new StringBuilder(prefix).append(":").append(localName).toString();
        }
        // get the atrribute
        WbXmlAttribute attr = elementIndex.currentElement.getAttribute(localName);
        String result = null;
        if (attr != null) {
            result = attr.getValue();
        }
        log.log(Level.FINE, "getAttributeValue(): {0}", result);
        return result;
    }

    /**
     * Returns the count of attributes on this START_ELEMENT, this method is 
     * only valid on a START_ELEMENT or ATTRIBUTE. This count excludes namespace
     * definitions. Attribute indices are zero-based.
     * 
     * @return returns the number of attributes
     */
    @Override
    public int getAttributeCount() {
        log.log(Level.FINE, "getAttributeCount()");
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        log.log(Level.FINE, "getAttributeCount(): {0}", elementIndex.currentElement.attributesSize());
        return elementIndex.currentElement.attributesSize();
    }

    /**
     * Returns the qname of the attribute at the provided index.
     * @param i the position of the attribute
     * @return QName of the attribute
     */
    @Override
    public QName getAttributeName(int i) {
        log.log(Level.FINE, "getAttributeName({0})", i);
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        WbXmlAttribute attr = elementIndex.currentElement.getAttribute(i);
        QName result = null;
        if (attr != null) {
            if (attr.isPrefixed()) {
                // get the prefix and the namespace
                String prefix  = attr.getNamePrefix();
                String namespaceURI = parser.getDefinition().getPrefixWithLinked(prefix);
                result = new QName(namespaceURI, attr.getNameWithoutPrefix(), prefix);
            } else {
                result = new QName(attr.getName());
            }
        }
        log.log(Level.FINE, "getAttributeName(): {0}", result);
        return result;
    }

    /**
     * Returns the namespace of the attribute at the provided index.
     * @param i the position of the attribute
     * @return the namespace URI (can be null)
     */
    @Override
    public String getAttributeNamespace(int i) {
        log.log(Level.FINE, "getAttributeNamespace({0})", i);
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        WbXmlAttribute attr = elementIndex.currentElement.getAttribute(i);
        String result = null;
        if (attr != null) {
            if (attr.isPrefixed()) {
                // it has a prefix
                result = parser.getDefinition().getNamespaceURIWithLinked(attr.getNamePrefix());
            } else {
                result = XMLConstants.NULL_NS_URI;
            }
        }
        log.log(Level.FINE, "getAttributeNamespace(): {0}", result);
        return result;
    }

    /**
     * Returns the localName of the attribute at the provided index
     * @param i the position of the attribute
     * @return the localName of the attribute
     */
    @Override
    public String getAttributeLocalName(int i) {
        log.log(Level.FINE, "getAttributeLocalName({0})", i);
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        WbXmlAttribute attr = elementIndex.currentElement.getAttribute(i);
        String result = null;
        if (attr != null) {
            result = attr.getNameWithoutPrefix();
        }
        log.log(Level.FINE, "getAttributeLocalName(): {0}", result);
        return result;
    }

    /**
     * Returns the prefix of this attribute at the provided index.
     * @param i the position of the attribute
     * @return the prefix of the attribute
     */
    @Override
    public String getAttributePrefix(int i) {
        log.log(Level.FINE, "getAttributePrefix({0})", i);
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        WbXmlAttribute attr = elementIndex.currentElement.getAttribute(i);
        String result = null;
        if (attr != null) {
            if (attr.isPrefixed()) {
                // it has a prefix
                result = attr.getNamePrefix();
            } else {
                result = XMLConstants.DEFAULT_NS_PREFIX;
            }
        }
        log.log(Level.FINE, "getAttributePrefix(): {0}", result);
        return result;
    }

    /**
     * Returns the XML type of the attribute at the provided index
     * NOTE. Always returns the "" empty type
     * @param i the position of the attribute
     * @return the XML type of the attribute
     */
    @Override
    public String getAttributeType(int i) {
        log.log(Level.FINE, "getAttributeType({0}): ", i);
        // TODO what here
        return "";
    }

    /**
     * Returns the value of the attribute at the index
     * @param i the position of the attribute
     * @return the attribute value
     */
    @Override
    public String getAttributeValue(int i) {
        log.log(Level.FINE, "getAttributeValue({0})", i);
        if (event != START_ELEMENT && event != ATTRIBUTE) {
            throw new IllegalStateException("Not in START_ELEMENT or ATTRIBUTE");
        }
        WbXmlAttribute attr = elementIndex.currentElement.getAttribute(i);
        String result = null;
        if (attr != null) {
            result =  attr.getValue();
        }
        log.log(Level.FINE, "getAttributeValue(): {0}", result);
        return result;
    }

    /**
     * <p>Returns the count of namespaces declared on this START_ELEMENT or 
     * END_ELEMENT, this method is only valid on a START_ELEMENT, END_ELEMENT 
     * or NAMESPACE. Always return 0</p>
     * 
     * <p>This implementation just returns the namespaces for the root 
     * elements in each namespace used. The idea is the root element defines
     * all the elements in the language definition.</p>
     * 
     * @return returns the number of namespace declarations on this specific element
     */
    @Override
    public int getNamespaceCount() {
        log.fine("getNamespaceCount()");
        int result = 0;
        log.log(Level.FINE, "getNamespaceCount(): {0}", result);
        return result;
    }

    /**
     * Returns the prefix for the namespace declared at the index. Returns null 
     * if this is the default namespace declaration. As it is said only root
     * language definition element has namespaces. Always return null.
     * 
     * @param i the position of the namespace declaration
     * @return returns the namespace uri
     */
    @Override
    public String getNamespacePrefix(int i) {
        log.log(Level.FINE, "getNamespacePrefix({0})", i);
        String result = null;
        log.log(Level.FINE, "getNamespaceCount(): {0}", result);
        return result;
    }

    /**
     * Returns the uri for the namespace declared at the index. As it is said
     * only root elements of the language definitions used by the parser
     * has the namespaces defined. Always return null.
     * 
     * @param i the position of the namespace declaration
     * @return returns the namespace uri
     */
    @Override
    public String getNamespaceURI(int i) {
        log.log(Level.FINE, "getNamespaceURI({0})", i);
        String result = null;
        log.log(Level.FINE, "getNamespaceCount(): {0}", result);
        return result;
    }

    /**
     * Returns a read only namespace context for the current position. 
     * The context is transient and only valid until a call to next() changes 
     * the state of the reader.
     * @return return a namespace context
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        log.fine("getNamespaceContext()");
        return nsctx;
    }

    /**
     * Returns an integer code that indicates the type of the event the cursor is pointing to.
     * @return The current event type
     */
    @Override
    public int getEventType() {
        return event;
    }

    /**
     * <p>Returns the current value of the parse event as a string, this returns 
     * the string value of a CHARACTERS event, returns the value of a COMMENT, 
     * the replacement value for an ENTITY_REFERENCE, the string value of a 
     * CDATA section, the string value for a SPACE event, or the String value 
     * of the internal subset of the DTD. If an ENTITY_REFERENCE has been 
     * resolved, any character data will be reported as CHARACTERS events.</p>
     * 
     * @return the current text or null
     */
    @Override
    public String getText() {
        log.log(Level.FINE, "getText()");
        if (event != CHARACTERS && event != SPACE && event != ENTITY_REFERENCE) {
            throw new IllegalStateException("Not in text event");
        }
        log.log(Level.FINE, "getText(): {0}", elementIndex.currentElement.getContent(elementIndex.index).getString());
        return elementIndex.currentElement.getContent(elementIndex.index).getString();
    }

    /**
     * Returns an array which contains the characters from this event. This 
     * array should be treated as read-only and transient. I.e. the array will 
     * contain the text characters until the XMLStreamReader moves on to the 
     * next event. Attempts to hold onto the character array beyond that time 
     * or modify the contents of the array are breaches of the contract for 
     * this interface.
     * @return the current text or an empty array
     */
    @Override
    public char[] getTextCharacters() {
        String result = getText();
        return (result != null)? result.toCharArray(): new char[0];
    }

    /**
     * Gets the the text associated with a CHARACTERS, SPACE or CDATA event. 
     * Text starting a "sourceStart" is copied into "target" starting at 
     * "targetStart". Up to "length" characters are copied. The number of 
     * characters actually copied is returned. The "sourceStart" argument must 
     * be greater or equal to 0 and less than or equal to the number of 
     * characters associated with the event. Usually, one requests text 
     * starting at a "sourceStart" of 0. If the number of characters actually 
     * copied is less than the "length", then there is no more text. Otherwise, 
     * subsequent calls need to be made until all text has been retrieved. 
     * For example: int length = 1024; char[] myBuffer = new char[ length ]; 
     * for ( int sourceStart = 0 ; ; sourceStart += length ) { int nCopied = 
     * stream.getTextCharacters( sourceStart, myBuffer, 0, length ); if 
     * (nCopied < length) break; } XMLStreamException may be thrown if there 
     * are any XML errors in the underlying source. The "targetStart" argument 
     * must be greater than or equal to 0 and less than the length of "target", 
     * Length must be greater than 0 and "targetStart + length" must be less 
     * than or equal to length of "target".
     * 
     * @param sourceStart the index of the first character in the source array to copy
     * @param target the destination array
     * @param targetStart the start offset in the target array
     * @param length the number of characters to copy
     * @return the number of characters actually copied
     * @throws XMLStreamException if the underlying XML source is not well-formed
     */
    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        getText().getChars(sourceStart,sourceStart+length,target,targetStart);
        return length;
    }

    /**
     * Returns the offset into the text character array where the first character (of this text event) is stored.
     * @return  Always 0
     */
    @Override
    public int getTextStart() {
        return 0;
    }

    /**
     * Returns the length of the sequence of characters for this Text event within the text character array
     * @return The length of the text
     */
    @Override
    public int getTextLength() {
        return getText().length();
    }

    /**
     * Return input encoding if known or null if unknown
     * @return The encoding of the WBXML document
     */
    @Override
    public String getEncoding() {
        log.log(Level.FINE, "getEncoding()");
        return parser.getCharset().name();
    }

    /**
     * Return true if the current event has text, false otherwise The following 
     * events have text: CHARACTERS,DTD ,ENTITY_REFERENCE, COMMENT, SPACE
     * @return true if current event is CHARACTERS or ENTITY_REFERENCE (only available text events in WBXML reader)
     */
    @Override
    public boolean hasText() {
        log.log(Level.FINE, "hasText()");
        return event == CHARACTERS || event == ENTITY_REFERENCE;
    }

    /**
     * Return the current location of the processor. If the Location is unknown 
     * the processor should return an implementation of Location that 
     * returns -1 for the location and null for the publicId and systemId. 
     * The location information is only valid until next() is called
     * @return An empty location (not implemented)
     */
    @Override
    public Location getLocation() {
        log.log(Level.FINE, "getLocation()");
        return new Location() {
            @Override
            public int getCharacterOffset() {
                return 0;
            }

            @Override
            public int getColumnNumber() {
                return 0;
            }

            @Override
            public int getLineNumber() {
                return -1;
            }

            @Override
            public String getPublicId() {
                return null;
            }

            @Override
            public String getSystemId() {
                return null;
            }
        };
    }

    /**
     * Returns a QName for the current START_ELEMENT or END_ELEMENT event
     * @return the QName for the current START_ELEMENT or END_ELEMENT event
     */
    @Override
    public QName getName() {
        log.log(Level.FINE, "getName()");
        QName qname;
        if (event == START_ELEMENT || event == END_ELEMENT) {
            log.log(Level.FINE, "tag={0}", elementIndex.currentElement.getTag());
            if (elementIndex.currentElement.isPrefixed()) {
                String prefix = elementIndex.currentElement.getTagPrefix();
                String namespaceURI = parser.getDefinition().getNamespaceURIWithLinked(prefix);
                qname = new QName(namespaceURI, elementIndex.currentElement.getTagWithoutPrefix(), prefix);
            } else {
                qname = new QName(elementIndex.currentElement.getTag());
            }
        } else {
            throw new IllegalStateException("Not in START_ELEMENT, END_ELEMENT");
        }
        log.log(Level.FINE, "getName(): {0}", qname);
        return qname;
    }

    /**
     * Returns the (local) name of the current event. For START_ELEMENT or 
     * END_ELEMENT returns the (local) name of the current element. For 
     * ENTITY_REFERENCE it returns entity name. The current event must be 
     * START_ELEMENT or END_ELEMENT, or ENTITY_REFERENCE
     * @return the localName
     */
    @Override
    public String getLocalName() {
        log.log(Level.FINE, "getLocalName()");
        String localName;
        if (event == START_ELEMENT || event == END_ELEMENT) {
            localName = elementIndex.currentElement.getTagWithoutPrefix();
        } else if (event == ENTITY_REFERENCE) {
            localName = elementIndex.currentElement.getContent(elementIndex.index).getString();
        } else {
            throw new IllegalStateException("Not in START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE");
        }
        log.log(Level.FINE, "getLocalName(): {0}", localName);
        return localName;
    }

    /**
     * returns true if the current event has a name (is a START_ELEMENT or END_ELEMENT) returns false otherwise
     * @return true if current event is a START_ELEMENT or END_ELEMENT
     */
    @Override
    public boolean hasName() {
        log.log(Level.FINE, "hasName(): {0}", event == START_ELEMENT || event == END_ELEMENT);
        return event == START_ELEMENT || event == END_ELEMENT;
    }

    /**
     * the current event is a START_ELEMENT or END_ELEMENT this method returns 
     * the URI of the prefix or the default namespace. Returns null if the event
     * does not have a prefix.
     * @return the URI bound to this elements prefix, the default namespace, or null
     */
    @Override
    public String getNamespaceURI() {
        log.log(Level.FINE, "getNamespaceURI()");
        String namespace = XMLConstants.DEFAULT_NS_PREFIX;
        if (event == START_ELEMENT || event == END_ELEMENT) {
            if (elementIndex.currentElement.isPrefixed()) {
                String prefix = elementIndex.currentElement.getTagPrefix();
                namespace = parser.getDefinition().getNamespaceURIWithLinked(prefix);
            } else {
                namespace = XMLConstants.NULL_NS_URI;
            }
        }
        log.log(Level.FINE, "getNamespaceURI(): {0}", namespace);
        return namespace;
    }

    /**
     * Returns the prefix of the current event or null if the event does not have a prefix
     * @return the prefix or null
     */
    @Override
    public String getPrefix() {
        log.log(Level.FINE, "getPrefix()");
        String prefix = null;
        if (event == START_ELEMENT || event == END_ELEMENT) {
            if (elementIndex.currentElement.isPrefixed()) {
                prefix = elementIndex.currentElement.getTagPrefix();
            } else {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            }
        }
        log.log(Level.FINE, "getPrefix(): {0}", prefix);
        return prefix;
    }

    /**
     * Get the xml version declared on the xml declaration Returns null if none was declared.
     * NOTE: Always returns 1.0
     * @return the XML version or null
     */
    @Override
    public String getVersion() {
        log.log(Level.FINE, "getVersion(): 1.0");
        return "1.0";
    }

    /**
     * Get the standalone declaration from the xml declaration.
     * It always return false.
     * @return true if standalone was set in the document, or false otherwise
     */
    @Override
    public boolean isStandalone() {
        return false;
    }

    /**
     * Checks if standalone was set in the document.
     * It always return false.
     * @return true if standalone was set in the document, or false otherwise
     */
    @Override
    public boolean standaloneSet() {
        return false;
    }

    /**
     * Returns the character encoding declared on the xml declaration Returns null if none was declared
     * @return the encoding declared in the document or null
     */
    @Override
    public String getCharacterEncodingScheme() {
        log.fine("getCharacterEncodingScheme()");
        return parser.getCharset().name();
    }

    /**
     * Get the target of a processing instruction.
     * NOTE: Unsupported, it throws and exception
     * @return the target or null
     */
    @Override
    public String getPITarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get the data section of a processing instruction
     * @return the data or null
     */
    @Override
    public String getPIData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
