/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.rickyepoderi.wbxml.test;

import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import es.rickyepoderi.wbxml.document.WbXmlEncoder;
import org.testng.annotations.Test;

/**
 *
 * @author ricky
 */
public class SyncMLTest extends GenericDirectoryTester {
    
    public SyncMLTest() {
        super("src/test/examples/syncml",
                WbXmlInitialization.getDefinitionByName("SyncML 1.1"));
    }
    
    @Test(groups = {"xml", "syncml", "type-if-needed", "stream" })
    public void testXmlStreamIfNeeded() throws Exception {
        testXmlDirectory(WbXmlEncoder.StrtblType.IF_NEEDED, true, false);
    }
    
    @Test(groups = {"xml", "syncml", "type-always", "stream" })
    public void testXmlStreamAllways() throws Exception {
        testXmlDirectory(WbXmlEncoder.StrtblType.ALWAYS, true, false);
    }
    
    @Test(groups = {"xml", "syncml", "type-no", "stream" })
    public void testXmlStreamNo() throws Exception {
        testXmlDirectory(WbXmlEncoder.StrtblType.NO, true, false);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-if-needed", "stream" })
    public void testWbXMLStreamIfNeeded() throws Exception {
        testWbXmlDirectory(WbXmlEncoder.StrtblType.IF_NEEDED, true, false);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-if-always", "stream" })
    public void testWbXMLStreamAllways() throws Exception {
        testWbXmlDirectory(WbXmlEncoder.StrtblType.ALWAYS, true, false);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-no", "stream" })
    public void testWbXMLStreamNo() throws Exception {
        testWbXmlDirectory(WbXmlEncoder.StrtblType.NO, true, false);
    }
    
    @Test(groups = {"xml", "syncml", "type-no", "jaxb", "stream" })
    public void testXMLStreamJaxb() throws Exception {
        testXmlJaxbDirectory(WbXmlEncoder.StrtblType.NO, true, false);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-no", "jaxb", "stream" })
    public void testWBXMLStreamJaxb() throws Exception {
        testWbXmlJaxbDirectory(WbXmlEncoder.StrtblType.NO, true, false);
    }
    
    
    
    @Test(groups = {"xml", "syncml", "type-if-needed", "event" })
    public void testXmlIfEventNeeded() throws Exception {
        testXmlDirectory(WbXmlEncoder.StrtblType.IF_NEEDED, true, true);
    }
    
    @Test(groups = {"xml", "syncml", "type-always", "event" })
    public void testXmlEventAllways() throws Exception {
        testXmlDirectory(WbXmlEncoder.StrtblType.ALWAYS, true, true);
    }
    
    @Test(groups = {"xml", "syncml", "type-no", "event" })
    public void testXmlEventNo() throws Exception {
        testXmlDirectory(WbXmlEncoder.StrtblType.NO, true, true);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-if-needed", "event" })
    public void testWbXMLEventIfNeeded() throws Exception {
        testWbXmlDirectory(WbXmlEncoder.StrtblType.IF_NEEDED, true, true);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-if-always", "event" })
    public void testWbXMLEventAllways() throws Exception {
        testWbXmlDirectory(WbXmlEncoder.StrtblType.ALWAYS, true, true);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-no", "event" })
    public void testWbXMLEventNo() throws Exception {
        testWbXmlDirectory(WbXmlEncoder.StrtblType.NO, true, true);
    }
    
    @Test(groups = {"xml", "syncml", "type-no", "jaxb", "event" })
    public void testXMLEventJaxb() throws Exception {
        testXmlJaxbDirectory(WbXmlEncoder.StrtblType.NO, true, true);
    }
    
    @Test(groups = {"wbxml", "syncml", "type-no", "jaxb", "event" })
    public void testWBXMLEventJaxb() throws Exception {
        testWbXmlJaxbDirectory(WbXmlEncoder.StrtblType.NO, true, false);
    }
}
