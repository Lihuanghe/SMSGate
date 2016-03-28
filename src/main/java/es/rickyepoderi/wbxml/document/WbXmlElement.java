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
import java.util.logging.Logger;

/**
 * 
 * <p>Java representation of an WBXML element. The element in the specification
 * is defined like this:</p>
 * 
 * <pre>
 * element = ([switchPage] stag) [ 1*attribute END ] [ *content END ]
 * 
 * stag = TAG | (literalTag index)
 * 
 * literalTag = LITERAL | LITERAL_A | LITERAL_C | LITERAL_AC
 * </pre>
 * 
 * <p>So it is a tag with attributes and contents.</p>
 * 
 * @author ricky
 */
public class WbXmlElement {
    
    /**
     * Logger for the class
     */
    protected static final Logger log = Logger.getLogger(WbXmlElement.class.getName());
    
    /**
     * Tag of the element (prefixed!).
     */
    private String tag = null;
    
    /**
     * List of attributes of the tag.
     */
    private List<WbXmlAttribute> attributes = null;
    
    /**
     * List of contents.
     */
    private List<WbXmlContent> contents = null;
    
    /**
     * Property that marks if the element is compacted or not.
     */
    private boolean compacted = false;
    
    /**
     * Empty constructor.
     */
    protected WbXmlElement() {
        this(null, (WbXmlAttribute[]) null, (WbXmlContent[]) null);
    }
    
    /**
     * Constructor via the tag name. The tag should be prefixed if the
     * definition uses namespaces.
     * @param tag The tag of the element 
     */
    public WbXmlElement(String tag) {
        this(tag, (WbXmlAttribute[]) null, (WbXmlContent[]) null);
    }
    
    /**
     * Constructor using tag, one attribute and one content
     * @param tag The tag
     * @param attribute The attribute
     * @param content The content
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute, WbXmlContent content) {
        this(tag, new WbXmlAttribute[] {attribute}, new WbXmlContent[] {content});
    }
    
    /**
     * Constructor using tag, one attribute and one element as content
     * @param tag The tag
     * @param attribute The attribute
     * @param element The element content
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute, WbXmlElement element) {
        this(tag, new WbXmlAttribute[] {attribute}, new WbXmlContent[] {new WbXmlContent(element)});
    }
    
    /**
     * Constructor using tag, one attribute and one string as content
     * @param tag The tag
     * @param attribute The attribute
     * @param string The string content
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute, String string) {
        this(tag, new WbXmlAttribute[] {attribute}, new WbXmlContent[] {new WbXmlContent(string)});
    }
    
    /**
     * Constructor using tag and one attribute
     * @param tag The tag
     * @param attribute The attribute
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute) {
        this(tag, new WbXmlAttribute[] {attribute}, (WbXmlContent[]) null);
    }
    
    /**
     * Constructor using tag and several attributes
     * @param tag The tag
     * @param attributes Some attributes
     */
    public WbXmlElement(String tag, WbXmlAttribute[] attributes) {
        this(tag, attributes, (WbXmlContent[]) null);
    }
    
    /**
     * Constructor using tag, several attributes and one content
     * @param tag The tag
     * @param attributes Several attributes
     * @param content The content
     */
    public WbXmlElement(String tag, WbXmlAttribute[] attributes, WbXmlContent content) {
        this(tag, attributes, new WbXmlContent[] {content});
    }
    
    /**
     * Constructor using tag, several attribute and one element content
     * @param tag The tag
     * @param attributes Several attributes
     * @param element The element content
     */
    public WbXmlElement(String tag, WbXmlAttribute[] attributes, WbXmlElement element) {
        this(tag, attributes, new WbXmlContent[] {new WbXmlContent(element)});
    }
    
    /**
     * Constructor using tag, several attributes and one string content
     * @param tag The tag
     * @param attributes Several attributes
     * @param string The string content
     */
    public WbXmlElement(String tag, WbXmlAttribute[] attributes, String string) {
        this(tag, attributes, new WbXmlContent[] {new WbXmlContent(string)});
    }
    
    /**
     * Constructor using tag and one content
     * @param tag The tag
     * @param content The content
     */
    public WbXmlElement(String tag, WbXmlContent content) {
        this(tag, (WbXmlAttribute[]) null, new WbXmlContent[] {content});
    }
    
    /**
     * Constructor using tag and one element content
     * @param tag The tag
     * @param element The element
     */
    public WbXmlElement(String tag, WbXmlElement element) {
        this(tag, (WbXmlAttribute[]) null, new WbXmlContent[] {new WbXmlContent(element)});
    }
    
    /**
     * Constructor using tag and one string content
     * @param tag The tag
     * @param string The string content
     */ 
    public WbXmlElement(String tag, String string) {
        this(tag, (WbXmlAttribute[]) null, new WbXmlContent[] {new WbXmlContent(string)});
    }
    
    /**
     * Constructor using tag, one attribute and several contents
     * @param tag The tag
     * @param attribute The attribute
     * @param contents Several contents
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute, WbXmlContent[] contents) {
        this(tag, new WbXmlAttribute[] {attribute}, contents);
    }
    
    /**
     * Private method to transform an array of elements in an array of contents.
     * @param elements The array of elements
     * @return The array of contents created for each element
     */
    static private WbXmlContent[] createContentsArrayFromElement(WbXmlElement[] elements) {
        if (elements == null) {
            return null;
        }
        WbXmlContent[] contents = new WbXmlContent[elements.length];
        for (int i = 0; i < elements.length; i++) {
            contents[i] = new WbXmlContent(elements[i]);
        }
        return contents;
    }
    
    /**
     * Constructor using tag, one attribute and several element contents
     * @param tag The tag
     * @param attribute The attribute
     * @param elements Several elements to be used as contents
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute, WbXmlElement[] elements) {
        this(tag, new WbXmlAttribute[] {attribute}, createContentsArrayFromElement(elements));
    }
    
    /**
     * Private method to transform an array of Strings into an array of contents
     * @param strings The array of strings
     * @return The array of contents created with each string
     */
    static private WbXmlContent[] createContentsArrayFromString(String[] strings) {
        if (strings == null) {
            return null;
        }
        WbXmlContent[] contents = new WbXmlContent[strings.length];
        for (int i = 0; i < strings.length; i++) {
            contents[i] = new WbXmlContent(strings[i]);
        }
        return contents;
    }
    
    /**
     * Constructor using the tag, one attribute and several string contents
     * @param tag The tag
     * @param attribute The attribute
     * @param strings Several string contents
     */
    public WbXmlElement(String tag, WbXmlAttribute attribute, String[] strings) {
        this(tag, new WbXmlAttribute[] {attribute}, createContentsArrayFromString(strings));
    }
    
    /**
     * Constructor using the tag and several contents
     * @param tag The tag
     * @param contents Several contents
     */
    public WbXmlElement(String tag, WbXmlContent[] contents) {
        this(tag, (WbXmlAttribute[]) null, contents);
    }
    
    /**
     * Constructor using the tag and several element contents
     * @param tag The tag
     * @param elements Several element contents
     */
    public WbXmlElement(String tag, WbXmlElement[] elements) {
        this(tag, (WbXmlAttribute[]) null, createContentsArrayFromElement(elements));
    }
    
    /**
     * Constructor using the tag and several string contents
     * @param tag The tag
     * @param strings Several string contents
     */
    public WbXmlElement(String tag, String[] strings) {
        this(tag, (WbXmlAttribute[]) null, createContentsArrayFromString(strings));
    }
    
    /**
     * Constructor using the tag, several attributes and several contents
     * @param tag The tag
     * @param attributes Several attributes
     * @param contents Several contents
     */
    public WbXmlElement(String tag, WbXmlAttribute[] attributes, WbXmlContent[] contents) {
        this.compacted = false;
        this.tag = tag;
        this.attributes = new ArrayList<WbXmlAttribute>();
        if (attributes != null) {
            this.attributes.addAll(Arrays.asList(attributes));
        }
        this.contents = new ArrayList<WbXmlContent>();
        if (contents != null) {
            this.contents.addAll(Arrays.asList(contents));
        }
    }
    
    /**
     * Method that checks if the element has been compacted.
     * @return true if compacted, false otherwise
     */
    public boolean isCompacted() {
        return compacted;
    }
    
    /**
     * Getter for the tag (prefixed if using namespaces).
     * @return The tag
     */
    public String getTag() {
        return tag;
    }
    
    /**
     * It checks if the tag is prefixed (contains the character ':').
     * @return true if contains ':', false otherwise
     */
    public boolean isPrefixed() {
        return tag.indexOf(':') >= 0;
    }
    
    /**
     * It returns the tag prefix if it is prefixed, null if not.
     * @return The tag prefix if prefixed, null if not
     */
    public String getTagPrefix() {
        int idx = tag.indexOf(':');
        if (idx >= 0) {
            return tag.substring(0, idx);
        } else {
            return null;
        }
    }
    
    /**
     * Method that returns the tag without the prefix (if prefixed).
     * @return The tag name with no prefix
     */
    public String getTagWithoutPrefix() {
        int idx = tag.indexOf(':');
        if (idx >= 0) {
            return tag.substring(idx + 1);
        } else {
            return tag;
        }
    }    
    
    /**
     * Setter for the tag
     * @param tag The new tag (prefixed!)
     * @return The same element to chain calls
     */
    public WbXmlElement setTag(String tag) {
        this.tag = tag;
        return this;
    }
    
    /**
     * Add a new attribute to the list of attributes.
     * @param attribute The new attribute to add
     * @return The same element to chain calls
     */
    public WbXmlElement addAttribute(WbXmlAttribute attribute) {
        this.attributes.add(attribute);
        return this;
    }
    
    /**
     * It checks if the attribute list is empty.
     * @return true if empty, false if not
     */
    public boolean isAttributesEmpty() {
        return attributes.isEmpty();
    }
    
    /**
     * Method that return the size of the attribute list-
     * @return The number of attributes in the list
     */
    public int attributesSize() {
        return attributes.size();
    }
    
    /**
     * Getter for the attribute.
     * @param idx The index of the attribute to return
     * @return The attribute in the idx position
     */
    public WbXmlAttribute getAttribute(int idx) {
        return attributes.get(idx);
    }
    
    /**
     * Getter for an attribute but using the name.
     * @param name The name of the attribute to get
     * @return The attribute with that name or null
     */
    public WbXmlAttribute getAttribute(String name) {
        for (WbXmlAttribute attr: attributes) {
            if (name.equals(attr.getName())) {
                return attr;
            }
        }
        return null;
    }
    
    /**
     * Getter for the complete list of attributes
     * @return The list of attributes of this element
     */
    public List<WbXmlAttribute> getAttributes() {
        return attributes;
    }
    
    /**
     * It adds a new content to the list of contents
     * @param content The content to add
     * @return The same element to chain calls
     */
    public WbXmlElement addContent(WbXmlContent content) {
        this.contents.add(content);
        return this;
    }
    
    /**
     * It checks if the list of contents is empty.
     * @return true if empty, false otherwise
     */
    public boolean isContentsEmpty() {
        return contents.isEmpty();
    }
    
    /**
     * Method that returns the size of the contents list.
     * @return The number of contents in the element
     */
    public int contentsSize() {
        return contents.size();
    }
    
    /**
     * Getter for the content in the idx position
     * @param idx The position to get the content from
     * @return The content in the idx position
     */
    public WbXmlContent getContent(int idx) {
        return contents.get(idx);
    }
    
    /**
     * Getter for the complete list of contents.
     * @return The list of contents
     */
    public List<WbXmlContent> getContents() {
        return contents;
    }

    /**
     * Method that compacts the contents of the element. As you know WBXML 
     * has extensions to reduce the length of string contents. This way this
     * method searches for possible extensions. The definition methods
     * for compacting are used. After this method the element is compacted.
     * @param encoder The encoder object used in the encoding process
     */
    public void compact(WbXmlEncoder encoder) {
        List<WbXmlContent> newContents = new ArrayList<WbXmlContent>();
        // compact ant content which is a string using extensions
        for (WbXmlContent content : contents) {
            if (content.isString() && encoder.getDefinition().locateTagPlugin(tag) == null) {
                List<String> strings = encoder.getDefinition().compactExtension(content.getString());
                for (String string : strings) {
                    newContents.add(new WbXmlContent(string));
                }
            } else {
                newContents.add(content);
            }
        }
        contents = newContents;
        compacted = true;
    }
    
    /**
     * Method that joins all consecutive string contents into one. After
     * that the element is nor compacted.
     */
    public void normalize() {
        List<WbXmlContent> newContents = new ArrayList<WbXmlContent>();
        // compact ant content which is a string using extensions
        StringBuilder sb = new StringBuilder();
        for (WbXmlContent content : contents) {
            if (content.isString() && !content.isEntity()) {
                // add the content to the buffer
                sb.append(content.getString());
            } else {
                // add previous strings if added
                if (sb.length() > 0) {
                    newContents.add(new WbXmlContent(sb.toString()));
                    sb.setLength(0);
                }
                newContents.add(content);
            }
        }
        // add last string if added
        if (sb.length() > 0) {
            newContents.add(new WbXmlContent(sb.toString()));
        }
        contents = newContents;
        compacted = false;
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
        sb.append("tag=");
        sb.append(tag);
        ident++;
        sb.append(System.getProperty("line.separator"));
        for (WbXmlAttribute a: attributes) {
            sb.append(a.toString(ident));
            sb.append(System.getProperty("line.separator"));
        }
        for (WbXmlContent c: contents) {
            sb.append(c.toString(ident));
        }
        return sb.toString();
    }
    
    /**
     * String representation
     * @return The string reoresentation
     */
    @Override
    public String toString() {
        return toString(0);
    }
}
