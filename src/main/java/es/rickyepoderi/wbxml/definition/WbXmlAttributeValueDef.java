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
package es.rickyepoderi.wbxml.definition;

/**
 * 
 * <p>Class that represents an attribute value defined in any WBXML language
 * definition. Same as attributes attribute values are commented in the
 * chapter em>5.8.3. Attribute Code Space (ATTRSTART and ATTRVALUE)</em> of
 * the WXML speficification.</p>
 * 
 * <p> The document defined an attribute value as follows: Attribute Value - 
 * tokens with a value of 128 or greater represent a well-known string present 
 * in an attribute value. These tokens may only be used to represent attribute 
 * values. Unknown attribute values are encoded with string, entity or extension 
 * codes.</p>
 * 
 * <p>Therefore an attribute value is a part of an attribute value or the 
 * complete one. Any attribute can be coded in WBXML using zero, one or more
 * attribute values...</p>
 * 
 * <p>In the properties file that is used to define any language in this library
 * a value consists in two keys:</p>
 * 
 * <ul>
 * <li>wbxml.attrvalue.{pageCode}[.{optional}]={token}</li>
 * <li>{previous_key}.value={value}</li>
 * </ul>
 * 
 * <p>The first key define the page and token byte and the second the real
 * value for the token.</p>
 *
 * @author ricky
 */
public class WbXmlAttributeValueDef implements Comparable<WbXmlAttributeValueDef> {

    /**
     * The value of the token.
     */
    private String value = null;
    
    /**
     * The token (token byte and page code).
     */
    private WbXmlToken token = null;

    /**
     * Constructor using value, token byte and page code.
     * @param value The value of the attribute
     * @param token The token byte (128 or greater)
     * @param pageCode The page code
     */
    protected WbXmlAttributeValueDef(String value, byte token, byte pageCode) {
        this(value, new WbXmlToken(pageCode, token));
    }
    
    /**
     * Constructor using value and token pair object
     * @param value The string value
     * @param token The token pair (token byte and page code)
     */
    protected WbXmlAttributeValueDef(String value, WbXmlToken token) {
        this.value = value;
        this.token = token;
    }

    /**
     * Getter for the value
     * @return The value associated to this attribute value definition
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter for the new value.
     * @param value The new value associated
     */
    protected void setValue(String value) {
        this.value = value;
    }

    /**
     * Getter for the token pair (page code and byte)
     * @return The token pair 
     */
    public WbXmlToken getToken() {
        return token;
    }

    /**
     * Setter for the token pair.
     * @param token The new token
     */
    public void setToken(WbXmlToken token) {
        this.token = token;
    }

    /**
     * Comparison is done first using length of the value (the more large string
     * the better to be used) and then normal string comparison of the value.
     * @param attrVal The attribute value to compare to
     * @return a negative integer, zero, or a positive integer as this object 
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(WbXmlAttributeValueDef attrVal) {
        int result = 0;
        if (this.value == null && attrVal.getValue() == null) {
            result = 0;
        } else if (this.value == null) {
            result = 1;
        } else if (attrVal.getValue() == null) {
            result = -1;
        } else {
            // check first via length
            result = new Integer(attrVal.getValue().length()).compareTo(this.getValue().length());
            if (result == 0) {
                result = this.value.compareTo(attrVal.getValue());
            }
        }
        return result;
    }

    /**
     * String representation of an attribute value.
     * @return The string representation
     */
    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getName())
                .append(": ")
                .append(value)
                .append("->")
                .append(token)
                .append(System.getProperty("line.separator"))
                .toString();
    }
}
