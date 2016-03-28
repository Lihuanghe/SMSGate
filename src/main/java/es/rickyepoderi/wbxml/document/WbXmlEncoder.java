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
import es.rickyepoderi.wbxml.definition.WbXmlTagDef;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The encoder class is the object to encode a Java WbXmlDocument object
 * into a real WBXML stream. The encoder is the symmetric class to the parser,
 * the methods are more or less the same but for encoding and writing instead of
 * parsing and reading.</p>
 * 
 * <p>The encoder follows the WXML format specification explained in the 
 * <a href="http://www.openmobilealliance.org/tech/affiliates/LicenseAgreement.asp?DocName=/wap/wap-192-wbxml-20010725-a.pdf">open alliance document</a>.
 * This class has different methods to encode all the objects in this
 * package although it is intended to only call main method encode().</p>
 * 
 * <p>The WBXML specification defines a string table (strtbl) that is 
 * written at the beginning of the document but, if used, it is filled
 * during the later processing of elements. So if the strtbl is used two
 * passes are needed (one to encode everything and determine the strtbl
 * contents, and the second to really write the stream). For that reason
 * the encoder has three types of processing:</p>
 * 
 * <ul>
 * <li>StrtblType=NO. This way not strtbl is used, the document is directly
 * written to the final stream and if (for some reasons) the strtbl is needed,
 * it throws an exception. So here only one pass is needed.</li>
 * <li>StrtblType=IF_NEEDED. Second and more conservative way of processing
 * the document, the strtbl is not used (no STR_T strings are used) but if for
 * some reason it is needed (missing tag or whatever) it is done. The idea
 * is the encoder uses another ByteArrayOutputStream in the first pass, and
 * if strtbl is not used the whole bytes are dumped into the real stream, 
 * is it was used a second real encoding process is performed.</li>
 * <li>StrtblType=ALWAYS. The strtbl is always used (instead of normal STR_I
 * the STR_T strings are used). The processing is exactly the same of the
 * previous type but here almost always the second pass will be needed.</li>
 * </ul>
 * 
 * @author ricky
 */
public class WbXmlEncoder {
    
    /**
     * Logger for the class.
     */
    protected static final Logger log = Logger.getLogger(WbXmlEncoder.class.getName());
    
    /**
     * Initial length of the auxiliary ByteArrayOuputStream.
     */
    static private final int BYTE_ARRAY_INITIAL_LENGTH = 1024;
    
    /**
     * Maximum value of a mb_u_int32.
     */
    static private final long MAX_UNSIGNED_INT = (((long)Integer.MAX_VALUE) * 2) + 1;
    
    /**
     * The StrtvlType is the type of encoding the encoder is going to perform
     * in relation with the String Table use.
     */
    public enum StrtblType { 
        
        /**
         * The Strtbl is never used (STR_I strings used). If, for some reason,
         * the strtbl is needed a exception is thrown. Only one pass is done.
         */
        NO, 
        
        /**
         * The Strtbl is only used if needed (STR_I strings still used). An
         * initial auxiliary ByteOutputStreamWriter is used in a first pass.
         */
        IF_NEEDED, 
        
        /**
         * The strtbl is used for all strings (STR_T). As in the previous 
         * type the auxiliary ByteOutputStreamWriter is used at first pass.
         */
        ALWAYS 
    };
    
    /**
     * The output stream used to perform the encoding process.
     */
    private OutputStream os;
    
    /**
     * Property to save the real in case of a first pass needed (IF_NEEDED and ALWAYS).
     */
    private OutputStream realOs;
    
    /**
     * The page attr state parser property.
     */
    private byte pageAttrState;
    
    /**
     * The page tag state parser property.
     */
    private byte pageTagState;
    
    /**
     * The document enconded to a WBXML stream.
     */
    private WbXmlDocument doc;
    
    /**
     * The type of the encoding process.
     */
    private StrtblType type;
    
    /**
     * Boolean property that marks if the strtbl is used. This is used to
     * check if a second pass is needed or the byte array can be directly dumpled.
     */
    private boolean strtblUsed;

    /**
     * Constructor of the encoder based in the output stream and the document
     * to encode. The default type is IF_NEEDED.
     * @param os The output stream to write the WBXML document
     * @param doc The document to encode
     */
    public WbXmlEncoder(OutputStream os, WbXmlDocument doc) {
        this(os, doc, StrtblType.IF_NEEDED);
    }
    
    /**
     * Constructor based in the three elements: output stream, document
     * and the type of used of the strtbl.
     * @param os The output stream to write the WBXML document
     * @param doc The document to encode
     * @param type The type of encoding to use (strtbl).
     */
    public WbXmlEncoder(OutputStream os, WbXmlDocument doc, StrtblType type) {
        this.pageAttrState = 0x00;
        this.pageTagState = 0x00;
        this.type = type;
        this.strtblUsed = false;
        this.realOs = os;
        this.os = os;
        this.doc = doc;
    }
    
    /**
     * Getter for the charset of the document.
     * @return The charset of the document
     */
    protected Charset getCharset() {
        return this.doc.getCharset().getCharset();
    }
    
    /**
     * Getter for the type of the encoder. 
     * @return The type or way of encoding.
     */
    public StrtblType getType() {
        return this.type;
    }
    
    /**
     * Getter for the charset of the document.
     * @return The IANA charset the document is using.
     */
    public IanaCharset getIanaCharset() {
        return this.doc.getCharset();
    }

    /**
     * Getter for the strtbl of the document.
     * @return The strtbl of the document
     */
    public WbXmlStrtbl getStrtbl() {
        return doc.getStrtbl();
    }

    /**
     * Getter for the definition of the document.
     * @return The definition of the document.
     */
    public WbXmlDefinition getDefinition() {
        return doc.getDefinition();
    }

    /**
     * Method that checks if the strtbl has been marched as used. It is called
     * when exported in the first pass.
     * @return true if the strtbl has been marked as used
     */
    protected boolean isStrtblUsed() {
        return strtblUsed;
    }

    /**
     * Setter for the strtbl as used. It is called form the WbXmlStrtbl object
     * when a string is added.
     */
    protected void setStrtblUsed() {
        this.strtblUsed = true;
    }
    
    /**
     * Method to reset the encoder to perform a second pass or a second
     * encoding process. A encoder is used only for one document, if you need
     * to encode a second document, create a second encoder.
     */
    public void reset() {
        this.pageAttrState = 0x0;
        this.pageTagState = 0x0;
        this.strtblUsed = false;
    }
    
    //
    // WRITE METHODS
    //
    
    /**
     * Method that writes a bite in the stream.
     * @param b The byte to write
     * @throws IOException Some error writing to the stream
     */
    public void write(byte b) throws IOException {
        write(new byte[] {b});
    }

    /**
     * Method that writes a byte array in the stream.
     * @param b The byte array to write
     * @throws IOException Some error writing the stream
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    
    /**
     * Method that write a byte array but starting from a specified index 
     * and with a specified length.
     * @param b The byte array to write
     * @param start The start index to write
     * @param length The length to write
     * @throws IOException Some error writing the stream
     */
    public void write(byte[] b, int start, int length) throws IOException {
        this.os.write(b, start, length);
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
     * @param value The value to write as a multi-byte integer
     * @throws IOException Some error writing the mb_u_int32
     */
    public void writeUnsignedInteger(long value) throws IOException {
        if (value > MAX_UNSIGNED_INT) {
            throw new IOException("Maximun unsigned integer value reached");
        }
        byte[] octets = new byte[5];
        octets[4] = (byte) (value & 0x7f);
        value >>= 7;
        //System.err.println(octets[4]);
        int i;
        for (i = 3; value > 0 && i >= 0; i--) {
            octets[i] = (byte) (0x80 | (value & 0x7f));
            //System.err.println(octets[i]);
            value >>= 7;
        }
        int start = i + 1;
        //System.err.println("start: " + start + " length: " + (5 - start));
        write(octets, start, 5 - start);
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
     * <p>This method is always a new attribute or attribute value is written, 
     * the method writes the switch page if the page changes. The state parser 
     * for attributes is updated with the new page.</p>
     * 
     * @param page The page of the attribute token
     * @throws IOException Some error writing to the stream
     */
    public void writeSwitchPageAttribute(byte page) throws IOException {
        if (pageAttrState != page) {
            pageAttrState = page;
            write(WbXmlLiterals.SWTICH_PAGE);
            write(pageAttrState);
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
     * <p>This method is always a new tag token is written, 
     * the method writes the switch page if the page changes. The state parser 
     * for tags is updated with the new page.</p>
     * 
     * @param page
     * @throws IOException 
     */
    public void writeSwitchPageTag(byte page) throws IOException {
        if (pageTagState != page) {
            pageTagState = page;
            write(WbXmlLiterals.SWTICH_PAGE);
            write(pageTagState);
        }
    }
    
    /**
     * Generic method to write strings to the stream. Depending the type of 
     * enconding STR_I (inline) or STR_T (reference) strings are used. If type
     * is NO or IF_NEEDED inline strings are used, if ALWAYS reference one are
     * written.
     * @param s The string to write to the stream
     * @throws IOException Some error writting the string
     */
    public void writeString(String s) throws IOException {
        if (StrtblType.ALWAYS.equals(type)) {
            writeReferenceString(s);
        } else {
            writeInlineString(s);
        }
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
     * So this method writes the STR_I token and the charset dependent 
     * string array and the final 0x00.</p>
     * 
     * @param s The string to write in the stream as STR_I
     * @throws IOException Some error writing to the stream
     */
    public void writeInlineString(String s) throws IOException {
        write(WbXmlLiterals.STR_I);
        writeTableString(s);
    }
    
    /**
     * A refrence string is a string which is in the string table and it is
     * referenced in some part of the document. The refernce string is defined
     * as follows by the specification:
     * 
     * <pre>
     * tableref = STR_T index
     * index = mb_u_int32 // integer index into string table.
     * </pre>
     * 
     * <p>A refrence string is just the STR_T token followed by the index
     * to the string table. That index is a multi-byte integer. This method
     * adds the string to the table (getting the index) and then writes
     * the token and the index obtained. The method addString() throws an
     * exception if the table cannot be used.</p>
     * 
     * @param s The string to write as reference (STR_T) string
     * @throws IOException Some error writing to the stream or strtbl cannot be used
     */
    public void writeReferenceString(String s) throws IOException {
        long idx = doc.getStrtbl().addString(this, s);
        write(WbXmlLiterals.STR_T);
        writeUnsignedInteger(idx);
    }
    
    /**
     * This method writes the string in the stream. The string is transformed in
     * a byte array using the document charset (IANA) and after that a 0x00 mark
     * is written. This method is used to write string in the strtbl and as
     * inline (STR_I) strings. The string is defined in the specification as follows:
     * 
     * <pre>
     * termstr = charset-dependent string with termination (0x00)
     * </pre>
     * 
     * @param s The string to write to the stream
     * @throws IOException Some error writing to the stream
     */
    public void writeTableString(String s) throws IOException {
        write(s.getBytes(getCharset()));
        write((byte) 0x00);
    }
    
    /**
     * Method that writes a opaque data byte array. An opaque data is just
     * defined by the specification as follows:
     * 
     * <pre>
     * opaque = OPAQUE length *byte
     * </pre>
     * 
     * <p>The opaque is just the OPAQUE token follow by the length of the array
     * and the array itself. This method should be called from opaque plugins
     * when the data is calculated.</p>
     * 
     * @param data The opaque data byte array to write
     * @throws IOException Some error writing to the stream
     */
    public void writeOpaque(byte[] data) throws IOException {
        write(WbXmlLiterals.OPAQUE);
        writeUnsignedInteger(data.length);
        write(data);
    }
    
    /**
     * The tag when encoding a WBXML document should be processed to mark if
     * it has attributes and/or content. It is explained in the chapter 
     * <em>5.8.2. Tag Code Space</em>.
     * 
     * <p>A TAG when writing should be marked in its 6 and 7 most significant bits
     * as the specification determines:</p>
     * 
     * <ul>
     * <li>7 (most significant). Indicates whether attributes follow the tag 
     * code. If this bit is zero, the tag contains no attributes. If this bit 
     * is one, the tag is followed immediately by one or more attributes. 
     * The attribute list is terminated by an END token.</li>
     * <li>6. Indicates whether this tag begins an element containing content. 
     * If this bit is zero, the tag contains no content and no end tag. If this 
     * bit is one, the tag is followed by any content it contains and is 
     * terminated by an END token.</li>
     * <li>5 - 0. Indicates the tag identity.</li>
     * </ul>
     * 
     * <p>this method converts the tag read from the specification marking 7 
     * and 6 bit depending if the attribute has or not attributes and contents.
     * </p>
     * 
     * @param tag The tag read from the language definition (5-0 bits)
     * @param hasAttributes true if the element has attributes, false if not
     * @param hasContent true if the element has contents, false if not
     * @return The tag with 6 and 7 bit correctly set
     */
    static public byte processTag(byte tag, boolean hasAttributes, boolean hasContent) {
        if (hasAttributes) {
            tag |= WbXmlLiterals.TAG_ATTRIBUTES_MASK;
        }
        if (hasContent) {
            tag |= WbXmlLiterals.TAG_CONTENT_MASK;
        }
        return tag;
    }
    
    /**
     * If a tag has no correspondence to any known token in the language 
     * definition it should be expressed using a literal tag. The tag to use
     * differ if the element has attribute and/or contents.
     * 
     * <p>In chapter <em>5.8.2. Tag Code Space</em> the specification talks
     * about those literals: The globally unique codes LITERAL, LITERAL_A, 
     * LITERAL_C, and LITERAL_AC represent unknown tag names. (Note that the 
     * tags LITERAL_A, LITERAL_C, and LITERAL_AC are the LITERAL tag with the
     * appropriate combinations of bits 6 and 7 set.) An XML tokeniser should 
     * avoid the use of the literal or string representations of a tag when a 
     * more compact form is available.</p>
     * 
     * <p>This method return the appropriate literal to use depending
     * if the element has attributes and/or contents.</p>
     * 
     * @param hasAttributes true if the element has attributes, false if not
     * @param hasContent true if the element has contents, false if not
     * @return The appropriate literal to use
     */
    static public byte processLiteralTag(boolean hasAttributes, boolean hasContent) {
        if (hasAttributes && hasContent) {
            return WbXmlLiterals.LITERAL_AC;
        } else if (hasContent) {
            return WbXmlLiterals.LITERAL_C;
        } else if (hasAttributes) {
            return WbXmlLiterals.LITERAL_A;
        } else {
            return WbXmlLiterals.LITERAL;
        }
    }
    
    /**
     * Main method of the encoder object. This is the method that should be
     * called when encoding a document. The whole document is encoded in the
     * Output Stream passed in the constructor. This is the method that,
     * depending the strtbl type of processing, performs one or two passes.
     * @throws IOException Some error writing the document to the stream
     */
    public void encode() throws IOException {
        if (StrtblType.NO.equals(this.type)) {
            // not use tblstr, strtbl generates an exception if used
            this.os = this.realOs;
            encode(doc);
        } else {
            // maybe it needs two passes, uses a byte array output stream
            ByteArrayOutputStream bos = null; 
            try {
                log.log(Level.FINE, "Performing first pass using a byte array");
                bos = new ByteArrayOutputStream(BYTE_ARRAY_INITIAL_LENGTH);
                this.os = bos;
                encode(doc);
                if (this.strtblUsed) {
                    // second pass needed cos strtbl was used, now using real os
                    // reset the decoder to restart with correct pages
                    reset();
                    log.log(Level.FINE, "Performing second pass into real stream cos strtbl used");
                    os = realOs;
                    encode(doc);
                } else {
                    // second pass not needed => just bulk write the full bytes
                    // into the real output stream
                    log.log(Level.FINE, "Dumping byte arrays cos strtbl not used");
                    realOs.write(((ByteArrayOutputStream) os).toByteArray());
                }
            } finally {
                if (bos != null) {
                    bos.close();
                }
                this.os = null;
            }
            
        }
    }
    
    /**
     * Method that encodes the version. The version is defined in the 
     * specification as follows:
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
     * @param version The version to write
     * @throws IOException Some error writing to the stream
     */
    public void encode(WbXmlVersion version) throws IOException {
        byte v = (byte) (((version.getMajor() - 1) << 4) | (version.getMinor()));
        write(v);
    }
    
    /**
     * method that encodes the string table to the stream. The string
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
     * relative starting idex of the string in the table.
     * 
     * @param strtbl The string table to write
     * @throws IOException Some error writing to the stream
     */
    public void encode(WbXmlStrtbl strtbl) throws IOException {
        // write the size of the strtbl
        writeUnsignedInteger(strtbl.getSize());
        if (strtbl.getSize() > 0) {
            // iterate over all the strings in the table
            // they are ordered cos it is stored in a TreeMap
            for (long idx: strtbl.getIndexes()) {
                writeTableString(strtbl.getString(idx));
            }
        }
    }
    
    /**
     * Method that encodes the whole document to the stream. The method writes
     * the different elements that compounds a WBXML document, following
     * the specifications are the following:
     * 
     * <pre>
     * start = version publicid charset strtbl body
     * 
     * publicid = mb_u_int32 | ( zero index )
     * zero = u_int8        // with a 0x0 value
     * index = mb_u_int32   // integer index into string table.
     * 
     * charset = mb_u_int32
     * </pre>
     * 
     * <p>So the method starts writing the five element one after the other.</p>
     * 
     * @param doc
     * @throws IOException 
     */
    public void encode(WbXmlDocument doc) throws IOException {
        // write the fixed data
        encode(doc.getVersion());
        if (doc.getDefinition().getPublicId() == WbXmlDefinition.PUBLIC_ID_UNKNOWN
                && !StrtblType.NO.equals(type)
                && doc.getDefinition().getXmlPublicId() != null) {
            // unknown wbxml public id => write the xml public id using strtbl
            writeUnsignedInteger(WbXmlDefinition.PUBLIC_ID_STR_T);
            long idx = doc.getStrtbl().addString(this, doc.getDefinition().getXmlPublicId());
            writeUnsignedInteger(idx);
        } else {
            // write normal number or unknown if NO strtbl is used
            writeUnsignedInteger(doc.getDefinition().getPublicId());
        }
        if (doc.getCharset() != null) {
            writeUnsignedInteger(doc.getCharset().getMibEnum());
        } else {
            writeUnsignedInteger(0);
        }
        encode(doc.getStrtbl());
        reset();
        encode(doc.getBody());
    }
    
    /**
     * Method that encodes a element into the output stream. The element is
     * defined in the specification as follows:
     * 
     * <pre>
     * element = ([switchPage] stag) [ 1*attribute END ] [ *content END ] 
     * stag = TAG | (literalTag index) 
     * literalTag = LITERAL | LITERAL_A | LITERAL_C | LITERAL_AC
     * </pre>
     * 
     * <p>So the method writes the tag or literal (depending it is defined
     * in the language definition or not) and the the attributes and contents
     * are encoded.</p>
     * 
     * @param element The element to encode to the stream
     * @throws IOException Some error writing to the stream
     */
    public void encode(WbXmlElement element) throws IOException {
        // get the tag for this element
        WbXmlTagDef def = getDefinition().locateTag(element.getTag());
        if (def != null) {
            // found stag => normal encoding
            // switchPage
            writeSwitchPageTag(def.getToken().getPageCode());
            // stag
            write(WbXmlEncoder.processTag(def.getToken().getToken(),
                    !element.isAttributesEmpty(), !element.isContentsEmpty()));
        } else {
            // unknown tag => literal used
            log.log(Level.WARNING, "Using literal TAG in element: {0}", element.getTag());
            write(WbXmlEncoder.processLiteralTag(!element.isAttributesEmpty(), !element.isContentsEmpty()));
            long idx = getStrtbl().addString(this, element.getTag());
            writeUnsignedInteger(idx);
        }
        if (!element.isAttributesEmpty()) {
            // 1*attributes
            for (WbXmlAttribute attr : element.getAttributes()) {
                encode(element, attr);
            }
            // END
            write(WbXmlLiterals.END);
        }
        if (!element.isCompacted()) {
            element.compact(this);
        }
        if (!element.isContentsEmpty()) {
            // *content
            for (WbXmlContent content : element.getContents()) {
                OpaqueContentPlugin plugin = getDefinition().locateTagPlugin(element.getTag());
                if (plugin != null) {
                    plugin.encode(this, element, content);
                } else {
                    encode(content);
                }
            }
            // END
            write(WbXmlLiterals.END);
        }
    }
    
    /**
     * Method that encodes a content into the output stream. 
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
     * entcode = mb_u_int32
     * 
     * opaque = OPAQUE length *byte
     * 
     * pi = PI attrStart *attrValue END
     * </pre>
     * 
     * The opaque one is not treated here (see encode(WbXmlElement) but all
     * the rest of possibilities are treated here.
     * 
     * @param content The content to write to the output stream
     * @throws IOException Some error writing to the stream
     */
    public void encode(WbXmlContent content) throws IOException {
        if (content.getElement() != null) {
            encode(content.getElement());
        } else if (content.getString() != null) {
            WbXmlExtensionDef extDef = getDefinition().locateExtension(content.getString());
            if (extDef != null) {
                write(WbXmlLiterals.EXT_T_0);
                writeUnsignedInteger(extDef.getToken());
            } else if (content.isEntity()) {
                write(WbXmlLiterals.ENTITY);
                writeUnsignedInteger(content.getEntityNumber());
            } else {
                writeString(content.getString());
            }
        } else if (content.getPi() != null) {
            write(WbXmlLiterals.PI);
            encode(null, content.getPi());
            write(WbXmlLiterals.END);
        }
    }
    
    /**
     * Encode an attribute value using the attr values defined in the
     * definition.
     * @param value The value to write
     * @throws IOException Some error writing to the stream
     */
    public void encodeAttributeValue(String value) throws IOException {
        WbXmlAttributeValueDef valueAttrDef = getDefinition().locateAttributeValue(value);
        if (valueAttrDef != null) {
            // it is an attribute value
            writeSwitchPageAttribute(valueAttrDef.getToken().getPageCode());
            write(valueAttrDef.getToken().getToken());
        } else {
            WbXmlExtensionDef extDef = getDefinition().locateExtension(value);
            if (extDef != null) {
                write(WbXmlLiterals.EXT_T_0);
                writeUnsignedInteger(extDef.getToken());
            } else {
                writeString(value);
            }
        }
    }
    
    /**
     * Method that encodes a complete attribute into the output stream. An
     * attribute is defined in the specifications as follows:
     * 
     * <pre>
     * attribute = attrStart *attrValue 
     * 
     * attrStart = ([switchPage] ATTRSTART) | (LITERAL index )
     * 
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
     * So the method tries to follow the specification.
     * 
     * @param element The element the attr belongs to (null in case of PI)
     * @param attr The attribute to write into the stream
     * @throws IOException  Some error writing to the stream
     */
    public void encode(WbXmlElement element, WbXmlAttribute attr) throws IOException {
        // search the best attribute definition
        String firstValue = null;
        if (!attr.isValuesEmpty()) {
            firstValue = attr.getValue(0);
        }
        // write attrStart
        WbXmlAttributeDef def = getDefinition().locateAttribute(attr.getName(), firstValue);
        if (def != null) {
            // tag exists
            writeSwitchPageAttribute(def.getToken().getPageCode());
            write(def.getToken().getToken()); 
        } else {
            // literal
            log.log(Level.WARNING, "Using literal TAG in attribute: {0}", attr.getName());
            write(WbXmlLiterals.LITERAL);
            long idx = getStrtbl().addString(this, attr.getName());
            writeUnsignedInteger(idx);
        }
        // compact the value string using attribute values or extensions
        if (!attr.isCompacted()) {
            attr.compact(this, def);
        }
        // write attrValues
        boolean first = true;
        for (String v: attr.getValues()) {
            if (first && def != null && def.getValue() != null) {
                v = v.substring(def.getValue().length());
                if (v.isEmpty()) {
                    first = false;
                    continue;
                }
            }
            OpaqueAttributePlugin plugin = null;
            if (def != null ) {
                plugin = getDefinition().locateAttrPlugin(def.getNameWithPrefix());
            }
            if (plugin != null) {
                plugin.encode(this, element, attr, v);
            } else {
                encodeAttributeValue(v);
            }
            first = false;
        }
    }
    
    /**
     * Method that encodes the body of a document. The body is as follows:
     * 
     * <pre>
     * body = *pi element *pi
     * </pre>
     * 
     * @param body The body to write into the stream
     * @throws IOException Some error writing to the stream
     */
    public void encode(WbXmlBody body) throws IOException {
        for (WbXmlAttribute pi: body.getPrePis()) {
            write(WbXmlLiterals.PI);
            encode(null, pi);
            write(WbXmlLiterals.END);
        }
        encode(body.getElement());
        for (WbXmlAttribute pi: body.getPostPis()) {
            write(WbXmlLiterals.PI);
            encode(null, pi);
            write(WbXmlLiterals.END);
        }
    }
}
