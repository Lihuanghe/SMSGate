/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.rickyepoderi.wbxml.test;

import es.rickyepoderi.wbxml.bind.si.Indication;
import es.rickyepoderi.wbxml.bind.si.Si;
import es.rickyepoderi.wbxml.stream.WbXmlInputFactory;
import es.rickyepoderi.wbxml.stream.WbXmlOutputFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author ricky
 */
public class SiStAXTest {
    
    
    /**
     * si-001.xml:
     * 
     * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
     * <!DOCTYPE si PUBLIC "-//WAPFORUM//DTD SI 1.0//EN" "http://www.wapforum.org/DTD/si.dtd">
     * <si>
     *   <indication created="1999-06-25T15:23:15Z" 
     *               href="http://www.xyz.com/email/123/abc.wml" 
     *               si-expires="1999-06-30T00:00:00Z">
     *     You have 4 new emails
     *   </indication>
     *</si>
     * 
     */
    
    File wbxmlFile = null;
    
    public SiStAXTest() {
        wbxmlFile = new File("src/test/examples/si/si-001.wbxml");
    }
    
    @Test(groups = {"stax"} )
    public void testReadWBXML1() throws Exception {
        XMLInputFactory f = new WbXmlInputFactory();
        XMLStreamReader reader = f.createXMLStreamReader(new FileInputStream(wbxmlFile));
        boolean foundSi = false;
        boolean foundIndication = false;
        while (reader.hasNext()) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if ("si".equals(reader.getLocalName())) {
                    foundSi = true;
                } else if ("indication".equals(reader.getLocalName())) {
                    foundIndication = true;
                    Assert.assertEquals(reader.getAttributeCount(), 3);
                    Assert.assertEquals(reader.getAttributeValue(null, "created"), "1999-06-25T15:23:15Z");
                    Assert.assertEquals(reader.getAttributeValue(null, "href"), "http://www.xyz.com/email/123/abc.wml");
                    Assert.assertEquals(reader.getAttributeValue(null, "si-expires"), "1999-06-30T00:00:00Z");
                    Assert.assertEquals(reader.getElementText(), "You have 4 new emails");
                }
            }
            reader.next();
        }
        Assert.assertTrue(foundSi);
        Assert.assertTrue(foundIndication);
        reader.close();
    }
    
    private void test2(InputStream in) throws Exception {
        XMLInputFactory f = new WbXmlInputFactory();
        XMLStreamReader reader = f.createXMLStreamReader(in);
        // read next tag
        int type = reader.nextTag();
        // it should be "si"
        Assert.assertEquals(type, XMLStreamConstants.START_ELEMENT);
        Assert.assertEquals(reader.getName().getLocalPart(), "si");
        Assert.assertEquals(reader.getAttributeCount(), 0);
        // read the next tag
        type = reader.nextTag();
        // it should be "indication"
        Assert.assertEquals(type, XMLStreamConstants.START_ELEMENT);
        Assert.assertEquals(reader.getName().getLocalPart(), "indication");
        Assert.assertEquals(reader.getAttributeCount(), 3);
        Assert.assertEquals(reader.getAttributeValue(null, "created"), "1999-06-25T15:23:15Z");
        Assert.assertEquals(reader.getAttributeValue(null, "href"), "http://www.xyz.com/email/123/abc.wml");
        Assert.assertEquals(reader.getAttributeValue(null, "si-expires"), "1999-06-30T00:00:00Z");
        // read the element text and position to END "indication"
        Assert.assertEquals(reader.getElementText(), "You have 4 new emails");
        // read next tag
        type = reader.nextTag();
        // "si" end
        Assert.assertEquals(type, XMLStreamConstants.END_ELEMENT);
        reader.close();
    }
    
    @Test(groups = {"stax"} )
    public void testReadWBXML2() throws Exception  {
        test2(new FileInputStream(wbxmlFile));
    }
    
    @Test(groups = {"stax"})
    public void testWriteWBML() throws Exception {
        Indication indication = new Indication();
        indication.setCreated("1999-06-25T15:23:15Z");
        indication.setHref("http://www.xyz.com/email/123/abc.wml");
        indication.setSiExpires("1999-06-30T00:00:00Z");
        indication.setvalue("You have 4 new emails");
        Si si = new Si();
        si.setIndication(indication);
        // write the "si" object
        XMLOutputFactory fact = new WbXmlOutputFactory();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = fact.createXMLStreamWriter(bos);
        xmlStreamWriter.writeStartDocument();
        xmlStreamWriter.writeStartElement("si");
        xmlStreamWriter.writeStartElement("indication");
        xmlStreamWriter.writeAttribute("created", si.getIndication().getCreated());
        xmlStreamWriter.writeAttribute("href", si.getIndication().getHref());
        xmlStreamWriter.writeAttribute("si-expires", si.getIndication().getSiExpires());
        xmlStreamWriter.writeCharacters(si.getIndication().getvalue());
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndDocument();
        xmlStreamWriter.close();
        // check the write
        test2(new ByteArrayInputStream(bos.toByteArray()));
    }
    
}