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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 * <p>WBXML defines a string table that can be used in any parsing or
 * encoding process. The table is used to store strings that are being
 * referenced later in the document. All the STR_T strings marks
 * to the string table in order to get the string content of an attribute
 * value or from a string content. The chapter <em>5.7. String Table</em>
 * of the specification explains this table.</p>
 * 
 * <p>The format of the table is the length of itself and the raw byte
 * data with all the strings:</p>
 * 
 * <pre>
 * strtbl = length *byte
 * </pre>
 * 
 * <p>The strings in the table are encoded using the specified document charset
 * and terminated in 0x00 token. All the references in the document to those
 * strings are done using the starting position of the string in the byte array
 * (for example if you want to reference the first string is 0x00).</p>
 * 
 * 
 * @author ricky
 */
public class WbXmlStrtbl {
    
    /**
     * map of string in the table indexed by its start position. This let us
     * get the string very quickly with a index from the document.
     */
    private SortedMap<Long, String> strtblByIndex = null;
    
    /**
     * Reverse map, string value to index. Useful when encoding.
     */
    private Map<String, Long> strtblByString = null;
    
    /**
     * Total size of the table.
     */
    private long size = 0x0;
    
    /**
     * Empty constructor.
     */
    public WbXmlStrtbl() {
        this.strtblByIndex = new TreeMap<Long, String>();
        this.strtblByString = new HashMap<String, Long>();
    }
    
    /**
     * This method adds a new string to the string table. First it checks if the
     * string is already in the table, if not, the string is added and the
     * position and size is calculated. If the encoder does not permit the
     * use of the strtbl a exception is thrown. The encoder is marked using
     * the setStrtblUsed() method.
     * 
     * @param encoder The encoder used in the encoding process
     * @param s The string to add
     * @return The position or index in which the string is set (this is the index
     *         to be used in the document).
     * @throws IOException The encoder does not permit the use of the strtbl
     */
    protected long addString(WbXmlEncoder encoder, String s) throws IOException {
        // check if its already in the index table
        Long idx = strtblByString.get(s);
        if (idx == null) {
            if (WbXmlEncoder.StrtblType.NO.equals(encoder.getType())) {
                throw new IOException(String.format("The strtbl cannot be used for '%s'!", s));
            }
            idx = size;
            internalAddString(idx, s);
            size += strtblByIndex.get(idx).getBytes(encoder.getCharset()).length + 1;
            encoder.setStrtblUsed();
        }
        return idx;
    }
    
    /**
     * Method that returns the string at the specified index position.
     * @param idx The idx to get the string from
     * @return The string at that index position (not null)
     * @throws IOException That position does not contain a string
     */
    public String getString(long idx) throws IOException {
        String s = strtblByIndex.get(idx);
        if (s == null) {
            throw new IOException(String.format("The strtbl does not contain a string in index %d", idx));
        }
        return s;
    }
    
    /**
     * Internal method that add the string in both maps (string to idx and idx to string).
     * @param idx The index of the string
     * @param s The string
     */
    protected void internalAddString(long idx, String s) {
        strtblByIndex.put(idx, s);
        strtblByString.put(s, idx);
    } 
    
    /**
     * Setter for the size when parsing a WBXML document.
     * @param size The new size of the strtbl
     */
    protected void setSize(long size) {
        this.size = size;
    }
    
    /**
     * Getter of the size of the strtbl.
     * @return The size in bytes of the strtbl
     */
    public long getSize() {
        return size;
    }
    
    /**
     * The list of indexes that the strtbl is using to store strings.
     * @return The collection of all the indexes used.
     */
    public Collection<Long> getIndexes() {
        return strtblByIndex.keySet();
    }

    /**
     * String representation using indentation.
     * @param ident The indentation
     * @return The string representation
     */
    public String toString(int ident) {
        String spaces = WbXmlLiterals.identString(ident);
        StringBuilder sb = new StringBuilder(spaces);
        sb.append(this.getClass().getSimpleName());
        sb.append(": ");
        sb.append(System.getProperty("line.separator"));
        for (Entry<Long, String> e: strtblByIndex.entrySet()) {
            sb.append(spaces);
            sb.append(e.getKey());
            sb.append("->");
            sb.append(e.getValue());
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    
    /**
     * String representation
     * @return The string representation
     */
    @Override
    public String toString() {
        return toString(0);
    }
}
