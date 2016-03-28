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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * <p>The input factory to create stream and event writer for the WBXML
 * format. There are some methods that are simple not implemented and others
 * not supported. They supports three properties:</p>
 * 
 * <ul>
 * <li>es.rickyepoderi.wbxml.stream.definition: The definition to force. It
 * should be a WbXmlDefinition (by default is null, no forced definition).</li>
 * </ul>
 * 
 * @author ricky
 */
public class WbXmlInputFactory extends XMLInputFactory {

    /**
     * The property for the definition to use. WbXmlDefnition value (default null,
     * the parser will guess it from public id of the WBXML file.
     */
    static public String DEFINITION_PROPERTY = "es.rickyepoderi.wbxml.stream.definition";
    
    /**
     * Properties of the factory.
     */
    private Map<String,Object> props = null;
    
    /**
     * Empty constructor with the default values.
     */
    public WbXmlInputFactory() {
        props = new HashMap<String,Object>();
    }
    
    /**
     * Create a new XMLStreamReader from a reader. It is not supported cos
     * WBXML is a binary format. Please use the other methods.
     * @param reader the XML data to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException 
     */
    @Override
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        throw new XMLStreamException("The WBXML is a binary format!");
    }

    /**
     * Create a new XMLStreamReader from a JAXP source. This method is optional.
     * @param source the source to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
        try {
            if (source instanceof StreamSource) {
                StreamSource ss = (StreamSource) source;
                return new WbXmlStreamReader(ss.getInputStream(),
                        (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
            } else {
                throw new XMLStreamException("WBXML only support StreamSource with InputStream!");
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLStreamReader from a java.io.InputStream 
     * @param in the InputStream to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLStreamReader createXMLStreamReader(InputStream in) throws XMLStreamException {
        try {
            return new WbXmlStreamReader(in,
                    (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLStreamReader from a java.io.InputStream 
     * @param in the InputStream to read from
     * @param encoding the character encoding of the stream
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException 
     */
    @Override
    public XMLStreamReader createXMLStreamReader(InputStream in, String encoding) throws XMLStreamException {
        try {
            return new WbXmlStreamReader(in,
                    (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLStreamReader from a java.io.InputStream
     * @param systemId the system ID of the stream
     * @param in the InputStream to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream in) throws XMLStreamException {
        try {
            return new WbXmlStreamReader(in,
                    (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLStreamReader from a java.io.InputStream. Not supported
     * cos WBXML is binary. Use one of the other methods.
     * @param systemId the system ID of the stream
     * @param reader the reader to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
        throw new XMLStreamException("The WBXML is a binary format!");
    }

    /**
     * Create a new XMLEventReader from a reader. Not supported
     * cos WBXML is binary. Use one of the other methods.
     * @param reader the XML data to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
        throw new XMLStreamException("The WBXML is a binary format!");
    }

    /**
     * Create a new XMLEventReader from a reader. Not supported
     * cos WBXML is binary. Use one of the other methods.
     * @param systemId the system ID of the stream
     * @param reader the XML data to read from
     * @return The stream reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Create a new XMLEventReader from an XMLStreamReader. After being used to
     * construct the XMLEventReader instance returned from this method the
     * XMLStreamReader must not be used. It only admits WbXmlStreamReader.
     * @param reader the XMLStreamReader to read from (may not be modified) 
     * @return a new XMLEventReader
     * @throws XMLStreamException Some error
     *     
     */
    @Override
    public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
        return new WbXmlEventReader((WbXmlStreamReader) reader);
    }

    /**
     * Create a new XMLEventReader from a JAXP source. Support of this method is optional.
     * Only StreamSource with InputStream is supported.
     * @param source
     * @return The event reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
        try {
            if (source instanceof StreamSource) {
                StreamSource ss = (StreamSource) source;
                return new WbXmlEventReader(ss.getInputStream(),
                        (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
            } else {
                throw new XMLStreamException("WBXML only support StreamSource with InputStream!");
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLEventReader from a java.io.InputStream
     * @param in the InputStream to read from
     * @return The event reader implementation for WBXML
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLEventReader createXMLEventReader(InputStream in) throws XMLStreamException {
        try {
            return new WbXmlEventReader(in,
                    (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLEventReader from a java.io.InputStream 
     * @param in the InputStream to read from
     * @param encoding the character encoding of the stream
     * @return The event reader implementation for WBXML
     * @throws XMLStreamException 
     */
    @Override
    public XMLEventReader createXMLEventReader(InputStream in, String encoding) throws XMLStreamException {
        try {
            return new WbXmlEventReader(in,
                    (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a new XMLEventReader from a java.io.InputStream
     * @param systemId the system ID of the stream
     * @param in the InputStream to read from
     * @return The event reader implementation for WBXML
     * @throws XMLStreamException 
     */
    @Override
    public XMLEventReader createXMLEventReader(String systemId, InputStream in) throws XMLStreamException {
        try {
            return new WbXmlEventReader(in,
                    (WbXmlDefinition) props.get(DEFINITION_PROPERTY));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Create a filtered reader that wraps the filter around the reader.
     * Not supported.
     * @param reader the event reader to wrap
     * @param filter the filter to apply to the event reader
     * @return The stream reader wrapped with the filter
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported!");
    }

    /**
     * Create a filtered event reader that wraps the filter around the event reader.
     * Not supported.
     * @param reader the event reader to wrap
     * @param filter the filter to apply to the event reader
     * @return The event reader wrapped with the filter
     * @throws XMLStreamException Some error
     */
    @Override
    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported!");
    }

    /**
     * The resolver that will be set on any XMLStreamReader or XMLEventReader 
     * created by this factory instance. It always returns null.
     * @return The resolver
     */
    @Override
    public XMLResolver getXMLResolver() {
        return null;
    }

    /**
     * The resolver that will be set on any XMLStreamReader or XMLEventReader 
     * created by this factory instance. Nothing is set.
     * @param resolver The resolver to use
     */
    @Override
    public void setXMLResolver(XMLResolver resolver) {
        // nothing
    }

    /**
     * The reporter that will be set on any XMLStreamReader or XMLEventReader
     * created by this factory instance. It always returns null.
     * @return  The reporter
     */
    @Override
    public XMLReporter getXMLReporter() {
        return null;
    }

    /**
     * The reporter that will be set on any XMLStreamReader or XMLEventReader 
     * created by this factory instance. Nothing is set.
     * @param reporter The new reporter.
     */
    @Override
    public void setXMLReporter(XMLReporter reporter) {
        //  nothing
    }

    /**
     * Allows the user to set specific feature/property on the underlying 
     * implementation. The underlying implementation is not required to support 
     * every setting of every property in the specification and may use 
     * IllegalArgumentException to signal that an unsupported property may 
     * not be set with the specified value. 
     * @param name The name of the property (may not be null)
     * @param value The value of the property 
     * @throws IllegalArgumentException if the property is not supported
     */
    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException {
        if (DEFINITION_PROPERTY.equals(name)) {
           props.put(name, (WbXmlDefinition) value);
       } else {
           throw new IllegalArgumentException(String.format("Invalid property %s", name));
       }
    }

    /**
     * Get the value of a feature/property from the underlying implementation
     * @param name The name of the property (may not be null) 
     * @return The value of the property
     * @throws IllegalArgumentException if the property is not supported
     */
    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        if (!isPropertySupported(name)) {
            throw new IllegalArgumentException(String.format("Invalid property %s", name));
        }
        return props.get(name);
    }

    /**
     * Query the set of properties that this factory supports. 
     * @param name The name of the property (may not be null)
     * @return true if the property is supported and false otherwise
     */
    @Override
    public boolean isPropertySupported(String name) {
        return DEFINITION_PROPERTY.equals(name);
    }

    /**
     * Set a user defined event allocator for events. Nothing is set.
     * @param allocator The new allocator
     */
    @Override
    public void setEventAllocator(XMLEventAllocator allocator) {
        // nothing
    }

    /**
     * Gets the allocator used by streams created with this factory. It 
     * always returns null.
     * @return  The allocator
     */
    @Override
    public XMLEventAllocator getEventAllocator() {
        return null;
    }
    
}
