package es.rickyepoderi.wbxml.test;


import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import es.rickyepoderi.wbxml.stream.WbXmlStreamReader;
import es.rickyepoderi.wbxml.stream.WbXmlStreamWriter;
import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import es.rickyepoderi.wbxml.stream.WbXmlEventReader;
import es.rickyepoderi.wbxml.stream.WbXmlEventWriter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ricky
 */
public class TestTools {
 
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("es.rickyepoderi.wbxml");
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.FINE);
        
        JAXBContext jc;
        Unmarshaller unmarshaller;
        FileOutputStream writer;
        FileInputStream reader;
        XMLStreamReader xmlStreamReader;
        XMLStreamWriter xmlStreamWriter;
        Marshaller marshaller;
        /*
        jc = JAXBContext.newInstance(OExRights.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setSchema(null);
        OExRights rights = (OExRights) unmarshaller.unmarshal(
                new File("/home/ricky/NetBeansProjects/wbxml-jaxb/examples/drmrel/drmrel-003.xml"));
        writer = new FileOutputStream("/home/ricky/drmrel.wbxml");
        xmlStreamWriter = new WbXmlStreamWriter(writer, WbXmlInitialization.getDefinitionByName("drmrel 1.0"));
        marshaller = jc.createMarshaller();
        marshaller.marshal(rights, xmlStreamWriter);
        xmlStreamWriter.close();
        
        jc = JAXBContext.newInstance(SyncML.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setSchema(null);
        SyncML syncml = (SyncML) unmarshaller.unmarshal(
                new File("/home/ricky/NetBeansProjects/wbxml-jaxb/examples/syncml/syncml-001.xml"));
        writer = new FileOutputStream("/home/ricky/syncml.wbxml");
        xmlStreamWriter = new WbXmlStreamWriter(writer);
        marshaller = jc.createMarshaller();
        marshaller.marshal(syncml, xmlStreamWriter);
        xmlStreamWriter.close();
        
        jc = JAXBContext.newInstance(Si.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setSchema(null);
        Si si = (Si) unmarshaller.unmarshal(
                new File("/home/ricky/NetBeansProjects/wbxml-jaxb/examples/si/si-001.xml"));
        System.err.println("action: " + si.getIndication().getAction());
        //ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer = new FileOutputStream("/home/ricky/si.wbxml");
        xmlStreamWriter = new WbXmlStreamWriter(writer);
        marshaller = jc.createMarshaller();
        marshaller.marshal(si, xmlStreamWriter);
        xmlStreamWriter.close();
        //bos.close();
        //for (int i = 0; i < bos.toByteArray().length; i++) {
        //    System.err.println(WbXmlDefinition.formatUInt8Char(bos.toByteArray()[i]));
        //}
        
        jc = JAXBContext.newInstance(WVCSPMessage.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setSchema(null);
        WVCSPMessage wvcsp = (WVCSPMessage) unmarshaller.unmarshal(
                new File("/home/ricky/NetBeansProjects/wbxml-jaxb/examples/wv/wv-013.xml"));
        writer = new FileOutputStream("/home/ricky/wvcsp.wbxml");
        xmlStreamWriter = new WbXmlStreamWriter(writer, WbXmlInitialization.getDefinitionByName("WV CSP 1.1"));
        marshaller = jc.createMarshaller();
        marshaller.marshal(wvcsp, xmlStreamWriter);
        xmlStreamWriter.close();
        
        jc = JAXBContext.newInstance(OExRights.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        reader = new FileInputStream("/home/ricky/drmrel.wbxml");
        xmlStreamReader = new WbXmlStreamReader(reader);
        OExRights rights = (OExRights) unmarshaller.unmarshal(xmlStreamReader);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(rights, System.out);
        xmlStreamReader.close();
        
        jc = JAXBContext.newInstance(SyncML.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        reader = new FileInputStream("/home/ricky/syncml.wbxml");
        xmlStreamReader = new WbXmlStreamReader(reader);
        SyncML syncml = (SyncML) unmarshaller.unmarshal(xmlStreamReader);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(syncml, System.out);
        xmlStreamReader.close();
        
        jc = JAXBContext.newInstance(Si.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        reader = new FileInputStream("/home/ricky/si.wbxml");
        xmlStreamReader = new WbXmlStreamReader(reader);
        Si si = (Si) unmarshaller.unmarshal(xmlStreamReader);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(si, System.out);
        xmlStreamReader.close();
        
        jc = JAXBContext.newInstance(WVCSPMessage.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        reader = new FileInputStream("/home/ricky/wvcsp.wbxml");
        xmlStreamReader = new WbXmlStreamReader(reader);
        WVCSPMessage wvcsp = (WVCSPMessage) unmarshaller.unmarshal(xmlStreamReader);
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(wvcsp, System.out);
        xmlStreamReader.close();
        
        jc = JAXBContext.newInstance(es.rickyepoderi.wbxml.bind.activesync.airsync.ObjectFactoryResponse.class, 
                es.rickyepoderi.wbxml.bind.activesync.airsyncbase.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.contacts.ObjectFactory.class, 
                es.rickyepoderi.wbxml.bind.activesync.rightsmanagement.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.calendar.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.tasks.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.contacts2.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.notes.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.email.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.email2.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.documentlibrary.ObjectFactory.class);
        System.err.println(jc.getClass());
        unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setSchema(null);
        SyncResponse sync = (SyncResponse) unmarshaller.unmarshal(new File("/home/ricky/lala.xml"));
        System.err.println(sync);
        writer = new FileOutputStream("/home/ricky/activesync.wbxml");
        xmlStreamWriter = new WbXmlStreamWriter(writer);
        marshaller = jc.createMarshaller();
        marshaller.marshal(sync, xmlStreamWriter);
        xmlStreamWriter.close();
        
        jc = JAXBContext.newInstance(es.rickyepoderi.wbxml.bind.activesync.airsync.ObjectFactoryResponse.class, 
                es.rickyepoderi.wbxml.bind.activesync.airsyncbase.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.contacts.ObjectFactory.class, 
                es.rickyepoderi.wbxml.bind.activesync.rightsmanagement.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.calendar.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.tasks.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.contacts2.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.notes.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.email.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.email2.ObjectFactory.class,
                es.rickyepoderi.wbxml.bind.activesync.documentlibrary.ObjectFactory.class);
        unmarshaller = jc.createUnmarshaller();
        reader = new FileInputStream("/home/ricky/lala.wbxml");
        xmlStreamReader = new WbXmlStreamReader(reader, WbXmlInitialization.getDefinitionByName("ActiveSync"));
        SyncResponse syncRes = (SyncResponse) unmarshaller.unmarshal(xmlStreamReader);
        reader.close();
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(syncRes, System.err);*/
        /*
        // read the XML file using DOM
        InputStream in = new FileInputStream("/home/ricky/syncml.xml");
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        domFact.setNamespaceAware(true);
        domFact.setIgnoringElementContentWhitespace(true);
        domFact.setCoalescing(true);
        domFact.setIgnoringComments(true);
        DocumentBuilder domBuilder = domFact.newDocumentBuilder();
        Document doc = domBuilder.parse(in);
        doc.normalizeDocument();
        // write a WBXML into a ByteArray
        WbXmlDefinition definition = WbXmlInitialization.getDefinitionByRoot(
            doc.getDocumentElement().getLocalName(), 
            doc.getDocumentElement().getNamespaceURI());
        FileOutputStream out = new FileOutputStream("/home/ricky/lala.wbxml");
        xmlStreamWriter = new WbXmlStreamWriter(out, definition, WbXmlEncoder.StrtblType.ALWAYS, true);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        Source domSource = new DOMSource(doc);
        StAXResult staxResult = new StAXResult(xmlStreamWriter);
        xformer.transform(domSource, staxResult);
        out.close();
        */
        
        
        // read a WBXML file using DOM
        InputStream in = new FileInputStream("/home/ricky/lala.wbxml");
        XMLEventReader xmlEventReader = new WbXmlEventReader(in, null);
        StAXSource staxSource = new StAXSource(xmlEventReader);
        StreamResult result = new StreamResult(System.out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(staxSource, result); 
        
        /*
        InputStream in = new FileInputStream("/home/ricky/lala.xml");
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        domFact.setNamespaceAware(true);
        domFact.setIgnoringElementContentWhitespace(true);
        domFact.setCoalescing(true);
        domFact.setIgnoringComments(true);
        domFact.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder domBuilder = domFact.newDocumentBuilder();
        Document doc = domBuilder.parse(in);
        doc.normalizeDocument();
        // write a WBXML into a ByteArray
        WbXmlDefinition definition = WbXmlInitialization.getDefinitionByName(
                "ActiveSync");
        FileOutputStream out = new FileOutputStream("/home/ricky/lala.wbxml");
        WbXmlEventWriter xmlEventWriter = new WbXmlEventWriter(out, definition, WbXmlEncoder.StrtblType.IF_NEEDED, true);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        Source domSource = new DOMSource(doc);
        StAXResult staxResult = new StAXResult(xmlEventWriter);
        xformer.transform(domSource, staxResult);
        out.close();*/
    }
 
}
