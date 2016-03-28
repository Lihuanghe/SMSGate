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
package es.rickyepoderi.wbxml.tools;

import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import es.rickyepoderi.wbxml.document.WbXmlParser;
import es.rickyepoderi.wbxml.stream.WbXmlEventReader;
import es.rickyepoderi.wbxml.stream.WbXmlInputFactory;
import es.rickyepoderi.wbxml.stream.WbXmlStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

/**
 * <p>Tool or command class that emulates the libwbxml <em>wbxml2xml</em> 
 * counterpart. This command let the user convert a WBML file into a common
 * XML file.</p>
 * 
 * <h3>NAME</h3>
 * 
 * <p>wbXml2Xml - Convert a WBXML binary file into a common XML text file</p>
 * 
 * <h3>SYNOPSYS</h3>
 * 
 * <p>java -cp wbxml-jaxb-X.X.X.jar es.rickyepoderi.wbxml.tools.Xml2WbXml 
 * [-j --jaxb] [-d --definition $lt;NAME&gt;] {infile} {outfile}</p>
 * 
 * <h3>DESCRIPTION</h3>
 * 
 * <p>Command that converts a binary WBXML file into an XML file. It uses
 * an intermediary form to represent the structure and then normal DOM or
 * JAXB techniques are used. In case of JAXB the representation classes 
 * should be provided (the <em>wbxml-stream</em> package does not contain
 * any JAXB classes, some classes were created for a few languages only for
 * testing purposes but they are not packaged in the library).</p>
 * 
 * <p>The following arguments are used:</p>
 * 
 * <ul>
 * <li><p><strong>-j --jaxb</strong>: Use JAXB processing (object representation)
 * instead of normal DOM processing. As it was said JAXB classes are not part 
 * of the <em>wbxml-stream</em> library.</p></li>
 * <li><p><strong>-e --event</strong>: Use the event reader instead of common
 * stream reader implementation.</p></li>
 * <li><p><strong>-d --definition</strong>: Use a fixed language definition 
 * for the WBXML file. If no one is provided the command tries to guess it
 * using the public identifier of the WBXML file. But if it is unknown (some
 * languages are nor standardized, like Microsoft ActiveSync) and no definition 
 * is provided an error is reported.</p>
 * <p>A list of possible language definitions is shown in the usage of the 
 * command.</p></li>
 * <li><p><strong>infile</strong>: WBXML file to convert, <em>-</em> can be
 * provided to use the standard output.</em>
 * <li><p><strong>outfile</strong>: file to write the resulting XML, 
 * <em>-</em> can be provided to use the standard input.</em>
 * </ul>
 * 
 * <h3>EXAMPLES</h3>
 * 
 * <pre>
 * java -cp wbxml-stream-0.1.0.jar es.rickyepoderi.wbxml.tools.WbXml2Xml si.wbxml -
 *     Convert the file si.wbxml into XML and the result is displayed in the
 *     standard output.
 * 
 * java -cp wbxml-stream-0.1.0.jar es.rickyepoderi.wbxml.tools.WbXml2Xml si.wbxml si.xml
 *     Convert the file si.wbxml into XML and the result is placed in si.xml
 * 
 * java -cp wbxml-stream-0.1.0.jar es.rickyepoderi.wbxml.tools.WbXml2Xml -d "SI 1.0" si.wbxml -
 *     Force definition to SI 1.0 
 * 
 * </pre>
 * 
 * @author ricky
 */
final public class WbXml2Xml {
    
    /**
     * The input stream to read the WBXML.
     */
    private InputStream in = null;
    
    /**
     * The output stream to write the XML.
     */
    private OutputStream out = null;
    
    /**
     * Use DOM (default) or the stranger JAXB.
     */
    private boolean useDom = true;
    
    /**
     * The definition to use in the conversion.
     */
    private WbXmlDefinition def = null;
    
    /**
     * Use XMLEventReader.
     */
    private boolean event = false;
    
    /**
     * It prints the usage of the command and the throws a IllegalArgumentException.
     * @param message The message to show previous of the usage part
     */
    private void usage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
        sb.append("java -cp wbxml-jaxb-X.X.X.jar ");
        sb.append(this.getClass().getName());
        sb.append(" [-j --jaxb] [-d --definition <NAME>] {infile} {outfile}");
        sb.append(System.getProperty("line.separator"));
        sb.append("       -j --jaxb: Use JAXB instead instead default DOM");
        sb.append(System.getProperty("line.separator"));
        sb.append("      -e --event: Use XMLEventWriter instead of the default XMLStreamWriter");
        sb.append(System.getProperty("line.separator"));
        sb.append("                  In order to use JAXB the classes should be generated from the DTD (xjc)");
        sb.append(System.getProperty("line.separator"));
        sb.append(" -d --definition: Force definition instead deriving from WBXML. Current definitions:");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlDefinition d: WbXmlInitialization.getDefinitions()) {
            sb.append(String.format("                  %s", d.getName()));
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("          infile: input WBXML file (\"-\" means standard input)");
        sb.append(System.getProperty("line.separator"));
        sb.append("         outfile: output XML file (\"-\" means sntandard output)");
        sb.append(System.getProperty("line.separator"));
        throw new IllegalArgumentException(sb.toString());
    }
    
    /**
     * Method that gets the value part of a parameter if it exists. If the
     * array has no more elements usage is called.
     * @param args The list of arguments
     * @param i The current position
     * @return The string of the value
     */
    private String getNext(String[] args, int i) {
        if (args.length > i) {
            return args[i];
        } else {
            usage("Invalid invocation.");
        }
        return null;
    }
    
    /**
     * Constructor that creates the command using the arguments passed by the 
     * caller. Usage is used (IllegalArgumentException) if some error
     * is detected in the arguments.
     * @param args The argument list
     * @throws Exception Any error
     */
    private WbXml2Xml(String[] args) throws Exception {
        String infile = null;
        String outfile = null;
        int i = 0;
        if (args.length < 2) {
            usage("Invalid invocation.");
        }
        while (i < args.length) {
            if ("-d".equals(args[i]) || "--definition".equals(args[i])) {
                // use fixed definition
                String defName = getNext(args, ++i);
                this.def = WbXmlInitialization.getDefinitionByName(defName);
                if (this.def == null) {
                    usage(String.format("Invalid definition specified '%s'.", defName));
                }
            } else if ("-e".equals(args[i]) || "--event".equals(args[i])) {
                // Use the XMLEventWriter
                this.event = true;
            } else if ("-j".equals(args[i]) || "--jaxb".equals(args[i])) {
                // use JAXB instead DOM
                this.useDom = false;
            } else {
                if (args.length - i != 2) {
                    usage("Invalid invocation.");
                }
                // the names of the infile and outfile
                infile = getNext(args, i);
                outfile = getNext(args, ++i);
            }
            i++;
        }
        // get the input stream
        if ("-".equals(infile)) {
            in = System.in;
        } else {
            File f = new File(infile);
            if (!f.exists() || !f.canRead()) {
                usage(String.format("Input WBXML file '%s' is not readable.", infile));
            }
            in = new FileInputStream(f);
        }
        // get the output stream
        if ("-".equals(outfile)) {
            out = System.out;
        } else {
            File f = new File(outfile);
            f.createNewFile();
            if (!f.canWrite()) {
                usage(String.format("Output XML file '%s' is not writable.", outfile));
            }
            out = new FileOutputStream(f);
        }
    }
    
    /**
     * Method that executes the command, ie, does the conversion from WBXML
     * to XML.
     * @throws Exception Some error
     */
    private void process() throws Exception {
        XMLStreamReader xmlStreamReader = null;
        XMLEventReader xmlEventReader = null;
        XMLInputFactory fact = new WbXmlInputFactory();
        fact.setProperty(WbXmlInputFactory.DEFINITION_PROPERTY, def);
        try {
            WbXmlParser parser;
            if (event) {
                xmlEventReader = fact.createXMLEventReader(in);
                parser = ((WbXmlEventReader) xmlEventReader).getParser();
            } else {
                xmlStreamReader = fact.createXMLStreamReader(in);
                parser = ((WbXmlStreamReader) xmlStreamReader).getParser();
            }
            if (!useDom) {
                String clazz = parser.getDefinition().getClazz();
                if (clazz == null || clazz.isEmpty()) {
                    usage(String.format("The definition '%s' does not contain a main class.",
                            parser.getDefinition().getName()));
                }
                JAXBContext jc = JAXBContext.newInstance(Class.forName(clazz));
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                Object obj;
                if (event) {
                    obj = unmarshaller.unmarshal(xmlEventReader);
                } else {
                    obj = unmarshaller.unmarshal(xmlStreamReader);
                }
                Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                if (parser.getDefinition().getXmlPublicId() != null
                        && parser.getDefinition().getXmlUriRef() != null) {
                    // add the doctype based in the definition
                    marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
                            String.format("\n<!DOCTYPE %s PUBLIC \"%s\" \"%s\">",
                            jc.createJAXBIntrospector().getElementName(obj), 
                            parser.getDefinition().getXmlPublicId(), 
                            parser.getDefinition().getXmlUriRef()));
                }
                marshaller.marshal(obj, out);
            } else {
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                if (parser.getDefinition().getXmlPublicId() != null
                        && parser.getDefinition().getXmlUriRef() != null) {
                    // add the doctype based in the definition
                    xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, parser.getDefinition().getXmlPublicId());
                    xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, parser.getDefinition().getXmlUriRef());
                }
                xformer.setOutputProperty(OutputKeys.INDENT, "yes");
                xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                StAXSource staxSource;
                if (event) {
                    staxSource = new StAXSource(xmlEventReader);
                } else {
                    staxSource = new StAXSource(xmlStreamReader);
                }
                DOMResult domResult = new DOMResult();
                xformer.transform(staxSource, domResult);
                Source domSource = new DOMSource(domResult.getNode(), domResult.getSystemId());
                Result result = new StreamResult(out);
                xformer.transform(domSource, result);
            }
        } finally {
            if (xmlStreamReader != null) {
                try {xmlStreamReader.close();} catch (Exception e) {}
            }
            if (xmlEventReader != null) {
                try {xmlEventReader.close();} catch (Exception e) {}
            }
        }
    }
    
    /**
     * It closes all the streams.
     */
    private void close() {
        try {in.close();} catch(Exception e) {}
        try {out.close();} catch(Exception e) {}
    }
    
    /**
     * Execution of the command. Usage is used if the arguments are incorrect.
     * @param args The arguments to call the command
     * @throws Exception Some error in the invocation
     */
    static public void main(String args[]) throws Exception {
        WbXml2Xml command = null;
        try {
            command = new WbXml2Xml(args);
            command.process();
        } finally {
            if (command != null) {
                command.close();
            }
        }
    }
}
