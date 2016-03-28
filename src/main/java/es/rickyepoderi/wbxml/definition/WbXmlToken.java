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
 *
 * <p>This class just represents a pair of bytes to define page code and
 * a byte token. In WBXML a single byte is used to define tags and attributes,
 * to let more possible values a page code concept is introduced. The way
 * the page code is used is explained in the chapter <em>5.8.1. Parser State Machine</em>
 * of the specification. In simple words the the page byte is reused if some tags
 * belongs to same page (there is a concept of switch page, to change the
 * current page of tokens).</p>
 * 
 * <p>This class just joins the byte id and the page the element belongs to. 
 * Pages only affect tags and attributes (attributes and their values).</p>
 * 
 * @author ricky
 */
public class WbXmlToken {
    
    /**
     * The id byte token
     */
    private byte token = 0;
    
    /**
     * The page code
     */
    private byte pageCode = 0;
    
    /**
     * Constructor using both bytes
     * @param pageCode The page code
     * @param token The byte token
     */
    public WbXmlToken(byte pageCode, byte token) {
        this.pageCode = pageCode;
        this.token = token;
    }

    /**
     * getter for the token byte
     * @return The token byte
     */
    public byte getToken() {
        return token;
    }

    /**
     * getter for the page byte
     * @return The page byte
     */
    public byte getPageCode() {
        return pageCode;
    }

    /**
     * hashcode based in the two bytes
     * @return The hashcode
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.token;
        hash = 97 * hash + this.pageCode;
        return hash;
    }

    /**
     * Equals based in the two bytes
     * @param obj The obj to compare
     * @return true if both object are equals, false if not
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WbXmlToken other = (WbXmlToken) obj;
        if (this.token != other.token) {
            return false;
        }
        if (this.pageCode != other.pageCode) {
            return false;
        }
        return true;
    }
    
    /**
     * String representation of the token.
     * @return The string representation
     */
    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getSimpleName())
                .append(": ")
                .append(WbXmlLiterals.formatUInt8(pageCode))
                .append("|")
                .append(WbXmlLiterals.formatUInt8(token))
                .toString();
    }
}
