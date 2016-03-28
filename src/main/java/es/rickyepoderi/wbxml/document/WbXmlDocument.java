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
import es.rickyepoderi.wbxml.definition.WbXmlDefinition;

/**
 * 
 * <p>Java representation of a whole WBXML document. Following the 
 * specification a document is the following:
 * 
 * <pre>
 * start = version publicid charset strtbl body
 * 
 * publicid = mb_u_int32 | ( zero index )
 * 
 * charset = mb_u_int32
 * 
 * </pre>
 * 
 * <p>So the document is the version (1.3), the language definition, 
 * the charset used (IANA mib), the string table and the body part.</p>
 * 
 * @author ricky
 */
public class WbXmlDocument {
    
    /**
     * Version of the WBXML.
     */
    private WbXmlVersion version = null;
    
    /**
     * Language definition used in the document.
     */
    private WbXmlDefinition definition = null;
    
    /**
     * Charset used in the document.
     */
    private IanaCharset charset = null;
    
    /**
     * The string table.
     */
    private WbXmlStrtbl strtbl = null;
    
    /**
     * The body (main element) part.
     */
    private WbXmlBody body = null;
    
    /**
     * Empty constructor.
     */
    public WbXmlDocument() {
        this(WbXmlVersion.VERSION_1_3, null, null, null);
    }
    
    /**
     * Constructor with the definition.
     * @param definition The used definition
     */
    public WbXmlDocument(WbXmlDefinition definition) {
        this(WbXmlVersion.VERSION_1_3, definition, null, null);
    }
    
    /**
     * Constructor with the version.
     * @param version The version of the document
     */
    public WbXmlDocument(WbXmlVersion version) {
        this(version, null, null, null);
    }
    
    /**
     * Constructor using the definition and the charset
     * @param definition The definition of the document
     * @param charset The charset of the documents
     */
    public WbXmlDocument(WbXmlDefinition definition, IanaCharset charset) {
        this(WbXmlVersion.VERSION_1_3, definition, charset, null);
    }
    
    /**
     * Constructor using version and definition
     * @param version The version of the document
     * @param definition The definition of the document
     */
    public WbXmlDocument(WbXmlVersion version, WbXmlDefinition definition) {
        this(version, definition, null, null);
    }
    
    /**
     * Constructor using version and charset
     * @param version The version of the document
     * @param charset The charset of the document
     */
    public WbXmlDocument(WbXmlVersion version, IanaCharset charset) {
        this(version, null, charset, null);
    }
    
    /**
     * Constructor using the version, definition and charset.
     * @param version The version of the document
     * @param definition The definition of the document
     * @param charset The charset of the document
     */
    public WbXmlDocument(WbXmlVersion version, WbXmlDefinition definition, IanaCharset charset) {
        this(version, definition, charset, null);
    }
    
    /**
     * Constructor with all the possibilities (version, definition, charset and body)
     * @param version The version of the document
     * @param definition The definition of the document
     * @param charset The charset of the document
     * @param body The body of the document
     */
    public WbXmlDocument(WbXmlVersion version, WbXmlDefinition definition, IanaCharset charset, WbXmlBody body) {
        this.version = WbXmlVersion.VERSION_1_3;
        this.definition = definition;
        this.strtbl = new WbXmlStrtbl();
        this.charset = charset;
        this.body = body;
    }

    /**
     * Getter for the version
     * @return The version
     */
    public WbXmlVersion getVersion() {
        return version;
    }

    /**
     * Setter for the version
     * @param version The new version
     */
    protected void setVersion(WbXmlVersion version) {
        this.version = version;
    }
    
    /**
     * Getter for the body
     * @return The body
     */
    public WbXmlBody getBody() {
        return this.body;
    }
    
    /**
     * Setter for the body.
     * @param body The new body
     * @return The same body to chain calls
     */
    public WbXmlDocument setBody(WbXmlBody body) {
        this.body = body;
        return this;
    }

    /**
     * Getter for the definition
     * @return The language definition
     */
    public WbXmlDefinition getDefinition() {
        return definition;
    }

    /**
     * Setter for the defnition.
     * @param definition The new definition
     */
    public void setDefinition(WbXmlDefinition definition) {
        this.definition = definition;
    }

    /**
     * Getter for the charset
     * @return The charset
     */
    public IanaCharset getCharset() {
        return charset;
    }

    /**
     * Setter for the charset
     * @param charset The new charset
     */
    protected void setCharset(IanaCharset charset) {
        this.charset = charset;
    }

    /**
     * Getter for the String Table
     * @return Teh string table
     */
    public WbXmlStrtbl getStrtbl() {
        return strtbl;
    }

    /**
     * Setter for the string table
     * @param strtbl The new string table
     */
    protected void setStrtbl(WbXmlStrtbl strtbl) {
        this.strtbl = strtbl;
    }

    /**
     * String representation with indentation.
     * @param ident The indentation to use
     * @return The string representation
     */
    public String toString(int ident) {
        String spaces = WbXmlLiterals.identString(ident);
        StringBuilder sb = new StringBuilder(spaces);
        sb.append(this.getClass().getSimpleName());
        sb.append(": ");
        sb.append(System.getProperty("line.separator"));
        // version
        sb.append(spaces);
        sb.append(version);
        sb.append(System.getProperty("line.separator"));
        // definition
        sb.append(spaces);
        sb.append("Definition: ");
        sb.append(definition.getName());
        sb.append(System.getProperty("line.separator"));
        // charset
        sb.append(spaces);
        sb.append("Charset: ");
        sb.append(charset.getName());
        sb.append(System.getProperty("line.separator"));
        // strtbl
        sb.append(spaces);
        sb.append(strtbl.toString(++ident));
        // body
        sb.append(spaces);
        sb.append(body.toString(ident));
        return sb.toString();
    }
    
    /**
     * String representation.
     * @return The string representation
     */
    @Override
    public String toString() {
        return toString(0);
    }
}
