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
package es.rickyepoderi.wbxml.document;

import es.rickyepoderi.wbxml.definition.IanaCharset;
import es.rickyepoderi.wbxml.definition.WbXmlAttributeDef;
import es.rickyepoderi.wbxml.definition.WbXmlAttributeValueDef;
import es.rickyepoderi.wbxml.definition.WbXmlDefinition;
import es.rickyepoderi.wbxml.definition.WbXmlExtensionDef;
import es.rickyepoderi.wbxml.definition.WbXmlInitialization;
import es.rickyepoderi.wbxml.definition.WbXmlTagDef;
import es.rickyepoderi.wbxml.definition.WbXmlToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * <p>The parser is the class that is used to parse an WBXML document into a
 * WbXmlDocument object in this package. This packages reads the document and
 * start creating the different elements in this package in order to finalize
 * with a complete document in Java/memory representation. All the different
 * parseXXX methods are called recursively to fulfill the complete Java
 * object.</p>
 * 
 * <p>The parser follows the WXML format specification explained in the 
 * <a href="http://www.openmobilealliance.org/tech/affiliates/LicenseAgreement.asp?DocName=/wap/wap-192-wbxml-20010725-a.pdf">open alliance document</a>.
 * This class has different methods to parse all the objects in this
 * package although it is intended to only call main method
 * parse().</p>
 * 
 * <p>All the parsing methods suppose that the stream is just at the start 
 * of the element to parse. That means that the currentByte is the first byte
 * of the element. In order to do that sometimes a byte should be read 
 * backwards.</p>
 * 
 * @author ricky
 */
public class WbXmlParser {
    
    /**
     * Input Stream where the WBXML document is going to be read.
     */
    private InputStream is;
    
    /**
     * The document the parser is creating during a parsing processing.
     */
    private WbXmlDocument doc;
    
    /**
     * the page code state for attributes. See chapter <em>5.8.1. Parser State Machine</em>
     * of the specification.
     */
    private byte pageAttrState;
    
    /**
     * The page code for tags. See same chapter <em>5.8.1. Parser State Machine</em>.
     */
    private byte pageTagState;
    
    /**
     * The current byte read from the stream.
     */
    private Byte currentByte;
    
    /**
     * Sometimes it is useful to read one byte backwards (to start when a
     * new element start and so on). This way a buffer of one byte is maintained
     * is used to can go back one byte.
     */
    private Byte nextByte;
    
    /**
     * Constructor of the parse. Only the input stream is needed cos the rest
     * is parsed from the document.
     * @param is The input stream to read the WBXML document from
     */
    public WbXmlParser(InputStream is) {
        this.is = is;
        this.pageAttrState = 0x00;
        this.pageTagState = 0x00;
        this.doc = null;
        currentByte = null;
        nextByte = null;
    }
    
    /**
     * Method that reads the next byte in the stream. Cos the parser lets one
     * read back the nextByte property is checked (in that case no byte
     * is really read and the nextByte is the current byte).
     * @return true if there is a new byte read, false if the document is finished
     * @throws IOException Some error reading from the stream
     */
    public boolean read() throws IOException {
        if (nextByte != null) {
            currentByte = nextByte;
            nextByte = null;
        } else {
            int i = this.is.read();
            if (i < 0) {
                currentByte = null;
            } else {
                currentByte = (byte) i;
            }
        }
        //System.err.println((currentByte != null)? WbXmlDefinition.formatUInt8Char(currentByte):"null");
        return currentByte != null;
    }
    
    /**
     * As explained in the class it is sometimes useful to read one byte 
     * backwards (usually to position the stream in the start of another 
     * element or attribute). This method does exactly that, the current byte
     * is set to null and the nextByte is filled with the current. The method 
     * is intended to immediately call the read() method.
     * @throws IOException A exception is thrown if the the current byte is null
     * (that means the readBackwards has been called twice).
     */
    public void readBackwards() throws IOException {
        if (currentByte != null) {
            nextByte = currentByte;
            currentByte = null;
        } else {
            throw new IOException("Only one readBackwards is permitted!");
        }
    }
    
    /**
     * Method that let us read a complete byte array buffer. This is used when
     * strings or other fixed arrays are presented (STR_I, OPAQUE,...). This
     * method is intended to read known fixed sized arrays (WBXML presents
     * this king of data always knowing the array length. The current byte is 
     * set to the last byte read.
     * @param b The buffer to read
     * @return The same buffer that is passed as argument
     * @throws IOException Some error reading from the stream or if the
     * end of the file is reached before filling all the array
     */
    public byte[] read(byte[] b) throws IOException {
        if (b.length == 0) {
            return b;
        }
        int offset = 0;
        if (nextByte != null) {
            b[0] = nextByte;
            nextByte = null;
            offset = 1;
        }
        int i = this.is.read(b, offset, b.length - offset);
        if (i  + offset < b.length) {
            // end of file => IOEXception
            throw new IOException(String.format("End of file reading a byte[] of size %d", b.length));
        }
        currentByte = b[b.length - 1];
        return b;
    }
    
    /**
     * <p>WBXML specification presents mb_u_int32 integers in a very strange
     * way in order to safe space. The way they are encoded is explained in
     * the chapter <em>5.1. Multi-byte Integers</em> of the specification.</p>
     * 
     * <p>A multi-byte integer consists of a series of octets, where the most 
     * significant bit is the continuation flag and the remaining seven bits 
     * are a scalar value. The continuation flag indicates that an octet is not 
     * the end of the multi-byte sequence. A single integer value is encoded 
     * into a sequence of N octets. The first N-1 octets have the continuation 
     * flag set to a value of one (1). The final octet in the series has a 
     * continuation flag value of zero (0). The remaining seven bits in each 
     * octet are encoded in a big-endian order, e.g., most significant bit 
     * first. The octets are arranged in a big-endian order, e.g., the most 
     * significant seven bits are transmitted first. In the situation where the 
     * initial octet has less than seven bits of value, all unused bits must be 
     * set to zero (0). For example, the integer value 0xA0 would be encoded 
     * with the two-byte sequence 0x81 0x20. The integer value 0x60 would be 
     * encoded with the one-byte sequence 0x60.</p>
     * 
     * @return The read integer in long format
     * @throws IOException Some error reading the integer
     */
    public long readUnsignedInteger() throws IOException {
        long res = 0;
        read();
        int times = 1;
        res = (res << 7) | (((byte) currentByte) & 0x7F);
        while ((((byte) currentByte) & 0x80) != 0) {
            if (times > 5) {
                throw new IOException("An unsigned integer should not be longer than 5 bytes!");
            }
            read();
            times++;
            res = (res << 7) | (((byte) currentByte) & 0x7F);
        }
        return res;
    }
    
    /**
     * A inline string (STR_I) is a string that is appended to the document
     * (as part of an attribute value or part of a content string). It is just 
     * defined as follows:
     * 
     * <pre>
     * inline = STR_I termstr
     * termstr = charset-dependent string with termination
     * </pre>
     * 
     * <p>The inline string is just a STR_I token follow by the string
     * (charset dependent) terminated in 0x00 token. The strings format are 
     * specified in the chapter <em>5.8.4.1. Strings</em> of the specification.
     * So this method read a byte until 0x00 token is found.</p>
     * 
     * @return The string inlined
     * @throws IOException 
     */
    public String readInlineString() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            read();
            while (currentByte != 0x0) {
                bos.write(currentByte);
                read();
            }
            return new String(bos.toByteArray(), doc.getCharset().getCharset());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch(IOException e) {}
            }
        }
    }
    
    /**
     * The WBXML format specification defines two states for a parser/encoder
     * machine. The states deals with the page code, one state is used for tags
     * and the other for attributes. When a tag or attribute comes if it
     * is from the same page code that the state is no switch page is written.
     * Nevertheless if the tag is from other page a switch page token should 
     * be written in order to change the state (there are two states and they
     * are independent, that is why the parser have to page codes, for tags
     * and for attributes). This states are explained in the chapter
     * <em>5.8.1. Parser State Machine</em> of the specification.
     * 
     * <p>This method is called when a new switch page can come (in any
     * tag, attribute or attribute value element), if it is found the 
     * switch page token the correspondent state is updated and the next
     * token is read in current byte.</p>
     * 
     * <p>This method reads possible switch page for the attribute state
     * parser.</p>
     * 
     * @throws IOException Some error reading the stream
     */
    public void readSwitchPageAttribute() throws IOException {
        if (WbXmlLiterals.SWTICH_PAGE == currentByte) {
            read();
            this.pageAttrState = currentByte;
            read();
        }
    }
    
    /**
     * The WBXML format specification defines two states for a parser/encoder
     * machine. The states deals with the page code, one state is used for tags
     * and the other for attributes. When a tag or attribute comes if it
     * is from the same page code that the state is no switch page is written.
     * Nevertheless if the tag is from other page a switch page token should 
     * be written in order to change the state (there are two states and they
     * are independent, that is why the parser have to page codes, for tags
     * and for attributes). This states are explained in the chapter
     * <em>5.8.1. Parser State Machine</em> of the specification.
     * 
     * <p>This method is called when a new switch page can come (in any
     * tag, attribute or attribute value element), if it is found the 
     * switch page token the correspondent state is updated and the next
     * token is read in current byte.</p>
     * 
     * <p>This method reads possible switch page for the TAG state
     * parser.</p>
     * 
     * @throws IOException Some error reading the stream
     */
    public void readSwitchPageTag() throws IOException {
        if (WbXmlLiterals.SWTICH_PAGE == currentByte) {
            read();
            this.pageTagState = currentByte;
            read();
        }
    }
    
    /**
     * Method that parses the version of a WBXML document. The version
     * is defined in the specification:
     * 
     * <pre>
     * version = u_int8 // WBXML version number
     * </pre>
     * 
     * The version encoding/parsing chapter is the <em>5.4. Version Number</em>:
     * All WBXML documents contain a version number in their initial byte. This 
     * version specifies the WBXML specification version. The version byte 
     * contains the major version minus one in the upper four bits and the minor
     * version in the lower four bits. For example, the version number 1.3 would
     * be encoded as 0x03, and version number 2.7 as 0x17.
     * 
     * @return The enumeration version that corresponds to the byte read
     * @throws IOException Some error reading the version or a unknown version
     */
    public WbXmlVersion parseVersion() throws IOException {
        read();
        byte major = (byte) ((currentByte >> 4) + 1);
        byte minor = (byte) (currentByte & 0x0F);
        WbXmlVersion v = WbXmlVersion.locateVersion(major, minor);
        if (v == null) {
            throw new IOException(String.format("Invalid version (%d,%d)", major, minor));
        }
        return v;
    }
    
    /**
     * Method that parses the WBXML public ID of a document. The public id
     * is defined in the specification as follows:
     * 
     * <pre>
     * publicid = mb_u_int32 | ( zero index )
     * zero = u_int8        // with a 0x0 value
     * index = mb_u_int32   // integer index into string table.
     * </pre>
     * 
     * The chapter <em>5.5. Document Public Identifier</em> defines how the
     * public id is parser/encoded. The public id can be parsed using 
     * directly the standard ID of the language (mb_u_int32) or using
     * the XML formal public id. In the last case a 0x00 byte is used and
     * the the String Table is used to locate the string. The public ID is
     * used to locate the language definition of the parsed document and, 
     * in case normal mb_u_int32 id used, it is set in the document.
     * 
     * @return The publicId, the index in the table in case string 
     * representation or -1 if unknown. In case of string representation StrTbl 
     * has not been read yet, so no language definition is still associated 
     * to the parser.
     * @throws IOException Error reading the stream or unknown language definition
     */
    public long parsePublicId() throws IOException {
        // read the mb_u_int32 or zero
        long publicId = readUnsignedInteger();
        if (publicId == WbXmlDefinition.PUBLIC_ID_STR_T) {
            // read the index in the strtbl, that index is returned
            publicId = readUnsignedInteger();
        } else if (publicId != WbXmlDefinition.PUBLIC_ID_UNKNOWN) {
            doc.setDefinition(WbXmlInitialization.getDefinitionByPublicId(publicId));
            if (doc.getDefinition() == null) {
                throw new IOException(String.format("Unknown definition public id (%d)", publicId));
            }
        } else {
            publicId = -1;
        }
        return publicId;
    }
    
    /**
     * Method that parses the charset of the WBXML document. In the specification
     * the charset is defined as follows:
     * 
     * <pre>
     * charset = mb_u_int32
     * </pre>
     * 
     * The chapter <em>5.6. Charset</em> of the specification explains how
     * the charset should be handled. it is just the MIB numeric identifier
     * of the IANA charset.The charset is set in the document.
     * 
     * @return The IANA charset that corresponds to the MIB found
     * @throws IOException Some error reading the stream or unknown IANA charset
     */
    public IanaCharset parseCharset() throws IOException {
        long mib = readUnsignedInteger();
        IanaCharset iana = IanaCharset.getIanaCharset(mib);
        if (mib != 0 && iana.equals(IanaCharset.UNKNOWN)) {
            throw new IOException(String.format("Unknown character encoding '%d'", mib));
        }
        doc.setCharset(iana);
        return iana;
    }
    
    /**
     * Method that parses the string table of the WBXML documnet. The string
     * table is defined as follows:
     * 
     * <pre>
     * strtbl = length *byte
     * </pre>
     * 
     * And the chapter <em>5.7. String Table</em> explains how the string
     * table is used and encoded. The table is just the length of itself and
     * a byte array with all the strings defined in the strtbl. The strings
     * are just charset dependent byte arrays 0x00 terminated. Later references
     * in the document to the strings in the table are done using the
     * relative starting idex of the string in the table. Besides being 
     * returned the strtbl is set in the parsed document.
     * 
     * @return The strtbl read
     * @throws IOException Some error in the stream or reading the table
     */
    public WbXmlStrtbl parseStrtbl() throws IOException {
        WbXmlStrtbl strtbl = new WbXmlStrtbl();
        long length = readUnsignedInteger();
        strtbl.setSize(length);
        //System.err.println("length=" + length);
        byte[] b = new byte[(int) length];
        read(b);
        int idx = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == 0x0) {
                String s = new String(b, idx, i - idx, doc.getCharset().getCharset());
                //System.err.println("idx=" + idx + " end=" + i + " s=" + s);
                strtbl.internalAddString(idx, s);
                idx = i + 1;
            }
        }
        doc.setStrtbl(strtbl);
        return strtbl;
    }
    
    /**
     * Method that parses an TAG opaque token. The WBXML specification let
     * languages to encode any TAG using a opaque byte array. Languages use
     * this feature to encode/parse particular data (datetime formats, bynary
     * data,...), so it is clearly language definition dependent. This 
     * library let define plugins to encode/parse an opaque data.
     * 
     * <p> The opaque is defined in the WBXML specification as follows:</p>
     * 
     * <pre>
     * opaque = OPAQUE length *byte
     * </pre>
     * 
     * <p>Just a OPAQUE token, length of the byte array and the bytes that
     * compound the opaque data. This method search if the definition defines
     * a plugin for this tag and calls it to retrieve the content 
     * associated to the element.</p>
     * 
     * NOTE: Right now a exception is throws if no plugin is found, libwbxml
     * just parses it as a string charset dependent!!!
     * 
     * @param tagName The name of the tag to loacte the plugin
     * @return The content after calling the associated plugin
     * @throws IOException Some error reading the stream or locating/executing the plugin
     */
    public WbXmlContent parseOpaqueTag(String tagName) throws IOException {
        // first get the plugin for the attr
        OpaqueContentPlugin plugin = doc.getDefinition().locateTagPlugin(tagName);
        if (plugin == null) {
            // read as a string opaque
            throw new IOException(String.format("No plugin defined for tag (%s)", tagName));
        }
        // read the OPAQUE tag
        read();
        if (WbXmlLiterals.OPAQUE != currentByte) {
            throw new IOException("Opaque must start with OPAQUE tag!");
        }
        // read the length
        long length = readUnsignedInteger();
        // create a byte array of that length
        byte[] b = new byte[(int) length];
        // read the byte array
        read(b);
        // parse the opaque data using the plugin
        return plugin.parse(this, b);
    }
    
    /**
     * Method that parses an attribute opaque token. The WBXML specification let
     * languages to encode any attribute value using a opaque byte array. Languages 
     * use this feature to encode/parse particular data (datetime formats, binary
     * data,...), so it is clearly language definition dependent. This 
     * library let define plugins to encode/parse an opaque data.
     * 
     * <p> The opaque is defined in the WBXML specification as follows:</p>
     * 
     * <pre>
     * opaque = OPAQUE length *byte
     * </pre>
     * 
     * <p>Just a OPAQUE token, length of the byte array and the bytes that
     * compound the opaque data. This method search if the definition defines
     * a plugin for this tag and calls it to retrieve the content 
     * associated to the element. In case of an attribute only string 
     * contents can be returned (as it is said in several points maybe two
     * interfaces would have been a better idea)</p>
     * 
     * NOTE: Right now a exception is throws if no plugin is found, libwbxml
     * just parses it as a string charset dependent!!!
     * 
     * @param attrName The name of the attribute to locate the plugin
     * @return The String of the attr
     * @throws IOException Some error reading the stream or locating/executing the plugin
     */
    public String parseOpaqueAttr(String attrName) throws IOException {
        // first get the plugin for the attr
        OpaqueAttributePlugin plugin = doc.getDefinition().locateAttrPlugin(attrName);
        if (plugin == null) {
            // read as a string opaque
            throw new IOException(String.format("No plugin defined for attr (%s)", attrName));
        }
        // read the OPAQUE tag
        read();
        if (WbXmlLiterals.OPAQUE != currentByte) {
            throw new IOException("Opaque must start with OPAQUE tag!");
        }
        // read the length
        long length = readUnsignedInteger();
        // create a byte array of that length
        byte[] b = new byte[(int) length];
        // read the byte array
        read(b);
        // parse the opaque data using the plugin
        return plugin.parse(this, b);
    }
    
    /**
     * Method that parses an numeric entity in the WBXML document. The 
     * entity is defined in the specification as follows:
     * 
     * <pre>
     * entity = ENTITY entcode
     * entcode = mb_u_int32 // UCS-4 character code
     * </pre>
     * 
     * The chapter <em>5.8.4.3. Character Entity</em> comments how the 
     * entity should be understood: The character entity token (ENTITY) encodes 
     * a numeric character entity. This has the same semantics as an XML
     * numeric character entity (e.g., &#32;). The mb_u_int32 refers to a 
     * character in the UCS-4 character encoding.
     * 
     * @return The string that represents the entity (i.e. "&#32;") 
     * @throws IOException Some error reading the stream
     */
    public String parseEntity() throws IOException {
        // read the ENTITY
        read();
        if (WbXmlLiterals.ENTITY != currentByte) {
            throw new IOException("Entity must start with the ENTITY tag!");
        }
        // read the numeric entity and construct the "&#" + num + ";"
        long entity = readUnsignedInteger();
        return new StringBuilder("&#").append(entity).append(";").toString();
    }
    
    /**
     * Method to parse the attribute values of attribute. In the specification
     * the attribute value is defined as follows:
     * 
     * <pre>
     * attrValue = ([switchPage] ATTRVALUE) | string | extension | entity | opaque
     *
     * string = inline | tableref 
     * inline = STR_I termstr 
     * tableref = STR_T index
     * index= mb_u_int32 // index in the attr table
     *
     * extension = [switchPage] (( EXT_I termstr ) | ( EXT_T index ) | EXT)
     *
     * entity = ENTITY entcode 
     * entcode = mb_u_int32 // UCS-4 character code
     *
     * opaque = OPAQUE length *byte
     * </pre>
     * 
     * The method keeps reading attributes and completing the values one
     * by one. It tries to differentiate the type (string, entity, opaque and
     * s on) and read each value accordingly. The values are returned as a
     * list of strings (one per different element in the WBXML document).
     * 
     * @param attrName The name of the attribute being parsed (used for opaques)
     * @return The list of values read from the stream
     * @throws IOException Some error reading the values from the stream
     */
    public List<String> parseAttributeValues(String attrName) throws IOException {
        //System.err.println("parseAttributeValues");
        List<String> values = new ArrayList<String>();
        read();
        boolean cont = true;
        while (cont) {
            // attr value can be switchPage => jump it if it is the case
            readSwitchPageAttribute();
            // ATTRVALUE, STR_I, STR_T, EXT_I*, EXT_T*, EXT, ENTITY or OPAQUE
            if (WbXmlLiterals.STR_I == currentByte) {
                values.add(readInlineString());
            } else if (WbXmlLiterals.STR_T == currentByte) {
                long idx = readUnsignedInteger();
                values.add(doc.getStrtbl().getString(idx));
            } else if (WbXmlLiterals.EXT_I_0 == currentByte
                    || WbXmlLiterals.EXT_I_1 == currentByte
                    || WbXmlLiterals.EXT_I_2 == currentByte) {
                // read the string from termstr
                values.add(readInlineString());
            } else if (WbXmlLiterals.EXT_T_0 == currentByte
                    || WbXmlLiterals.EXT_T_1 == currentByte
                    || WbXmlLiterals.EXT_T_2 == currentByte) {
                // read the index
                long extToken = readUnsignedInteger();
                WbXmlExtensionDef ext = doc.getDefinition().locateExtension(extToken);
                if (ext == null) {
                    throw new IOException(String.format("Unknown extension (%d)", extToken));
                }
                values.add(ext.getValue());
            } else if (WbXmlLiterals.EXT_0 == currentByte
                    || WbXmlLiterals.EXT_1 == currentByte
                    || WbXmlLiterals.EXT_2 == currentByte) {
                throw new IOException("Implementation does not support EXT_0, EXT_1 or EXT_2 in attribute values");
            } else if (WbXmlLiterals.ENTITY == currentByte) {
                // read backwards to start with ENTITY tag
                readBackwards();
                values.add(parseEntity());
            } else if (WbXmlLiterals.OPAQUE == currentByte) {
                // read backwars to start at OPAQUE and parse it
                readBackwards();
                String v = parseOpaqueAttr(attrName);
                if (v != null && !v.isEmpty()) {
                    values.add(v);
                }
            } else if ((currentByte & 0x80) != 0) {
                // it is an ATTRVALUE (any attrvalue is >= 128)
                WbXmlAttributeValueDef attrValDef = doc.getDefinition().locateAttributeValue(pageAttrState, currentByte);
                if (attrValDef == null) {
                    throw new IOException(String.format("Unknown ATTRVALUE in the definition (%s)",
                            new WbXmlToken(pageAttrState, currentByte)));
                }
                values.add(attrValDef.getValue());
            } else {
                // start of the next attribute or END
                cont = false;
            }
            if (cont) {
                // read next attribute value or END
                read();
            }
        }
        return values;
    }
    
    /**
     * Method that read a complete attribute from the WBXML stream. An
     * attribute is defined in the specifications as follows:
     * 
     * <pre>
     * attribute = attrStart *attrValue 
     * 
     * attrStart = ([switchPage] ATTRSTART) | (LITERAL index )
     * 
     * attrValue = ...
     * </pre>
     * 
     * So this method reads the attrStart and then call the parseAttributeValues
     * method to read all the values for the attribute.
     * 
     * @return The attribute read from the document
     * @throws IOException Some error reading the attribute from the stream
     */
    public WbXmlAttribute parseAttribute() throws IOException {
        //System.err.println("parseAttribute");
        WbXmlAttribute attr = new WbXmlAttribute();
        WbXmlAttributeDef attrDef;
        // read the first byte switchpage or attrstart
        read();
        // can be a switchPage => jump it
        readSwitchPageAttribute();
        // now b is the ATTRSTART or LITERAL
        if (WbXmlLiterals.LITERAL == currentByte) {
            // the attribute is a literal
            long idx = readUnsignedInteger();
            attr.setName(doc.getStrtbl().getString(idx));
        } else {
            // ATTRSTART, read the attribute definition
            attrDef = doc.getDefinition().locateAttribute(pageAttrState, currentByte);
            if (attrDef == null) {
                throw new IOException(String.format("Unknown ATTRSTART in the definition (%s)", 
                        new WbXmlToken(pageAttrState, currentByte)));
            }
            attr.setName(attrDef.getNameWithPrefix());
            //System.err.println("parseAttribute: name=" + attrDef.getName());
            if (attrDef.getValue() != null) {
                // add the first part of the string
                attr.addValue(attrDef.getValue());
            }
        }
        // now the attribute value
        attr.addValues(parseAttributeValues(attr.getName()));
        //System.err.println("parseAttribute: " + attr);
        // transform all the strings into one
        attr.normalize();
        return attr;
    }
    
    /**
     * Method that parses the different types of contents in an WBXML document.
     * The content is defined in the specificatiosn as follows:
     * 
     * <pre>
     * content = element | string | extension | entity | pi | opaque
     * 
     * string = inline | tableref
     * inline = STR_I termstr
     * tableref = STR_T index
     * 
     * extension = [switchPage] (( EXT_I termstr ) | ( EXT_T index ) | EXT)
     * 
     * entity = ENTITY entcode
     * 
     * opaque = OPAQUE length *byte
     * 
     * pi = PI attrStart *attrValue END
     * </pre>
     * 
     * So the method tries to differentiate the type (string, pi, opaque,...)
     * and create the content object with the value.
     * 
     * @param  tagName The tag of the element the content belongs to (used for opaques)
     * @return The content read from the stream
     * @throws IOException Some error reading the content from the stream
     */
    public WbXmlContent parseContent(String tagName) throws IOException {
        //System.err.println("parseContent");
        WbXmlContent content = new WbXmlContent();
        read();
        //System.err.println(WbXmlLiterals.formatUInt8Char(currentByte));
        // can be a switchPage => jump it
        readSwitchPageTag();
        // the first byte can be:
        //  -> string: STR_I, STR_T
        //  -> extension: EXT_I*, EXT_T*, EXT*
        //  -> entity: ENTITY
        //  -> opaque: OPAQUE
        //  -> pi: PI
        //  -> ELEMENT: TAG, LITERAL_*
        // ATTRVALUE, STR_I, STR_T, EXT_I*, EXT_T*, EXT, ENTITY or OPAQUE
        if (WbXmlLiterals.STR_I == currentByte) {
            content.setString(readInlineString());
        } else if (WbXmlLiterals.STR_T == currentByte) {
            long idx = readUnsignedInteger();
            content.setString(doc.getStrtbl().getString(idx));
        } else if (WbXmlLiterals.EXT_I_0 == currentByte
                || WbXmlLiterals.EXT_I_1 == currentByte
                || WbXmlLiterals.EXT_I_2 == currentByte) {
            // read the string from termstr
            content.setString(readInlineString());
        } else if (WbXmlLiterals.EXT_T_0 == currentByte
                || WbXmlLiterals.EXT_T_1 == currentByte
                || WbXmlLiterals.EXT_T_2 == currentByte) {
            // read the index
            long extToken = readUnsignedInteger();
            WbXmlExtensionDef ext = doc.getDefinition().locateExtension(extToken);
            if (ext == null) {
                throw new IOException(String.format("Unknown extension (%x)", extToken & 0xFF));
            }
            content.setString(ext.getValue());
        } else if (WbXmlLiterals.EXT_0 == currentByte
                || WbXmlLiterals.EXT_1 == currentByte
                || WbXmlLiterals.EXT_2 == currentByte) {
            throw new IOException("Implementation does not support EXT_0, EXT_1 or EXT_2 in contents");
        } else if (WbXmlLiterals.ENTITY == currentByte) {
            // read backwards to start with ENTITY tag
            readBackwards();
            content.setString(parseEntity());
        } else if (WbXmlLiterals.OPAQUE == currentByte) {
            // read backwars to start at OPAQUE and parse it
            readBackwards();
            content = parseOpaqueTag(tagName);
        } else if (WbXmlLiterals.PI == currentByte) {
            // read backwards to start with PI and parse;
            readBackwards();
            content.setPi(parsePi());
        } else if (WbXmlLiterals.END != currentByte) {
            // element => read backwards and call recursive
            readBackwards();
            content.setElement(parseElement());
        }
        // read the next element => END or another content
        read();
        if (content.isEmpty()) {
            // assign empty string
            content = null;
        }
        //System.err.println("parseContent: " + content);
        return content;
    }
    
    /**
     * Method that reads a complete element from the WBXML stream. An
     * element is defined in the specification as follows:
     * 
     * <pre>
     * element = ([switchPage] stag) [ 1*attribute END ] [ *content END ] 
     * stag = TAG | (literalTag index) 
     * literalTag = LITERAL | LITERAL_A | LITERAL_C | LITERAL_AC
     * </pre>
     * 
     * So the method reads the stag and then calls recursively to read all
     * the attributes and contents to previous methods.
     * 
     * @return The element read from the stream
     * @throws IOException Some error reading the element from the stream
     */
    public WbXmlElement parseElement() throws IOException {
        //System.err.println("parseElement");
        WbXmlElement element = new WbXmlElement();
        boolean hasAttributes;
        boolean hasContent;
        read();
        //System.err.println(WbXmlLiterals.formatUInt8Char(currentByte));
        // read possible switch page
        readSwitchPageTag();
        // the tag can be a LITERAL or a tag definition
        if (WbXmlLiterals.LITERAL == currentByte
                || WbXmlLiterals.LITERAL_A == currentByte
                || WbXmlLiterals.LITERAL_C == currentByte
                || WbXmlLiterals.LITERAL_AC == currentByte) {
            // literal => the name is in the strtbl
            hasAttributes = (WbXmlLiterals.LITERAL_A == currentByte) || 
                    (WbXmlLiterals.LITERAL_AC == currentByte);
            hasContent = (WbXmlLiterals.LITERAL_C == currentByte) || 
                    (WbXmlLiterals.LITERAL_AC == currentByte);
            long idx = readUnsignedInteger();
            String name = doc.getStrtbl().getString(idx);
            element.setTag(name);
        } else {
            // look for the tag in the definition
            WbXmlTagDef tagDef = doc.getDefinition().locateTag(pageTagState, (byte) (currentByte & 0x3F));
            if (tagDef == null) {
                throw new IOException(String.format("Unknown TAG in the definition (%s)", 
                        new WbXmlToken(pageTagState, currentByte)));
            }
            element.setTag(tagDef.getNameWithPrefix());
            // calculate attributes and contents
            hasAttributes = ((currentByte & 0x80) != 0);
            hasContent = ((currentByte & 0x40) != 0);
        }
        //System.err.println(element.getTag());
        if (hasAttributes) {
            boolean cont = true;
            while (cont) {
                // read one or more attributes
                element.addAttribute(parseAttribute());
                if (WbXmlLiterals.END == currentByte) {
                    cont = false;
                } else {
                    // keep going but set the current at start
                    readBackwards();
                }
            }
        }
        if (hasContent) {
            boolean cont = true;
            while (cont) {
                // read one or more contents
                WbXmlContent content = parseContent(element.getTag());
                if (content != null) {
                    element.addContent(content);
                }
                if (currentByte == null || WbXmlLiterals.END == currentByte) {
                    cont = false;
                } else {
                    // keep going but set the current at start
                    readBackwards();
                }
            }
        }
        // normalize strings
        element.normalize();
        //System.err.println("parseElement: " + element);
        return element;
    }
    
    /**
     * Method that parses a PI element from the WBXML stream. A PI element
     * is very similar to an attribute, it is defined as follows:
     * 
     * <pre>
     * PI attrStart *attrValue END
     * </pre>
     * 
     * So it reads the start PI token and then call the parseAttribute method.
     * 
     * @return The attribute of the PI
     * @throws IOException Some error reading the PI attribute from the stream
     */
    public WbXmlAttribute parsePi() throws IOException {
        read();
        if (WbXmlLiterals.PI != currentByte) {
            throw new IOException("PI must start with PI tag!");
        }
        WbXmlAttribute attr = parseAttribute();
        read();
        if (WbXmlLiterals.END != currentByte) {
            throw new IOException("PI must end with END tag!");
        }
        return attr;
    }
    
    /**
     * Method that parses the body of a WBXML document. The body is defined as
     * follows in the specification:
     * 
     * <pre>
     * body = *pi element *pi
     * </pre>
     * 
     * So the three elements are called to be read.
     *
     * @return The body read from the stream
     * @throws IOException Some error reading the body from the stream
     */
    public WbXmlBody parseBody() throws IOException {
        WbXmlBody body = new WbXmlBody();
        read();
        // b can be a PI, switchPage or stag, the first indicates
        // a pi element follows, the other a element
        while (WbXmlLiterals.PI == currentByte) {
            readBackwards();
            body.addPrePi(parsePi());
            read();
        }
        // we are at the start of an element => read backwards and parse
        readBackwards();
        body.setElement(parseElement());
        // we can be at the end of the file or before a PI
        read();
        while (currentByte != null && WbXmlLiterals.PI == currentByte) {
            readBackwards();
            body.addPostPi(parsePi());
            read();
        }
        return body;
    }
    
    /**
     * Main method of the class. This method starts the complete parsing of
     * the WBXML document, it calls recursively the different previous methods
     * to construct a whole Java representation of the WBXML document. This 
     * method let the caller to set a fixed definition.
     * The document is defined by the specification as follows:
     * 
     * <pre>
     * start = version publicid charset strtbl body
     * </pre>
     * 
     * @param def The definition to be used (forced), it can be null
     * @return The document (java representation) of the stream
     * @throws IOException Some error reading the document from the stream
     */
    public WbXmlDocument parse(WbXmlDefinition def) throws IOException {
        doc = new WbXmlDocument();
        // read the version
        doc.setVersion(parseVersion());
        // read the public id for the definition
        long publicId = parsePublicId();
        if (def != null) {
            // force the definition to the one specified
            doc.setDefinition(def);
        }
        // parse the charset
        parseCharset();
        // read the strtbl
        parseStrtbl();
        // get the definition
        if (doc.getDefinition() == null && publicId != -1) {
            // the public id is a index in the strtbl => read it
            String fpi = doc.getStrtbl().getString(publicId);
            doc.setDefinition(WbXmlInitialization.getDefinitionByFPI(fpi));
            if (doc.getDefinition() == null) {
                throw new IOException(String.format("Unknown definition formal public id (%s)", fpi));
            }
        } else if (doc.getDefinition() == null) {
            throw new IOException("Unknown definition and no one specified");
        }
        // read the body
        doc.setBody(parseBody());
        return doc;
    }
    
    /**
     * Main method of the class. This method starts the complete parsing of
     * the WBXML document, it calls recursively the different previous methods
     * to construct a whole Java representation of the WBXML document. The
     * document is defined by the specification as follows:
     * 
     * <pre>
     * start = version publicid charset strtbl body
     * </pre>
     * 
     * @return The document (java representation) of the stream
     * @throws IOException Some error reading the document from the stream
     */
    public WbXmlDocument parse() throws IOException {
        return parse(null);
    }
    
    /**
     * Getter for the document after being parsed.
     * @return The document
     */
    public WbXmlDocument getDocument() {
        return this.doc;
    }
    
    /**
     * Getter for the charset after being parsed
     * @return The charset of the document
     */
    public Charset getCharset() {
        return this.doc.getCharset().getCharset();
    }
    
    /**
     * Getter for the document language definition. As it is said the WBXML
     * specification defines one language for document but some of them
     * (SyncML for instance) let encode another language as opaque data. 
     * For that reason the parser provides a getdefinitionsUsed() method
     * to obtain all the languages used in the parsing and not only the
     * one of the document.
     * @return The language definition used in the parsing
     */
    public WbXmlDefinition getDefinition() {
        return this.doc.getDefinition();
    }
    
}
