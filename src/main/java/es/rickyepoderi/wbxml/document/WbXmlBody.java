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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * <p>Java representation of the body of the WBXML format. Body is the
 * main part of the WBXML document (all WBXML documents have a unique body).
 * based on the format specification the body is the following:</p>
 * 
 * <pre>
 * body = *pi element *pi
 * 
 * pi = PI attrStart *attrValue END
 * </pre>
 * 
 * <p>The class is just a pre list of attributes (PI is just an attribute
 * with PI and END token), and element and a list of post pis attributes.</p>
 * 
 * @author ricky
 */
public class WbXmlBody {
    
    /**
     * List of the pre PIs attributes.
     */
    List<WbXmlAttribute> prePi = null;
    
    /**
     * Main element of the document.
     */
    WbXmlElement element = null;
    
    /**
     * List of post PIs attributes.
     */
    List<WbXmlAttribute> postPi = null;
    
    /**
     * Empty constructor.
     */
    public WbXmlBody() {
        this(null, null, null);
    }
    
    /**
     * Constructor using only the element.
     * @param element The element of the body
     */
    public WbXmlBody(WbXmlElement element) {
        this(null, element, null);
    }
    
    /**
     * Constructor using all pre PIs, element and post PIs.
     * @param prePi The pre PIs attributes
     * @param element The main element of the document
     * @param postPi The post PIs attributes
     */
    public WbXmlBody(WbXmlAttribute[] prePi, WbXmlElement element, WbXmlAttribute[] postPi) {
        this.element = element;
        this.prePi = new ArrayList<WbXmlAttribute>();
        if (prePi != null) {
            this.prePi.addAll(Arrays.asList(prePi));
        }
        this.postPi = new ArrayList<WbXmlAttribute>();
        if (postPi != null) {
            this.postPi.addAll(Arrays.asList(postPi));
        }
    }
    
    /**
     * Method that adds a new pi attribute to the pre PIs list.
     * @param pi The new PI attribute to add
     * @return The same body to chain calls
     */
    public WbXmlBody addPrePi(WbXmlAttribute pi) {
        this.prePi.add(pi);
        return this;
    }
    
    /**
     * Getter for the pre PIs list.
     * @param i The position of the PI to return
     * @return The PI attribute in the i position
     */
    public WbXmlAttribute getPrePis(int i) {
        return this.prePi.get(i);
    }
    
    /**
     * Returns if the pre PI list is empty.
     * @return true if empty false otherwise
     */
    public boolean IsPrePiEmpty() {
        return this.prePi.isEmpty();
    }
    
    /**
     * Getter for the complete pre PIs list.
     * @return The list of pre PI attributes
     */
    public List<WbXmlAttribute> getPrePis() {
        return this.prePi;
    }
    
    /**
     * Setter for the element.
     * @param element The element to set
     * @return The same body to chain calls
     */
    protected WbXmlBody setElement(WbXmlElement element) {
        this.element = element;
        return this;
    }
    
    /**
     * Getter for the element.
     * @return The element of the document body
     */
    public WbXmlElement getElement() {
        return this.element;
    }
    
    /**
     * Adds a new PI attribute for the list of post PIs.
     * @param pi The PI attribute to add at the end of the post PI list
     * @return The same body to chain calls
     */
    public WbXmlBody addPostPi(WbXmlAttribute pi) {
        this.postPi.add(pi);
        return this;
    }
    
    /**
     * Getter for the post PI attributes. It returns the PI attribute in the i
     * position of the post PI list.
     * @param i The position to get the attribute from
     * @return The PI attribute at that position in post PI list
     */
    public WbXmlAttribute getPostPis(int i) {
        return this.postPi.get(i);
    }
    
    /**
     * Checks if the post PI attribute list is empty.
     * @return true if empty false otherwise
     */
    public boolean IsPostPiEmpty() {
        return this.postPi.isEmpty();
    }
    
    /**
     * Getter for the complete list of post PI attributes.
     * @return The list of post PI attributes
     */
    public List<WbXmlAttribute> getPostPis() {
        return this.postPi;
    }

    /**
     * String representation with identation.
     * @param ident The indentation level
     * @return String representation
     */
    public String toString(int ident) {
        String spaces = WbXmlLiterals.identString(ident);
        StringBuilder sb = new StringBuilder(spaces);
        sb.append(this.getClass().getSimpleName());
        sb.append(": ");
        sb.append(System.getProperty("line.separator"));
        ident++;
        for (WbXmlAttribute pi: prePi) {
            sb.append("PI: ");
            sb.append(pi.toString(ident));
            sb.append(System.getProperty("line.separator"));
        }
        sb.append(element.toString(ident));
        for (WbXmlAttribute pi: prePi) {
            sb.append("PI: ");
            sb.append(pi.toString(ident));
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    
    /**
     * String representation.
     * @return The string representation of the body
     */
    @Override
    public String toString() {
        return toString(0);
    }
}
