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

import es.rickyepoderi.wbxml.definition.WbXmlAttributeDef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * <p>Java representation for an attribute in the WBXML specification. An 
 * attribute is defined in the following way: </p> * 
 * 
 * <pre>
 * attribute = attrStart *attrValue
 * attrStart = ([switchPage] ATTRSTART) | ( LITERAL index )
 * 
 * attrValue = ([switchPage] ATTRVALUE) | string | extension | entity | opaque
 * 
 * string = inline | tableref
 * inline = STR_I termstr
 * tableref = STR_T index
 * index= mb_u_int32   // index in the attr table
 * 
 * extension = [switchPage] (( EXT_I termstr ) | ( EXT_T index ) | EXT)
 * 
 * entity = ENTITY entcode
 * entcode = mb_u_int32  // UCS-4 character code
 * 
 * opaque = OPAQUE length *byte
 * </pre>
 * 
 * <p>So the attribute is the name of the attribute (then when encoded it
 * could be used as LITERAL or not, depending if it is defined in the specification),
 * and a list of values. Those can be an attribute value, a string (STR_I or STR_T), 
 * a extension (part of the value corresponds with an extension), and entity or 
 * a opaque data. In general the in memory representation is just the name
 * and a list of string for the values. The value can be compacted or not,
 * compacting a value is using calling the definition method that searches
 * for attributes values and extensions to reduce the length of the string value.</p>
 * 
 * @author ricky
 */
public class WbXmlAttribute {
    
    /**
     * Logger for the class
     */
    protected static final Logger log = Logger.getLogger(WbXmlAttribute.class.getName());
    
    /**
     * The name of the attribute. This name corresponds to the one used in
     * the definition (should be prefixed if the language uses namespaces).
     */
    private String name = null;
    
    /**
     * List of values. Any value can match a extension or an attribute value.
     * An entity is just something like this "&20;"
     */
    private List<String> values = null;
    
    /**
     * boolean property that marks if the attribute has been compacted or not.
     */
    private boolean compacted = false;
    
    /**
     * Empty constructor.
     */
    protected WbXmlAttribute() {
        this(null, (String[]) null);
    }
    
    /**
     * Constructor via name.
     * @param name The name of the attribute (prefixed!)
     */
    public WbXmlAttribute(String name) {
        this(name, (String[]) null);
    }
    
    /**
     * Constructor via name and one attribute value.
     * @param name The name of the attribute (prefixed!)
     * @param value The value (only one in the list)
     */
    public WbXmlAttribute(String name, String value) {
        this(name, new String[]{value});
    }
    
    /**
     * Constructor using the name and several values.
     * @param name The name of the attribute (prefixed!)
     * @param values The array with the values
     */
    public WbXmlAttribute(String name, String[] values) {
        this.name = name;
        this.values = new ArrayList<String>();
        if (values != null) {
            this.values.addAll(Arrays.asList(values));
        }
    }

    /**
     * getter for the name.
     * @return The name of the attribute
     */
    public String getName() {
        return name;
    }

    /**
     * It returns true if the attribute is prefixed (used the ":" character).
     * @return true if it is prefixed, false otherwise
     */
    public boolean isPrefixed() {
        return name.indexOf(':') >= 0;
    }
    
    /**
     * Method that returns the prefix of the attribute or null if not prefixed.
     * @return The prefix used or null
     */
    public String getNamePrefix() {
        int idx = name.indexOf(':');
        if (idx >= 0) {
            return name.substring(0, idx);
        } else {
            return null;
        }
    }
    
    /**
     * Method that return the name without the prefix if prefixed, if not
     * prefixed returns the normal name.
     * @return The name without the prefix
     */
    public String getNameWithoutPrefix() {
        int idx = name.indexOf(':');
        if (idx >= 0) {
            return name.substring(idx + 1);
        } else {
            return name;
        }
    }
    
    /**
     * Setter for the name.
     * @param name The new name
     */
    protected void setName(String name) {
        this.name = name;
    }
    
    /**
     * Method that add a new value to the list of values.
     * @param value The value to add
     * @return The same attribute object to chain calls
     */
    public WbXmlAttribute addValue(String value) {
        this.values.add(value);
        return this;
    }
   
    /**
     * Method that adds several values at the same time.
     * @param values The list of values to add
     * @return The same attribute object to chain calls
     */
    public WbXmlAttribute addValues(List<String> values) {
        this.values.addAll(values);
        return this;
    }
    
    /**
     * Method that joins all the values in a unique string. Simple concatenating
     * is used.
     * @return The concatenated value
     */
    public String getValue() {
        if (values.isEmpty()) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String v: values) {
                sb.append(v);
            }
            return sb.toString();
        }
    }
    
    /**
     * method that returns the value in the position specified.
     * @param i The position to get the value from
     * @return The value in the specified position
     */
    public String getValue(int i) {
        return values.get(i);
    }
    
    /**
     * It checks if the list of values is empty.
     * @return true if empty false otherwise
     */
    public boolean isValuesEmpty() {
        return values.isEmpty();
    }
    
    /**
     * Method that returns the list of values of the attribute.
     * @return The list of values of the attribute
     */
    public List<String> getValues() {
        return values;
    }
    
    /**
     * Returns if the attribute has been compacted or not.
     * @return true if compacted, false otherwise
     */
    public boolean isCompacted() {
        return compacted;
    }

    /**
     * Method to compact the attribute values. This method first checks
     * attribute values specified in the language definition and then performs
     * the same with extensions. Methods of the definition are used to compact
     * the string values. After the call the attribute is compacted.
     * @param encoder The encoder used in the encoding process
     * @param def The Attribute definition of this attribute
     */
    public void compact(WbXmlEncoder encoder, WbXmlAttributeDef def) {
        if (def == null || encoder.getDefinition().locateAttrPlugin(def.getNameWithPrefix()) == null) {
            int start = 0;
            if (def != null && def.getValue() != null) {
                start = def.getValue().length() - 1;
            }
            // compact using the attribute values
            boolean first = true;
            List<String> newValues = new ArrayList<String>();
            for (String v : values) {
                if (first) {
                    newValues.addAll(encoder.getDefinition().compactAttributeValue(v, start));
                    first = false;
                } else {
                    newValues.addAll(encoder.getDefinition().compactAttributeValue(v));
                }
            }
            // compact using the extensions
            values.clear();
            first = true;
            for (String v : newValues) {
                if (first) {
                    values.addAll(encoder.getDefinition().compactExtension(v, start));
                    first = false;
                } else {
                    values.addAll(encoder.getDefinition().compactExtension(v));
                }
            }
        }
        compacted = true;
    }
    
    /**
     * This method convert the list of values again into a only one value. 
     * After that the attribute is not compacted.
     * TODO: what about entities, now are just appended "abcd...&20;...cc"
     */
    public void normalize() {
        StringBuilder sb = new StringBuilder();
        for (String v: values) {
            sb.append(v);
        }
        values.clear();
        values.add(sb.toString());
        compacted = false;
    }
    
    /**
     * String representation of the attribute.
     * @param ident The ident to better understanding
     * @return The string representation
     */
    public String toString(int ident) {
        String spaces = WbXmlLiterals.identString(ident);
        StringBuilder sb = new StringBuilder(spaces);
        sb.append(this.getClass().getSimpleName());
        sb.append(": ");
        sb.append(name);
        sb.append("=");
        for (String v: values) {
            sb.append('"');
            sb.append(v);
            sb.append('"');
            sb.append("|");
        }
        String res = sb.toString();
        return res.substring(0, res.length() - 1);
    }
    
    /**
     * String representation of the attribute.
     * @return The string representation
     */
    @Override
    public String toString() {
        return toString(0);
    }
}
