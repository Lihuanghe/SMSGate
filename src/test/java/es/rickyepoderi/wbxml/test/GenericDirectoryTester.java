/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.rickyepoderi.wbxml.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.w3c.dom.Document;

import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import es.rickyepoderi.wbxml.stream.WbXmlInputFactory;
import es.rickyepoderi.wbxml.stream.WbXmlOutputFactory;

/**
 *
 * @author ricky
 */
public class GenericDirectoryTester {
    
    private String directory = null;
    private WbXmlDefinition def = null;
    private JAXBContext jc = null;
    private XMLInputFactory inFact = null;
    private XMLOutputFactory outFact = null;
    
    public GenericDirectoryTester(String directory, WbXmlDefinition def) {
        this.directory = directory;
        this.def = def;
        this.inFact = new WbXmlInputFactory();
        inFact.setProperty(WbXmlInputFactory.DEFINITION_PROPERTY, def);
        this.outFact = new WbXmlOutputFactory();
        outFact.setProperty(WbXmlOutputFactory.DEFINITION_PROPERTY, def);
    }
    
    protected Object xmlFile2Object(File f) throws Exception {
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            if (jc == null) {
                jc = JAXBContext.newInstance(Class.forName(this.def.getClazz()));
            }
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(in);
            Object o = unmarshaller.unmarshal(xmlStreamReader);
            return o;
        } finally {
            if (in != null) {
                try {in.close();} catch (Exception  e) {}
            }
        }
    }
    
    protected Document xmlFile2Doc(File f) throws Exception {
        InputStream in = null; 
        try {
            in = new FileInputStream(f);
            DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
          
            domFact.setNamespaceAware(true);
            domFact.setIgnoringElementContentWhitespace(true);
            domFact.setIgnoringComments(true);
            domFact.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
          
            DocumentBuilder domBuilder = domFact.newDocumentBuilder();
            Document doc = domBuilder.parse(in);
           // doc.normalize();
           doc.normalizeDocument();
            return doc;
        } finally {
            if (in != null) {
                try {in.close();} catch (Exception  e) {}
            }
        }
    }
    
    protected Document wbxmlStream2Doc(InputStream in, boolean event) throws Exception {
       XMLStreamReader xmlStreamReader = null;
       XMLEventReader xmlEventReader = null;
        try {
            if (event) {
                xmlEventReader = inFact.createXMLEventReader(in);
            } else {
                xmlStreamReader = inFact.createXMLStreamReader(in);
            }
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            StAXSource staxSource = event? new StAXSource(xmlEventReader) :
                    new StAXSource(xmlStreamReader);
            DOMResult domResult = new DOMResult();
            xformer.transform(staxSource, domResult);
            Document doc = (Document) domResult.getNode();
            doc.normalize();
            return doc;
        } finally {
            if (xmlStreamReader != null) {
                try {xmlStreamReader.close();} catch (Exception e) {}
            }
            if (xmlEventReader != null) {
                try {xmlEventReader.close();} catch (Exception e) {}
            }
        } 
    }
    
    protected Object wbxmlStream2Object(InputStream in, boolean event) throws Exception {
        XMLStreamReader xmlStreamReader = null;
        XMLEventReader xmlEventReader = null;
        try {
            if (event) {
                xmlEventReader = inFact.createXMLEventReader(in);
            } else {
                xmlStreamReader = inFact.createXMLStreamReader(in);
            }
            if (jc == null) {
                jc = JAXBContext.newInstance(Class.forName(this.def.getClazz()));
            }
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            if (event) {
                return unmarshaller.unmarshal(xmlEventReader);
            } else {
                return unmarshaller.unmarshal(xmlStreamReader);
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
    
    protected Document wbxmlFile2Doc(File f, boolean event) throws Exception {
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            return wbxmlStream2Doc(in, event);
        } finally {
            if (in != null) {
                try {in.close();} catch (Exception  e) {}
            }
        }
    }
    
    protected Object wbxmlFile2Object(File f, boolean event) throws Exception {
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            return wbxmlStream2Object(in, event);
        } finally {
            if (in != null) {
                try {in.close();} catch (Exception  e) {}
            }
        }
    }
    
    protected Document wbxml2doc(byte[] bytes, WbXmlDefinition def,
            boolean event) throws Exception {
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(bytes);
            return wbxmlStream2Doc(in, event);
        } finally {
            if (in != null) {
                try {in.close();} catch (Exception  e) {}
            }
        }
    }
    
    protected Object wbxml2Object(byte[] bytes, WbXmlDefinition def,
            boolean event) throws Exception {
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(bytes);
            return wbxmlStream2Object(in, event);
        } finally {
            if (in != null) {
                try {in.close();} catch (Exception  e) {}
            }
        }
    }
    
    protected byte[] doc2WbXml(Document doc, WbXmlDefinition def,
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces, boolean event) throws Exception {
        XMLStreamWriter xmlStreamWriter = null;
        XMLEventWriter xmlEventWriter = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            outFact.setProperty(WbXmlOutputFactory.ENCODING_TYPE_PROPERTY, encoderType);
            outFact.setProperty(WbXmlOutputFactory.SKIP_SPACES_PROPERTY, skipSpaces);
            if (event) {
                xmlEventWriter = outFact.createXMLEventWriter(out);
            } else {
                xmlStreamWriter = outFact.createXMLStreamWriter(out);
            }
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            Source domSource = new DOMSource(doc);
            StAXResult staxResult = (event)? new StAXResult(xmlEventWriter) : 
                    new StAXResult(xmlStreamWriter);
            xformer.transform(domSource, staxResult);
            return out.toByteArray();
        } finally {
            if (out != null) {
                try {out.close();} catch(Exception e) {}
            }
            if (xmlStreamWriter != null) {
                try {xmlStreamWriter.close();} catch(Exception e) {}
            }
            if (xmlEventWriter != null) {
                try {xmlEventWriter.close();} catch(Exception e) {}
            }
        }
    }
    
    protected Document object2Doc(Object o) throws Exception {
        if (jc == null) {
            jc = JAXBContext.newInstance(o.getClass());
        }
        Marshaller marshaller = jc.createMarshaller();
        DOMResult res = new DOMResult();
        marshaller.marshal(o, res);
        return (Document) res.getNode();
    }
    
    protected Object doc2Object(Document doc) throws Exception {
        if (jc == null) {
            jc = JAXBContext.newInstance(Class.forName(this.def.getClazz()));
        }
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Object o = unmarshaller.unmarshal(doc);
        return o;
    }
    
    protected byte[] object2WbXml(Object o, WbXmlDefinition def,
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces, boolean event) throws Exception {
        XMLStreamWriter xmlStreamWriter = null;
        XMLEventWriter xmlEventWriter = null;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            outFact.setProperty(WbXmlOutputFactory.ENCODING_TYPE_PROPERTY, encoderType);
            outFact.setProperty(WbXmlOutputFactory.SKIP_SPACES_PROPERTY, skipSpaces);
            if (event) {
                xmlEventWriter = outFact.createXMLEventWriter(out);
            } else {
                xmlStreamWriter = outFact.createXMLStreamWriter(out);
            }
            JAXBContext jc = JAXBContext.newInstance(o.getClass());
            Marshaller marshaller = jc.createMarshaller();
            if (event) {
                marshaller.marshal(o, xmlEventWriter);
            } else {
                marshaller.marshal(o, xmlStreamWriter);
            }
            return out.toByteArray();
        } finally {
            if (out != null) {
                try {out.close();} catch(Exception e) {}
            }
            if (xmlStreamWriter != null) {
                try {xmlStreamWriter.close();} catch(Exception e) {}
            }
            if (xmlEventWriter != null) {
                try {xmlEventWriter.close();} catch(Exception e) {}
            }
        }
    }
    
    protected boolean checkDocs(Document doc1, Document doc2) {
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreWhitespace(true);
        DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(doc1, doc2));
        boolean result = diff.similar();
        if (!result) {
            for (Difference d: (List<Difference>) diff.getAllDifferences()) {
                System.out.println(d);
            }
        }
        return result;
    }
            
    protected boolean testXmlFile(File f, WbXmlDefinition def,
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces,
            boolean event) {
        try {
            Document doc1 = xmlFile2Doc(f);
            byte[] bytes = doc2WbXml(doc1, def, encoderType, skipSpaces, event);
            Document doc2 = wbxml2doc(bytes, def, event);
            boolean result = checkDocs(doc1, doc2);
            System.out.println(String.format("Testing file '%s' = %s", 
                    f.getName(), result? "OK" : "ERROR"));
            return result;
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println(String.format("Testing file '%s' = ERROR (%s)", 
                    f.getName(), e.getMessage()));
            return false;
        }
    }
    
    protected boolean testXmlJaxbFile(File f, WbXmlDefinition def,
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces,
            boolean event) {
        try {
            Object o1 = xmlFile2Object(f);
            byte[] bytes = object2WbXml(o1, def, encoderType, skipSpaces, event);
            Document doc1 = wbxml2doc(bytes, def, event);
            Object o2 = xmlFile2Object(f);
            Document doc2 = object2Doc(o2);
            boolean result = checkDocs(doc1, doc2);
            System.out.println(String.format("Testing file '%s' = %s", 
                    f.getName(), result? "OK" : "ERROR"));
            return result;
        } catch (Exception e) {
            System.out.println(String.format("Testing file '%s' = ERROR (%s)", 
                    f.getName(), e.getMessage()));
            return false;
        }
    }
    
    protected boolean testWbXmlJaxbFile(File f, WbXmlDefinition def,
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces,
            boolean event) throws Exception {
        try {
            Object o1 = wbxmlFile2Object(f, event);
            byte[] bytes = object2WbXml(o1, def, encoderType, skipSpaces, event);
            Document doc1 = wbxml2doc(bytes, def, event);
            Object o2 = wbxmlFile2Object(f, event);
            Document doc2 = object2Doc(o2);
            boolean result = checkDocs(doc1, doc2);
            System.out.println(String.format("Testing file '%s' = %s", 
                    f.getName(), result? "OK" : "ERROR"));
            return result;
        } catch (Exception e) {
            System.out.println(String.format("Testing file '%s' = ERROR (%s)", 
                    f.getName(), e.getMessage()));
            return false;
        }
    }
    
    protected boolean testWbXmlFile(File f, WbXmlDefinition def,
            WbXmlEncoder.StrtblType encoderType, boolean skipSpaces,
            boolean event) throws Exception {
        try {
            Document doc1 = wbxmlFile2Doc(f, event);
            byte[] bytes = doc2WbXml(doc1, def, encoderType, skipSpaces, event);
            Document doc2 = wbxml2doc(bytes, def, event);
            boolean result = checkDocs(doc1, doc2);
            System.out.println(String.format("Testing file '%s' = %s", 
                    f.getName(), result? "OK" : "ERROR"));
            return result;
        } catch (Exception e) {
            System.out.println(String.format("Testing file '%s' = ERROR (%s)", 
                    f.getName(), e.getMessage()));
            return false;
        }
    }
    
    protected File[] getFiles(final String suffix) throws Exception {
        File dir = new File(directory);
        if (!dir.isDirectory()) {
            throw new Exception("The file is not a directory!");
        }
        return  dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(suffix);
            }
        });
    }
    
    protected void testXmlDirectory(WbXmlEncoder.StrtblType encoderType, 
            boolean skipSpaces, boolean event) throws Exception {
        System.out.println(String.format("Testing XML DOM definition '%s' type=%s skip=%b event=%b...", 
                def.getName(), encoderType.name(), skipSpaces, event));
        File[] files = getFiles(".xml");
        int errors = 0;
        for (File f: files) {
            boolean ok = testXmlFile(f, def, encoderType, skipSpaces, event);
            if (!ok) {
                errors++;
            }
        }
        System.out.println(String.format("Processed %d XML files with %d errors.", 
                files.length, errors));
        Assert.assertEquals(errors, 0);
    }
    
    protected void testXmlJaxbDirectory(WbXmlEncoder.StrtblType encoderType, 
            boolean skipSpaces, boolean event) throws Exception {
        System.out.println(String.format("Testing XML JAXB definition '%s' type=%s skip=%b event=%b...", 
                def.getName(), encoderType.name(), skipSpaces, event));
        File[] files = getFiles(".xml");
        int errors = 0;
        for (File f: files) {
            boolean ok = testXmlJaxbFile(f, def, encoderType, skipSpaces, event);
            if (!ok) {
                errors++;
            }
        }
        System.out.println(String.format("Processed %d XML files with %d errors.", 
                files.length, errors));
        Assert.assertEquals(errors, 0);
    }
    
    protected void testWbXmlDirectory(WbXmlEncoder.StrtblType encoderType, 
            boolean skipSpaces, boolean event) throws Exception {
        System.out.println(String.format("Testing WBXML DOM definition '%s' type=%s skip=%b event=%b...", 
                def.getName(), encoderType.name(), skipSpaces, event));
        File[] files = getFiles(".wbxml");
        int errors = 0;
        for (File f: files) {
            boolean ok = testWbXmlFile(f, def, encoderType, skipSpaces, event);
            if (!ok) {
                errors++;
            }
        }
        System.out.println(String.format("Processed %d WBXML files with %d errors.", 
                files.length, errors));
        Assert.assertEquals(errors, 0);
    }
    
    protected void testWbXmlJaxbDirectory(WbXmlEncoder.StrtblType encoderType, 
            boolean skipSpaces, boolean event) throws Exception {
        System.out.println(String.format("Testing WBXML JAXB definition '%s' type=%s skip=%b event=%b...", 
                def.getName(), encoderType.name(), skipSpaces, event));
        File[] files = getFiles(".wbxml");
        int errors = 0;
        for (File f: files) {
            boolean ok = testWbXmlJaxbFile(f, def, encoderType, skipSpaces, event);
            if (!ok) {
                errors++;
            }
        }
        System.out.println(String.format("Processed %d WBXML files with %d errors.", 
                files.length, errors));
        Assert.assertEquals(errors, 0);
    }
}
