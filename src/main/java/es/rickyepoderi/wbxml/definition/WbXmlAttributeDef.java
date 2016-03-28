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
 * <p>This class represents the part of the specification for a WBXML
 * language that list the attributes in that language. This part is defined
 * in the chapter <em>5.8.3. Attribute Code Space (ATTRSTART and ATTRVALUE)</em>
 * of the specification.</p> 
 * 
 * <p>An attribute is a token represented in a byte (7 bits specifically cos
 * it should be less than 128) and it can represents the name of the attribute
 * and part (of all) of its value.</p>
 * 
 * <p> the commented chapter says the following: tokens with a value less 
 * than indicate the start of an attribute. The attribute start token fully 
 * identifies the attribute name, e.g., <em>URL=</em>, and may optionally 
 * specify the beginning of the attribute value, e.g.,<em>PUBLIC="TRUE"</em>. 
 * Unknown attribute names are encoded with the globally unique 
 * code LITERAL. LITERAL must not be used to encode any portion of an attribute 
 * value.</p>
 * 
 * <p>In the properties file the attributes are specified with the following
 * key format:</p>
 * 
 * <ul>
 * <li>wbxml.attr.{pageCode}.[{prefix}:]{name}[.{optional-differenciator}]={token}</li>
 * <li>{previous-key}.value={optional-value}</li>
 * </ul>
 * 
 * <p>The definition of the two attributes commented in the specification
 * would be the followint:</p>
 * 
 * <ul>
 * <li><p><em>URL=</em> in the page 0 with token 0x05:</p>
 * <p>
 * wbxml.attr.0.url=0x05
 * </p></li>
 * <li><p><em>PUBLIC="TRUE"</em> in page 0, tag 0x06:</p>
 * <p>
 * wbxml.attr.0.public.true=0x06<br/>
 * wbxml.attr.0.public.true=TRUE
 * </p></li>
 * </ul>
 * 
 * <p>As you see the idea is simple each attribute is defined in a page with
 * a numeric token less than 128, the optional value is represented using
 * a second key terminates with ".value". If the attribute is associated
 * with a namespace the attribute name is prefix:attribute-name.</p>
 * 
 * <p>The definition loads the attributes in such order that always the more
 * suited attributes is checked first (obviously if there are some keys
 * defined for the same attribute the more lengthed value the better.</p>
 * 
 * @author ricky
 */
public class WbXmlAttributeDef implements Comparable<WbXmlAttributeDef> {
    
    /**
     * The prefix of the attribute (used to link to the namespace).
     */
    private String prefix = null;
    
    /**
     * The attribute name.
     */
    private String name = null;
    
    /**
     * The token (page and token) associated to this attribute.
     */
    private WbXmlToken token = null;
    
    /**
     * The optional value part.
     */
    private String value = null;

    /**
     * Constructor via name, token and pageCode.
     * @param name The attribute name
     * @param token The token
     * @param pageCode  The page code
     */
    protected WbXmlAttributeDef(String name, byte token, byte pageCode) {
        this(null, name, new WbXmlToken(pageCode, token), null);
    }
    
    /**
     * Constructor using prefix, name, token and pageCode
     * @param prefix The prefix of the attribute
     * @param name The name of the attribute
     * @param token The token byte
     * @param pageCode The page code
     */
    protected WbXmlAttributeDef(String prefix, String name, byte token, byte pageCode) {
        this(prefix, name, new WbXmlToken(pageCode, token), null);
    }
    
    protected WbXmlAttributeDef(String name, WbXmlToken token) {
        this(null, name, token, null);
    }
    
    /**
     * Constructor via name, token, pageCode and value.
     * @param name The name of the attribute
     * @param token The token byte
     * @param pageCode The page code
     * @param value The value
     */
    protected WbXmlAttributeDef(String name, byte token, byte pageCode, String value) {
        this(null, name, new WbXmlToken(pageCode, token), value);
    }
    
    /**
     * Constructor via prefix, name, token, pageCode and value.
     * @param prefix The prefix (namespace) of the attribute
     * @param name The name of the attribute
     * @param token The token byte
     * @param pageCode The page code
     * @param value The value
     */
    protected WbXmlAttributeDef(String prefix, String name, byte token, byte pageCode, String value) {
        this(prefix, name, new WbXmlToken(pageCode, token), value);
    }
    
    /**
     * Constructor viake prefix, name, token object and value.
     * @param prefix The attribute prefix (namespace)
     * @param name The attribute name
     * @param token The token (token byte and page code)
     * @param value The value (optional)
     */
    protected WbXmlAttributeDef(String prefix, String name, WbXmlToken token, String value) {
        this.prefix = prefix;
        this.name = name;
        this.token = token;
        this.value = value;
    }
    
    /**
     * return the name prefixed "prefix:name" of the attribute, if there is no
     * namespace defined normal name is returned.
     * @return The prefixed name
     */
    public String getNameWithPrefix() {
        if (prefix != null) {
            return new StringBuilder(prefix).append(":").append(name).toString();
        } else {
            return name;
        }
    }
    
    /**
     * Getter for the name
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name
     * @param name The new name
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the token of this attribute (token byte and page code).
     * @return The token of this attribute in the language definition
     */
    public WbXmlToken getToken() {
        return token;
    }

    /**
     * Setter for the token pair.
     * @param token The new token associated to this attribute
     */
    public void setToken(WbXmlToken token) {
        this.token = token;
    }

    /**
     * Getter for the value
     * @return The value defined in this attribute (can be null)
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter for the optional value of the attribute.
     * @param value The new value
     */
    protected void setValue(String value) {
        this.value = value;
    }

    /**
     * Getter for the prefix (namespace link).
     * @return The prefix of the attribute
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Setter for the attribute prefix.
     * @param prefix Thew new prefix (should be one defined in the namespaces)
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * Comparison of attributes based in name and value.
     * @param attr The attribute to compare
     * @return a negative integer, zero, or a positive integer as this object 
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(WbXmlAttributeDef attr) {
        int result = this.getNameWithPrefix().compareTo(attr.getNameWithPrefix());
        if (result == 0) {
            if (this.value == null && attr.getValue() == null) {
                result = 0;
            } else if (this.value == null) {
                result = 1;
            } else if (attr.getValue() == null) {
                result = -1;
            } else {
                result = -this.value.compareTo(attr.getValue());
            }
        }
        return result;
    }
    
    /**
     * String representation for debugging purposes.
     * @return The string representation of an attribute definition.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName())
                .append(": ");
        if (prefix != null){
            sb.append(prefix);
            sb.append(":");
        }
        sb = sb.append(name);
        sb.append("=")
                .append(value)
                .append("->")
                .append(token)
                .append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
