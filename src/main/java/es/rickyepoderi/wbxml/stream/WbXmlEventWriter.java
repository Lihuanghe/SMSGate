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

import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>WbXML implementation for the event writer. It uses internally the
 * stream implementation. It just gets the events passed and transform in
 * normal methods to the stream writer. Easy and simple implementation.</p>
 * 
 * @author ricky
 */
public class WbXmlEventWriter implements XMLEventWriter {
    
    /**
     * Logger of the class.
     */
    protected static final Logger log = Logger.getLogger(WbXmlEventWriter.class.getName());
    
    /**
     * Internal Stream writer.
     */
    private WbXmlStreamWriter stream = null;

    /**
     * Constructor with all the possibilities.
     * @param os The output stream to write
     * @param def The definition to use (if null it is guessed)
     * @param encoderType The encoder type to use
     * @param skipSpaces if the spaces are trimmed
     * @param encoding The encoding to use if not specified
     */
    public WbXmlEventWriter(OutputStream os, WbXmlDefinition def, 
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces, String encoding) {
        stream = new WbXmlStreamWriter(os, def, encoderType, skipSpaces, encoding);
    }
    
    /**
     * Constructor with all the possible features except encoding.
     * @param os The output stream to write
     * @param def The definition to use (if null it is guessed)
     * @param encoderType The encoder type to use
     * @param skipSpaces if the spaces are trimmed
     */
    public WbXmlEventWriter(OutputStream os, WbXmlDefinition def, 
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces) {
        stream = new WbXmlStreamWriter(os, def, encoderType, skipSpaces);
    }
    
    /**
     * Constructor with only the output stream and the definition. The type
     * is defaulted to IF_NEEDED and skip to true.
     * @param os The output stream to write
     * @param def The definition to used (guessed)
     */
    public WbXmlEventWriter(OutputStream os, WbXmlDefinition def) {
        stream = new WbXmlStreamWriter(os, def);
    }
    
    /**
     * Constructor with only the ouput stream. Definition is guessed, the 
     * type of encoding is IF_NEEDED and the spaces are skipped.
     * @param os The output stream to write
     */
    public WbXmlEventWriter(OutputStream os) {
        stream = new WbXmlStreamWriter(os);
    }
    
    /**
     * Frees any resources associated with this stream. The internal 
     * stream flush is called.
     * @throws XMLStreamException  Some error flushing
     */
    @Override
    public void flush() throws XMLStreamException {
        log.log(Level.FINE, "flush()");
        stream.flush();
    }

    /**
     * Frees any resources associated with this stream. The internal
     * stream close is called.
     * @throws XMLStreamException Some error encoding
     */
    @Override
    public void close() throws XMLStreamException {
        log.log(Level.FINE, "close()");
        stream.close();
    }

    /**
     * Add an event to the output stream Adding a START_ELEMENT will open a new 
     * namespace scope that will be closed when the corresponding END_ELEMENT 
     * is written.
     * 
     * <table rules="all" border="2" cellpadding="4">
     * <thead>
     *    <tr>
     *      <th colspan="2" align="center">
     *        Required and optional fields for events added to the writer 
     *      </th>
     *    </tr>
     *  </thead>
     *  <tbody>
     *    <tr>
     *      <th>Event Type</th>
     *      <th>Required Fields</th>
     *      <th>Optional Fields</th>
     *      <th>Required Behavior</th>
     *    </tr>
     *    <tr>
     *      <td> START_ELEMENT  </td>
     *      <td> QName name </td>
     *      <td> namespaces , attributes </td>
     *      <td> A START_ELEMENT will be written by writing the name, 
     *      namespaces, and attributes of the event in XML 1.0 valid
     *      syntax for START_ELEMENTs.  
     *      The name is written by looking up the prefix for
     *      the namespace uri.  The writer can be configured to 
     *      respect prefixes of QNames.  If the writer is respecting
     *      prefixes it must use the prefix set on the QName.  The
     *      default behavior is to lookup the value for the prefix
     *      on the EventWriter's internal namespace context.   
     *      Each attribute (if any)
     *      is written using the behavior specified in the attribute
     *      section of this table.  Each namespace (if any) is written
     *      using the behavior specified in the namespace section of this
     *      table. 
     *      </td>
     *    </tr>
     *    <tr>
     *      <td> END_ELEMENT  </td>
     *      <td> Qname name  </td>
     *      <td> None </td>
     *      <td> A well formed END_ELEMENT tag is written.
     *      The name is written by looking up the prefix for
     *      the namespace uri.  The writer can be configured to 
     *      respect prefixes of QNames.  If the writer is respecting
     *      prefixes it must use the prefix set on the QName.  The
     *      default behavior is to lookup the value for the prefix
     *      on the EventWriter's internal namespace context. 
     *      If the END_ELEMENT name does not match the START_ELEMENT
     *      name an XMLStreamException is thrown.
     *      </td>
     *    </tr>
     *    <tr>
     *      <td> ATTRIBUTE  </td>
     *      <td> QName name , String value </td>
     *      <td> QName type </td>
     *      <td> An attribute is written using the same algorithm
     *           to find the lexical form as used in START_ELEMENT.
     *           The default is to use double quotes to wrap attribute
     *           values and to escape any double quotes found in the
     *           value.  The type value is ignored.
     *      </td>
     *    </tr>
     *    <tr>
     *      <td> NAMESPACE  </td>
     *      <td> String prefix, String namespaceURI, 
     *           boolean isDefaultNamespaceDeclaration
     *     </td>
     *      <td> None  </td>
     *      <td> A namespace declaration is written.  If the 
     *           namespace is a default namespace declaration
     *           (isDefault is true) then xmlns="$namespaceURI"
     *           is written and the prefix is optional.  If 
     *           isDefault is false, the prefix must be declared
     *           and the writer must prepend xmlns to the prefix 
     *           and write out a standard prefix declaration.
     *     </td>
     *    </tr>
     *    <tr>
     *      <td> PROCESSING_INSTRUCTION  </td>
     *      <td>   None</td>
     *      <td>   String target, String data</td>
     *      <td>   The data does not need to be present and may be
     *             null.  Target is required and many not be null.  
     *             The writer
     *             will write data section 
     *             directly after the target, 
     *             enclosed in appropriate XML 1.0 syntax
     *     </td>
     *    </tr>
     *    <tr>
     *      <td> COMMENT  </td>
     *      <td> None  </td>
     *      <td> String comment  </td>
     *      <td> If the comment is present (not null) it is written, otherwise an
     *           an empty comment is written
     *     </td>
     *    </tr>
     *    <tr>
     *      <td> START_DOCUMENT  </td>
     *      <td> None  </td>
     *      <td> String encoding , boolean standalone, String version  </td>
     *      <td> A START_DOCUMENT event is not required to be written to the
     *            stream.  If present the attributes are written inside
     *            the appropriate XML declaration syntax
     *     </td>
     *    </tr>
     *    <tr>
     *      <td> END_DOCUMENT  </td>
     *      <td> None </td>
     *      <td> None  </td>
     *      <td> Nothing is written to the output  </td>
     *    </tr>
     *    <tr>
     *      <td> DTD  </td>
     *      <td> String DocumentTypeDefinition  </td>
     *      <td> None  </td>
     *      <td> The DocumentTypeDefinition is written to the output  </td>
     *    </tr>
     *  </tbody>
     *  </table>
     * 
     * @param event the event to be added
     * @throws XMLStreamException Some error encoding
     */
    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        log.log(Level.FINE, "add() {0}", event);
        switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                StartElement startElement = (StartElement) event;
                stream.writeStartElement(startElement.getName().getPrefix(), 
                        startElement.getName().getLocalPart(),
                        startElement.getName().getNamespaceURI());
                // write attributes
                Iterator<Attribute> ia = startElement.getAttributes();
                while (ia.hasNext()) {
                    Attribute attr = ia.next();
                    stream.writeAttribute(attr.getName().getPrefix(), 
                            attr.getName().getNamespaceURI(),
                            attr.getName().getLocalPart(), 
                            attr.getValue());
                }
                // write namespaces
                Iterator<Namespace> in = startElement.getNamespaces();
                while (in.hasNext()) {
                    Namespace ns = in.next();
                    stream.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
                }
                break;
            case XMLStreamConstants.ATTRIBUTE:
                Attribute attr = (Attribute) event;
                stream.writeAttribute(attr.getName().getPrefix(),
                        attr.getName().getNamespaceURI(),
                        attr.getName().getLocalPart(),
                        attr.getValue());
                break;
            case XMLStreamConstants.END_ELEMENT:
                stream.writeEndElement();
                break;
            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.CHARACTERS:
                Characters chars = (Characters) event;
                if (chars.isCData()) {
                    stream.writeCData(chars.getData());
                } else {
                    stream.writeCharacters(chars.getData());
                }
                break;
            case XMLStreamConstants.NAMESPACE:
                Namespace namespace = (Namespace) event;
                stream.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
                break;
            case XMLStreamConstants.START_DOCUMENT:
                StartDocument startDocument = (StartDocument) event;
                stream.writeStartDocument(startDocument.getCharacterEncodingScheme(), 
                        startDocument.getVersion());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                stream.writeEndDocument();
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                ProcessingInstruction pi = (ProcessingInstruction) event;
                stream.writeProcessingInstruction(pi.getTarget(), pi.getData());
                break;
            case XMLStreamConstants.COMMENT:
                Comment comment = (Comment) event;
                stream.writeComment(comment.getText());
                break;
            case XMLStreamConstants.DTD:
                DTD dtd = (DTD) event;
                stream.writeDTD(dtd.getDocumentTypeDeclaration());
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                EntityReference ref = (EntityReference) event;
                stream.writeEntityRef(ref.getName());
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("The %i event is not supported yet", event.getEventType()));
        }
    }

    /**
     * Adds an entire stream to an output stream, calls next() on the 
     * inputStream argument until hasNext() returns false This should be 
     * treated as a convenience method that will perform the following loop 
     * over all the events in an event reader and call add on each event. 
     * @param reader
     * @throws XMLStreamException 
     */
    @Override
    public void add(XMLEventReader reader) throws XMLStreamException {
        log.log(Level.FINE, "add() {0}", reader);
        // just iterate the reader getting events and write them
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            this.add(event);
        }
    }

    /**
     *  the prefix the uri is bound to.
     * @param uri the uri to look up 
     * @return The prefix for that namespace
     * @throws XMLStreamException Some error
     */
    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        log.log(Level.FINE, "getPrefix() {0}", uri);
        String prefix = stream.getPrefix(uri);
        log.log(Level.FINE, "getPrefix(): {0}", prefix);
        return prefix;
    }

    /**
     * Sets the prefix the uri is bound to. This prefix is bound in the scope 
     * of the current START_ELEMENT / END_ELEMENT pair. If this method is called 
     * before a START_ELEMENT has been written the prefix is bound in the root 
     * scope.
     * @param prefix the prefix to bind to the uri
     * @param uri the uri to bind to the prefix
     * @throws XMLStreamException Some error
     */
    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        log.log(Level.FINE, "getPrefix() {0} {1}", new Object[]{prefix, uri});
        stream.setPrefix(prefix, uri);
    }

    /**
     * Binds a URI to the default namespace This URI is bound in the scope of 
     * the current START_ELEMENT / END_ELEMENT pair. If this method is called 
     * before a START_ELEMENT has been written the uri is bound in the root 
     * scope.
     * @param uri the uri to bind to the default namespace
     * @throws XMLStreamException Some error
     */
    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        log.log(Level.FINE, "setDefaultNamespace() {0}", uri);
        stream.setDefaultNamespace(uri);
    }

    /**
     * Sets the current namespace context for prefix and uri bindings. This 
     * context becomes the root namespace context for writing and will replace
     * the current root namespace context. Subsequent calls to setPrefix and 
     * setDefaultNamespace will bind namespaces using the context passed to the 
     * method as the root context for resolving namespaces.
     * @param context the namespace context to use for this writer
     * @throws XMLStreamException 
     */
    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        log.log(Level.FINE, "setNamespaceContext()");
        stream.setNamespaceContext(context);
    }

    /**
     * Returns the current namespace context.
     * @return the current namespace context
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        log.log(Level.FINE, "getNamespaceContext()");
        return stream.getNamespaceContext();
    }
    
}
