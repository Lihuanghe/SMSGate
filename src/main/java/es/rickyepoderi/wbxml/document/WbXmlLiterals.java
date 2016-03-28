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


/**
 *
 * <p>WBXML specification defines in the chapter <em>7.1. Global Tokens</em>
 * a list of known tokens. This class has all those tokens and some utility
 * methods.</p>
 * 
 * @author ricky
 */
public final class WbXmlLiterals {
    
    /**
     * Change the code page for the current token state. Followed by a
     * single u_int8 indicating the new code page number.
     */
    static public final byte SWTICH_PAGE = 0x00;
    
    /**
     * Indicates the end of an attribute list or the end of an element.
     */
    static public final byte END = 0x01;
    
    /**
     * A character entity. Followed by a mb_u_int32 encoding the
     * character entity number.
     */
    static public final byte ENTITY = 0x02;
    
    /**
     * An unknown attribute name, or unknown tag posessing no
     * attributes or content.Followed by a mb_u_int32 that encodes
     * an offset into the string table.
     */
    static public final byte LITERAL = 0x04;
    
    /**
     * An unknown tag posessing content but no attributes.
     */
    static public final byte LITERAL_C = 0x44;
    
    /**
     * An unknown tag posessing attributes but no content.
     */
    static public final byte LITERAL_A = (byte) 0x84;
    
    /**
     * An unknown tag posessing both attributes and content.
     */
    static public final byte LITERAL_AC = (byte) 0xC4;
    
    /**
     * Inline string. Followed by a termstr.
     */
    static public final byte STR_I = 0x03;
    
    /**
     * String table reference. Followed by a mb_u_int32 encoding a
     * byte offset from the beginning of the string table.
     */
    static public final byte STR_T = (byte) 0x83;
    
    /**
     * Processing instruction.
     */
    static public final byte PI = 0x43;
    
    /**
     * Inline string document-type-specific extension token. Token is
     * followed by a termstr.
     */
    static public final byte EXT_I_0 = 0x40;
    
    /**
     * Inline string document-type-specific extension token. Token is
     * followed by a termstr.
     */
    static public final byte EXT_I_1 = 0x41;
    
    /**
     * Inline string document-type-specific extension token. Token is
     * followed by a termstr.
     */
    static public final byte EXT_I_2 = 0x42;
    
    /**
     * Inline integer document-type-specific extension token. Token is
     * followed by a mb_u_int32.
     */
    static public final byte EXT_T_0  = (byte) 0x80;
    
    /**
     * Inline integer document-type-specific extension token. Token is
     * followed by a mb_u_int32.
     */
    static public final byte EXT_T_1  = (byte) 0x81;
    
    /**
     * Inline integer document-type-specific extension token. Token is
     * followed by a mb_u_int32.
     */
    static public final byte EXT_T_2  = (byte) 0x82;
    
    /**
     * Single -byte document-type-specific extension token.
     */
    static public final byte EXT_0  = (byte) 0xC0;
    
    /**
     * Single -byte document-type-specific extension token.
     */
    static public final byte EXT_1  = (byte) 0xC1;
    
    /**
     * Single -byte document-type-specific extension token.
     */
    static public final byte EXT_2  = (byte) 0xC2;
    
    /**
     * Opaque document-type-specific data.
     */
    static public final byte OPAQUE = (byte) 0xC3;
    
    /**
     * TAG mask to check if a tag has attributes or not
     */
    static public final byte TAG_ATTRIBUTES_MASK = (byte) 0x80;
    
    /**
     * TAG mask to check if a tag has contents or not
     */
    static public final byte TAG_CONTENT_MASK = 0x40;
    
    /**
     * Utility method to print a byte in hexa 0x
     * @param b The byte to print
     * @return The string in hexa 0x__
     */
    static public String formatUInt8(byte b) {
        return String.format("%#4x", new Byte(b).intValue() & 0xFF);
    }
    
    /**
     * Utility method to print a byte in hexa an ascii
     * @param b The byte to print
     * @return The string 0x__:c
     */
    static public String formatUInt8Char(byte b) {
        String ch = new String(new byte[]{b});
        ch = ch.replaceAll("\\p{C}", " ");
        return formatUInt8(b) + ":" + ch;
    }
    
    /**
     * Utility method to get the indentation spaces.
     * @param ident The indentation 
     * @return The spaces string
     */
    static protected String identString(int ident) {
        StringBuilder sb = new StringBuilder();
        String spaces = "  ";
        for (int i = 0; i < ident; i++) {
            sb.append(spaces);
        }
        return sb.toString();
    }
}
