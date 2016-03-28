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
 * <p>The version of the WBXML document is defined in the specification in
 * the following chapter <em>5.4. Version Number</em></p>
 * 
 * <pre>
 * version = u_int8 // WBXML version number
 * </pre>
 * 
 * <p>The version is just the major and minor number. Although all versions are
 * defined only 1.3 is used (parser and encoder do not differentiate between
 * versions. Here a enumeration is used.</p>
 * 
 * @author ricky
 */
public enum WbXmlVersion {
    
    /**
     * Version 1.0
     */
    VERSION_1_0((byte)1, (byte)0),
    
    
    /**
     * Version 1.1
     */
    VERSION_1_1((byte)1, (byte)1),
    
    /**
     * Version 1.2
     */
    VERSION_1_2((byte)1, (byte)2),
    
    /**
     * Version 1.3
     */
    VERSION_1_3((byte)1, (byte)3);
    
    /**
     * major version number
     */
    private byte major;
    
    /**
     * minor version number
     */
    private byte minor;
    
    /**
     * Private constructor for the enumeration.
     * @param major The major version number
     * @param minor The minor version number
     */
    private WbXmlVersion(byte major, byte minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * Getter for the major number
     * @return The major number
     */
    public byte getMajor() {
        return major;
    }

    /**
     * Getter for the minor number
     * @return The minor number
     */
    public byte getMinor() {
        return minor;
    }

    /**
     * Searches over the list of versions to get the one that corresponds to
     * this major and minor.
     * @param major The major version number of the version to search
     * @param minor The minor version number of the version to search
     * @return The version or null
     */
    static public WbXmlVersion locateVersion(byte major, byte minor) {
        for (WbXmlVersion v : WbXmlVersion.values()) {
            if (v.getMajor() == major && v.getMinor() == minor) {
                return v;
            }
        }
        return null;
    }

    /**
     * String representation with indentation
     * @param ident The indentation to use
     * @return The string representation
     */
    public String toString(int ident) {
        return new StringBuilder(WbXmlLiterals.identString(ident))
                .append("version: ")
                .append(major)
                .append(".")
                .append(minor)
                .toString();
    }
    
    /**
     * String representation
     * @return The strong representation
     */
    @Override
    public String toString() {
        return toString(0);
    }
}
