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

import es.rickyepoderi.wbxml.document.WbXmlLiterals;

/**
 * <p>A extension in the WBXML specification is any string that can be
 * used inside attribute values and tag PCDATA to compact the value. For
 * example if a extension is defined for "ENABLED" and attribute
 * value <em>status="ENABLED"</em> can be compacted for the extension and
 * if the value is <em>status="PARTIALLYENABLED"</em> the value is
 * partially substituted (PARTIALLY and the extension). In the definition
 * the extension is just a mb_u_int32 token which represents a string value.
 * In the specification extensions are explained in chapter 
 * <em>5.8.4.2. Global Extension Tokens</em>. There there are three types
 * of extensions but only the EXT_T is used.</p>
 * 
 * <p>In the properties file any extension is defined by two keys, the first 
 * one assigns the integer id and the second the value:</p>
 * 
 * <ul>
 * <li>wbxml.ext.{key_differenciator}={token}</li>
 * <li>{previous_key}.value={value}</li>
 * </ul>
 * 
 * TODO: libwbxml defines an extension as a unsigned byte (255), nevertheless
 * the WBXML format assigns a mb_u_int32... Take care!
 * 
 * @author ricky
 */
public class WbXmlExtensionDef implements Comparable<WbXmlExtensionDef> {
    
    /**
     * The string value of the extension.
     */
    private String value = null;
    
    /**
     * The token or identifier.
     */
    private byte token = 0;

    /**
     * Constructor via value and token.
     * @param value The value string
     * @param token The token id
     */
    protected WbXmlExtensionDef(String value, byte token) {
        this.value = value;
        this.token = token;
    }
    
    /**
     * getter for the value
     * @return The value for the extension definition
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter for the value
     * @param value  The new value
     */
    protected void setValue(String value) {
        this.value = value;
    }

    /**
     * Getter for the token id
     * @return The token
     */
    public byte getToken() {
        return token;
    }

    /**
     * Setter for the token
     * @param token The token
     */
    protected void setToken(byte token) {
        this.token = token;
    }

    /**
     * Comparable method that first check lengths and then normal lexicographic
     * order. This way larger extensions are checked first in compactation methods.
     * @param ext The extension to compare
     * @return a negative integer, zero, or a positive integer as this object 
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(WbXmlExtensionDef ext) {
        int result;
        if (this.value == null && ext.getValue() == null) {
            result = 0;
        } else if (this.value == null) {
            result = 1;
        } else if (ext.getValue() == null) {
            result = -1;
        } else {
            // check first via length
            result = new Integer(ext.getValue().length()).compareTo(this.getValue().length());
            if (result == 0) {
                result = this.value.compareTo(ext.getValue());
            }
        }
        return result;
    }
    
    /**
     * String representation of the extension.
     * @return The string representation
     */
    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getName())
                .append(": ")
                .append(value)
                .append("->")
                .append(WbXmlLiterals.formatUInt8(token))
                .append(System.getProperty("line.separator"))
                .toString();
    }
}
