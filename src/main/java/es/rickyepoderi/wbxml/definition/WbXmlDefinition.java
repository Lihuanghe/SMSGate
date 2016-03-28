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

import es.rickyepoderi.wbxml.document.OpaqueAttributePlugin;
import es.rickyepoderi.wbxml.document.OpaqueContentPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * <p>WBXML is a format that assigns several identifiers to different
 * elements of an XML (tags, attributes, values,...). This way when you try
 * to parse/encode any WBXML document you need to know the language you
 * are using. That language specification gives you those identifiers. 
 * It is impossible to parse/encode a unknown language specification. This
 * class represents a Java representation of any WBXML language definition.
 * This definition is based on a properties file which needs to be 
 * provided</p>
 * 
 * <p>Basically a language definition is the following list of information
 * (some are optional fields):</p>
 * 
 * <ul>
 * <li>name: A simple name to classify the language (for example SyncML 1.1).</li>
 * <li>publicId: WBXML format defines that each language should have a identifier
 * (numeric). 0x01 represents the unknown language (it does not have a standard 
 * id).</li>
 * <li>Formal public Id: The formal name of the XML identifier, any XML definition
 * has a FPI (for example SyncML version 1.1 is <em>-//SYNCML//DTD SyncML 1.1//EN</em>).
 * This FPI is used in any DOCTYPE.</li>
 * <li>XML URI definition: The URL where the XMl definition for the language 
 * can be obtained (again it appears in any DOCTYPE).</li>
 * <li>Root element: The root element for the language (in SyncML "SyncML" tag).</li>
 * <li>JAXB class: This optional field should be passed if the definition
 * has JAXB classes created for the language (using xjc for example).</li>
 * <li>Namespaces: The list of namespaces the language manages (a namespace
 * is the uri and the prefix).</li>
 * <li>Tags: List of tags definition for the language (with the respective token
 * identifiers).</li>
 * <li>Attributes: List of attribute definition tokens.</li>
 * <li>Attribute values: List os attribute values definition tokens.</li>
 * <li>Extensions: List of extensions defined for this language.</li>
 * <li>Opaque plugins: List of opaque plugins used in opaque data representations.</li>
 * <li>Linked definitions: This is a non standard feature. Some definitions mix
 * some languages using opaques. This way definitions can have another linked 
 * ones. The parsing/encoding methods use all of them.</li>
 * </ul>
 * 
 * <p>The properties file is parsed and an object of this kind is created
 * in order to parse or encode a WBXML document of this language. If the 
 * language is not specified no WBXML document can be parsed or encoded.</p>
 * 
 * @author ricky
 * @see WbXmlTagDef
 * @see WbXmlAttributeDef
 * @see WbXmlAttributeValueDef
 * @see WbXmlExtensionDef
 * @see OpaqueContentPlugin
 */
public class WbXmlDefinition {
    
    /**
     * The public ID for unknown type
     */
    static public final long PUBLIC_ID_UNKNOWN = 0x01;
    
    /**
     * The public ID is defined in the STR_T table
     */
    static public final long PUBLIC_ID_STR_T = 0x00;
    
    /**
     * The representative name of the definition language.
     */
    private String name = null;
    
    /**
     * WBXML public identifier assigned to this language.
     */
    private long publicId = 0L;
    
    /**
     * XML Formal Public ID.
     */
    private String xmlPublicId = null;
    
    /**
     * URI used to download the DTD of the document.
     */
    private String xmlUriRef = null;
    
    /**
     * Class used for JAXB if exists.
     */
    private String clazz = null;
    
    /**
     * The tag used for root components.
     */
    private WbXmlTagDef root = null;
    
    /**
     * Namespaces keyed by namespace uri.
     */
    Map<String,WbXmlNamespaceDef> nsByNamespace = null;
    
    /**
     * Namespaces keyed by prefix.
     */
    Map<String,WbXmlNamespaceDef> nsByPrefix = null;
    
    /**
     * Tags keyed by the token (token byte and page code).
     */
    private Map<WbXmlToken, WbXmlTagDef> tagsByToken = null;
    
    /**
     * Tags keyed by the prefixed name.
     */
    private Map<String, WbXmlTagDef> tagsByName = null;
    
    /**
     * Attributes keyed by the token.
     */
    private Map<WbXmlToken, WbXmlAttributeDef> attrsByToken = null;
    
    /**
     * Attributes keyed by the prefixed name. Cos an attribute can be
     * represented by several tokens (it depends on the value) a set is
     * used.
     */
    private Map<String, TreeSet<WbXmlAttributeDef>> attrsByName = null;
    
    private Map<WbXmlToken, WbXmlAttributeValueDef> attrValuesByToken = null;
    private TreeSet<WbXmlAttributeValueDef> attrValuesByValue = null;
    
    /**
     * Attribute values keyed by the token.
     */
    private Map<Byte, WbXmlExtensionDef> extsByToken = null;
    
    /**
     * Attributes keyed by the value.
     */
    private TreeSet<WbXmlExtensionDef> extsByValue = null;
    
    /**
     * Opaque plugins for attributes keyed by the token.
     */
    private Map<WbXmlToken, OpaqueAttributePlugin> opaqueAttrByToken = null;
    
    /**
     * Opaque plugins for attributes keyed by the prefixed name.
     */
    private Map<String, OpaqueAttributePlugin> opaqueAttrByName = null;
    
    /**
     * Opaque plugins for tags keyed by the token.
     */
    private Map<WbXmlToken, OpaqueContentPlugin> opaqueTagByToken = null;
    
    /**
     * Opaque plugins for tags keyed by the prefixed name.
     */
    private Map<String, OpaqueContentPlugin> opaqueTagByName = null;
    
    /**
     * Linked definitions for this one.
     */
    private Map<String, WbXmlDefinition> linkedDefinitions = null;

    /**
     * Constructor via name.
     * @param name The name of the specification
     */
    protected WbXmlDefinition(String name) {
        this.name = name;
        nsByNamespace = new HashMap<String, WbXmlNamespaceDef>();
        nsByPrefix = new HashMap<String, WbXmlNamespaceDef>();
        tagsByToken = new HashMap<WbXmlToken, WbXmlTagDef>();
        tagsByName = new HashMap<String, WbXmlTagDef>();
        attrsByToken = new HashMap<WbXmlToken, WbXmlAttributeDef>();
        attrsByName = new HashMap<String, TreeSet<WbXmlAttributeDef>>();
        attrValuesByToken = new HashMap<WbXmlToken, WbXmlAttributeValueDef>();
        attrValuesByValue = new TreeSet<WbXmlAttributeValueDef>();
        extsByToken = new HashMap<Byte, WbXmlExtensionDef>();
        extsByValue = new TreeSet<WbXmlExtensionDef>();
        opaqueAttrByName = new HashMap<String, OpaqueAttributePlugin>();
        opaqueAttrByToken = new HashMap<WbXmlToken, OpaqueAttributePlugin>();
        opaqueTagByName = new HashMap<String, OpaqueContentPlugin>();
        opaqueTagByToken = new HashMap<WbXmlToken, OpaqueContentPlugin>();
        linkedDefinitions = new HashMap<String, WbXmlDefinition>();
    }
    
    /**
     * Constructor via name and public WBXML id.
     * @param name The name of the language
     * @param publicId The WBXML standard identifier for the language
     */
    protected WbXmlDefinition(String name, long publicId) {
        this(name);
        this.publicId = publicId;
        
    }
    
    /**
     * Constructor via name, public WBXML id and XML Formal public id.
     * @param name The language name
     * @param publicId The WBXML identifier
     * @param xmlPublicId The XML formal identifier
     */
    protected WbXmlDefinition(String name, long publicId, String xmlPublicId) {
        this(name, publicId);
        this.xmlPublicId = xmlPublicId;
    }
    
    /**
     * Constructor via name, public WBXML id, XML fornal id, teh DTD uri and root JAXB class.
     * @param name The name of the language.
     * @param publicId The WBXML identifier 
     * @param xmlPublicId The XML formal identifier
     * @param xmlUriRef DTD uri for the xml
     * @param clazz The JAXB class that encapsulates the root element
     */
    protected WbXmlDefinition(String name, long publicId, String xmlPublicId, String xmlUriRef, String clazz) {
        this(name, publicId, xmlPublicId);
        this.xmlUriRef = xmlUriRef;
        this.clazz = clazz;
    }
    
    /**
     * Getter for the name
     * @return The name of the language
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
     * Getter for the WBXML public identifier
     * @return The identifier
     */
    public long getPublicId() {
        return publicId;
    }

    /**
     * Setter for the WBXML public identifier
     * @param publicId The new identifier
     */
    protected void setPublicId(long publicId) {
        this.publicId = publicId;
    }

    /**
     * Getter for the XML formal public identifier
     * @return The XML identifier
     */
    public String getXmlPublicId() {
        return xmlPublicId;
    }

    /**
     * Setter for the XML formal public identifier
     * @param xmlPublicId Thw new identifier
     */
    protected void setXmlPublicId(String xmlPublicId) {
        this.xmlPublicId = xmlPublicId;
    }

    /**
     * Getter for the DTD uri
     * @return The DTD uri
     */
    public String getXmlUriRef() {
        return xmlUriRef;
    }

    /**
     * Setter for the DTD uri
     * @param xmlUriRef The new uri
     */
    protected void setXmlUriRef(String xmlUriRef) {
        this.xmlUriRef = xmlUriRef;
    }

    /**
     * Setter for the root Tag, tag that represents the root element for the language.
     * @param root The root tag
     */
    protected void setRoot(WbXmlTagDef root) {
        this.root = root;
    }
    
    /**
     * Setter using string instead of the complete tag definition.
     * @param root The new root
     */
    protected void setRoot(String root) {
        this.root = this.tagsByName.get(root);
    }

    /**
     * getter for the root element of the language
     * @return The root tag
     */
    public WbXmlTagDef getRoot() {
        return root;
    }
    
    /**
     * Getter of the root element but returning the name.
     * @return The root name
     */
    public String getRootName() {
        return (root == null)? null : root.getNameWithPrefix();
    }

    /**
     * Gettter for the class of the root element.
     * @return The JAXB class
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Setter for the class.
     * @param clazz The new class
     */
    protected void setClazz(String clazz) {
        this.clazz = clazz;
    }
    
    /**
     * Get the linked definitions for this definition.
     * @return The linked map of definitions (indexed by the name)
     */
    protected Map<String, WbXmlDefinition> getLinkedDefinitions() {
        return this.linkedDefinitions;
    }
    
    /**
     * Return the list of linked definitions of this one.
     * @return The list of linked definitions
     */
    public Collection<WbXmlDefinition> getLinkedDefs() {
        return this.linkedDefinitions.values();
    }
    
    /**
     * It returns a linked definition for this one.
     * @param name The name of the linked def to get
     * @return the definition or null
     */
    public WbXmlDefinition getLinkedDef(String name) {
        return this.linkedDefinitions.get(name);
    }
    
    /**
     * Add a new namespace to the list of namespaces managed by this
     * language definition. The methos adds it to both maps.
     * @param ns The new namespace to add
     */
    protected void addNamespace(WbXmlNamespaceDef ns) {
        nsByNamespace.put(ns.getNamespace(), ns);
        nsByPrefix.put(ns.getPrefix(), ns);
    }
    
    /**
     * Add a new extension to the language definition. The method adds it
     * to both maps.
     * @param ext The new extension.
     */
    protected void addExtension(WbXmlExtensionDef ext) {
        this.extsByValue.add(ext);
        this.extsByToken.put(ext.getToken(), ext);
    }
    
    /**
     * Add a new opaque plugin for attributes for this language definition.
     * The method adds it to both maps.
     * @param attr The attribute with a plugin associated
     * @param plugin The plugin
     */
    protected void addOpaqueAttr(WbXmlAttributeDef attr, OpaqueAttributePlugin plugin) {
        this.opaqueAttrByName.put(attr.getNameWithPrefix(), plugin);
        this.opaqueAttrByToken.put(attr.getToken(), plugin);
    }
    
    /**
     * Add a new opaque plugin for tag. The methos adds it to both maps.
     * @param tag The tag with a plugin associated
     * @param plugin The plugin
     */
    protected void addOpaqueTag(WbXmlTagDef tag, OpaqueContentPlugin plugin) {
        this.opaqueTagByName.put(tag.getNameWithPrefix(), plugin);
        this.opaqueTagByToken.put(tag.getToken(), plugin);
    }
    
    /**
     * Add a nerw Attribute definition to this language definition. The method
     * adds it to both maps.
     * @param attr The attribute to add
     */
    protected void addAttr(WbXmlAttributeDef attr) {
        TreeSet set = this.attrsByName.get(attr.getNameWithPrefix());
        if (set == null) {
            set = new TreeSet<WbXmlAttributeDef>();
            this.attrsByName.put(attr.getNameWithPrefix(), set);
        }
        set.add(attr);
        this.attrsByToken.put(attr.getToken(), attr);
    }
    
    /**
     * Add a new attribute value for this language definition. The method
     * adds it to both maps.
     * @param attrVal The attribute value to add
     */
    protected void addAttrValue(WbXmlAttributeValueDef attrVal) {
        this.attrValuesByValue.add(attrVal);
        this.attrValuesByToken.put(attrVal.getToken(), attrVal);
    }
    
    /**
     * Add a new tag for this language definition. The method adds it to both maps.
     * @param tag The new tag to add
     */
    protected void addTag(WbXmlTagDef tag) {
        this.tagsByName.put(tag.getNameWithPrefix(), tag);
        this.tagsByToken.put(tag.getToken(), tag);
    }
    
    /**
     * Method that compacts a attribute value using extensions and attribute
     * values. In WBXML the normal string attribute value can be compacted
     * using extensions or attribute values, this method search over the
     * definition to compact a normal string value into a list of them
     * that matches with a attribute value or a extension.
     * @param value The value to compact
     * @return The list of string resulted after compactation
     */
    public List<String> compactAttributeValue(String value) {
        return compactAttributeValue(value, 0);
    }
    
    /**
     * Private method that check if a extension or attribute value is found
     * in the value passed. If it is contained the string is splitted in three
     * possible parts (the part before the match, the matched string, and the
     * rest). Obviously the returned list is from one to three sized list.
     * If there is no match a null is returned.
     * @param value The value to search the string in
     * @param valueToFound The value (correspond from an extension or attribute value) to look for
     * @param start The start point to search (attribute values can start after 0).
     * @param attr true if attr, false if extension
     * @return The list of splitted strings or null
     */
    private List<String> matches(String value, String valueToFound, int start, boolean attr) {
        int pos = value.indexOf(valueToFound, start);
        if (pos >= 0) {
            List<String> result = new ArrayList<String>();
            if (valueToFound.length() < 3 && (value.length() != valueToFound.length() - start)) {
                // if the extension is very short and it does not
                // substitite the whole string it is descarted
                result.add(value);
                return result;
            }
            // split the string into parts the value => three posible parts
            String firstPart = null;
            if (pos > 0) {
                firstPart = value.substring(0, pos);
            }
            String secondPart = value.substring(pos, pos + valueToFound.length());
            String thirdPart = null;
            //System.err.println(pos + attrVal.getValue().length() + " > " + value.length());
            if (pos + valueToFound.length() < value.length()) {
                thirdPart = value.substring(pos + valueToFound.length());
            }
            if (firstPart != null && !firstPart.isEmpty()) {
                if (attr) {
                    result.addAll(compactAttributeValue(firstPart, start));
                } else {
                    result.addAll(compactExtension(firstPart, start));
                }
            }
            result.add(secondPart);
            if (thirdPart != null && !thirdPart.isEmpty()) {
                if (attr) {
                    result.addAll(compactAttributeValue(thirdPart, 0));
                } else {
                    result.addAll(compactExtension(thirdPart, 0));
                }
            }
            return result;
        } else {
            return null;
        }
    }
    
    /**
     * Recursive methodthe that searches for possible compaction matches of an
     * attribute value against the definition (attribute values or extensions
     * can be used to compact it). This method searches in a specified order, 
     * first attribute values and then extension. In both larger definitions
     * are taken into account first.
     * @param value The attribute value to compact
     * @param start The start (cos an attribute can define a part of a value)
     * @return The list of string the value is compacted
     */
    public List<String> compactAttributeValue(String value, int start) {
        //System.err.println("compactAttributeValue: " + value + " " + start);
        List<String> result = new ArrayList<String>();
        if (start > value.length()) {
            result.add(value);
            return result;
        }
        // search over all the values that are less than the specified value
        WbXmlAttributeValueDef valueDef = new WbXmlAttributeValueDef(value.substring(start), (byte) 0x0, (byte) 0x0);
        for (WbXmlAttributeValueDef attrVal : attrValuesByValue.tailSet(valueDef)) {
            //System.err.println("testing... " + attrVal.getValue());
            List<String> found = matches(value, attrVal.getValue(), start, true);
            if (found != null) {
                return found;
            }
        }
        // no matches => return same value
        result.add(value);
        return result;
    }
    
    /**
     * Similar method of compactAttributeValue but only uses extensions
     * (PCDATA inside a tag). 
     * @param value The value to be compacted
     * @return The list of strings after compactation
     */
    public List<String> compactExtension(String value) {
        return compactExtension(value, 0);
    }
    
    /**
     * Similar method of the recursive version of compactAttributeValue. This
     * method searches over the extensions in order to compact the value
     * in a list of defined extensions.
     * @param value The value to be compacted
     * @param start The index of start
     * @return The string values compacted in several strings
     */
    public List<String> compactExtension(String value, int start) {
        //System.err.println("compactExtension: " + value + " " + start);
        List<String> result = new ArrayList<String>();
        if (start > value.length()) {
            result.add(value);
            return result;
        }
        // search over all the values that are less than the specified value
        WbXmlExtensionDef extDef = new WbXmlExtensionDef(value.substring(start), (byte) 0x0);
        for (WbXmlExtensionDef ext : extsByValue.tailSet(extDef)) {
            //System.err.println("testing... " + ext.getValue());
            List<String> found = matches(value, ext.getValue(), start, false);
            if (found != null) {
                return found;
            }
        }
        // no matches => return same value
        result.add(value);
        return result;
    }
    
    /**
     * Method that locates an attribute in the language definition. WBXML 
     * let define several tokens for the same attribute (different values), 
     * so a set is returned.
     * @param name The prefixed attribute name
     * @return The set of attributes definied for the specified name or null
     */
    public Set<WbXmlAttributeDef> locateAttribute(String name) {
        return this.attrsByName.get(name);
    }
    
    /**
     * Locate the attribute using the attribute name and the value. Cos the 
     * set is a TreeSet the better match is assured (the longer value, the 
     * better).
     * @param name The prefixed attribute name
     * @param value The value of the attribute
     * @return The attribute definition for this name and value
     */
    public WbXmlAttributeDef locateAttribute(String name, String value) {
        TreeSet<WbXmlAttributeDef> attrs = this.attrsByName.get(name);
        if (attrs != null) {
            for (WbXmlAttributeDef attr : attrs) {
                if (name.equals(attr.getNameWithPrefix()) &&
                        (attr.getValue() == null || value.startsWith(attr.getValue()))) {
                    return attr;
                }
            }
        }
        return null;
    }
    
    /**
     * Method that locates an attribute using the token (page code and token byte).
     * @param pageCode The page code
     * @param token The byte token
     * @return The attribute definition or null
     */
    public WbXmlAttributeDef locateAttribute(byte pageCode, byte token) {
        return attrsByToken.get(new WbXmlToken(pageCode, token));
    }
    
    /**
     * Method that locates an attribute value using the value.
     * @param value The value of the attribute value
     * @return The attribute value definition or null
     */
    public WbXmlAttributeValueDef locateAttributeValue(String value) {
        WbXmlAttributeValueDef attrVal = new WbXmlAttributeValueDef(value, (byte) 0x0, (byte) 0x0);
        WbXmlAttributeValueDef res = this.attrValuesByValue.ceiling(attrVal);
        if (res != null && res.getValue().equals(value)) {
            return res;
        } else {
            return null;
        }
    }
    
    /**
     * Method that locates an attribute value using the token.
     * @param pageCode The page code
     * @param token The attribute value token byte
     * @return The attribute value definition or null
     */
    public WbXmlAttributeValueDef locateAttributeValue(byte pageCode, byte token) {
        return attrValuesByToken.get(new WbXmlToken(pageCode, token));
    }
    
    /**
     * Method that locates an extension using the extension code.
     * TODO: The WBXML format defined a mb_u_int32 the extension but libwbxml
     * uses a single byte, maybe any language uses more than 255 extensions.
     * @param ext The extension code
     * @return The extension definition or null
     */
    public WbXmlExtensionDef locateExtension(long ext) {
        // TODO: extensions are a long => why a byte in the definition => CHANGE!!!
        return extsByToken.get((byte) ext);
    }
    
    /**
     * Method that locates an extension in the language definition using its
     * value.
     * @param value The extension value.
     * @return The extension definition or null
     */
    public WbXmlExtensionDef locateExtension(String value) {
        WbXmlExtensionDef ext = new WbXmlExtensionDef(value, (byte) 0x0);
        WbXmlExtensionDef res = this.extsByValue.ceiling(ext);
        if (res != null && res.getValue().equals(value)) {
            return res;
        } else {
            return null;
        }
    }
    
    /**
     * Method that locates a tag using the name.
     * @param name The prefixed name of the tag
     * @return The tag definition or null
     */
    public WbXmlTagDef locateTag(String name) {
        return this.tagsByName.get(name);
    }
    
    /**
     * Method that searches over the linked definitions in order the
     * definition that handles a specified tag. It does not return current
     * definition.
     * @param name The prefixed name of the tag
     * @return The linked definition or null
     */
    public WbXmlDefinition locateLinkedDefinitionForTag(String name) {
        for (WbXmlDefinition def: linkedDefinitions.values()) {
            if (def.locateTag(name) != null) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Method that locates a tag using the token (page code and tag token).
     * @param pageCode The page code
     * @param tag The tag token
     * @return The tag definiotn or null
     */
    public WbXmlTagDef locateTag(byte pageCode, byte tag) {
        return this.tagsByToken.get(new WbXmlToken(pageCode, tag));
    }
    
    /**
     * Method that locates a opaque plugin for an attribute.
     * @param name The prefixed name of the attribute
     * @return the plugin or null
     */
    public OpaqueAttributePlugin locateAttrPlugin(String name) {
        return this.opaqueAttrByName.get(name);
    }
    
    /**
     * Method that locates an opaque tag plugin using the tag name.
     * @param tag The prefixed tag name.
     * @return The plugin or null if not defined
     */
    public OpaqueContentPlugin locateTagPlugin(String tag) {
        return this.opaqueTagByName.get(tag);
    }
    
    /**
     * return the list of namespaces defined in the definition.
     * @return The list of namespaces defined in the language definition.
     */
    public Collection<WbXmlNamespaceDef> getNamespaces() {
        return this.nsByNamespace.values();
    }
    
    /**
     * Method that obtains the prefix of a namespace uri. Take in mind that
     * the definition needs prefixed names if the language uses namespaces.
     * @param namespaceURI The namespace uri
     * @return The prefix for this namespace uri or null if not exists
     */
    public String getPrefix(String namespaceURI) {
        WbXmlNamespaceDef ns = this.nsByNamespace.get(namespaceURI);
        if (ns != null) {
            return ns.getPrefix();
        } else {
            return null;
        }
    }
    
    /**
     * Utility method to obtain a prefix from all the possible definitions
     * in this definition. The method used all the definitions (the main one
     * and the linked).
     * @param namespaceURI The namespaceURI to locate a prefix from
     * @return The prefix or null
     */
    public String getPrefixWithLinked(String namespaceURI) {
        String prefix = getPrefix(namespaceURI);
        if (prefix != null) {
            return prefix;
        }
        for (WbXmlDefinition def : linkedDefinitions.values()) {
            prefix = def.getPrefix(namespaceURI);
            if (prefix != null) {
                return prefix;
            }
        }
        return null;
    }
    
    /**
     * method that obtains the namespace uri of a prefix.
     * @param prefix The prefix
     * @return The namespace uri or null
     */
    public String getNamespaceURI(String prefix) {
        WbXmlNamespaceDef ns = this.nsByPrefix.get(prefix);
        if (ns != null) {
            return ns.getNamespace();
        } else {
            return null;
        }
    }
    
    /**
     * Utility method to obtain a namespaceURI from all the possible definitions
     * used in this definition. The method uses all the language definitions
     * includying the linked ones.
     * @param prefix The prefix of the namespaceURI to search
     * @return The namespaceURI or null
     */
    public String getNamespaceURIWithLinked(String prefix) {
        String namespaceURI = getNamespaceURI(prefix);
        if (namespaceURI != null) {
            return namespaceURI;
        }
        for (WbXmlDefinition def : linkedDefinitions.values()) {
            namespaceURI = def.getNamespaceURI(prefix);
            if (namespaceURI != null) {
                return namespaceURI;
            }
        }
        return null;
    }
    
    /**
     * Method that return the namespace definition for a specified prefix.
     * @param prefix The prefix
     * @return The namespace definition object
     */
    public WbXmlNamespaceDef getNamespace(String prefix) {
        return this.nsByPrefix.get(prefix);
    }
    
    /**
     * String representation of a language definition.
     * @return The string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append(System.getProperty("line.separator"));
        sb.append("name: ");
        sb.append(name);
        sb.append(System.getProperty("line.separator"));
        sb.append("publicId: ");
        sb.append(publicId);
        sb.append(System.getProperty("line.separator"));
        sb.append("xmlPublicId: ");
        sb.append(xmlPublicId);
        sb.append(System.getProperty("line.separator"));
        sb.append("xmlUriRef: ");
        sb.append(xmlUriRef);
        sb.append(System.getProperty("line.separator"));
        sb.append("clazz: ");
        sb.append(clazz);
        sb.append(System.getProperty("line.separator"));
        sb.append("root: ");
        sb.append(root);
        sb.append("namespaces: ");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlNamespaceDef ns: nsByPrefix.values()) {
            sb.append(ns.getPrefix());
            sb.append(" -> ");
            sb.append(ns);
        }
        sb.append("tags:");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlTagDef tag: tagsByName.values()) {
            sb.append(tag);
        }
        sb.append("attributes: ");
        sb.append(System.getProperty("line.separator"));
        for (TreeSet<WbXmlAttributeDef> attrs: attrsByName.values()) {
            for (WbXmlAttributeDef attr: attrs) {
                sb.append(attr);
            }
        }
        sb.append("values:");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlAttributeValueDef attrVal: attrValuesByValue) {
            sb.append(attrVal);
        }
        sb.append("extensions: ");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlExtensionDef ext: extsByValue) {
            sb.append(ext);
        }
        sb.append("opaque attr plugins: ");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlToken token: opaqueAttrByToken.keySet()) {
            sb.append(token);
            sb.append("->");
            sb.append(opaqueAttrByToken.get(token).getClass().getName());
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("opaque tag plugins: ");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlToken token: opaqueTagByToken.keySet()) {
            sb.append(token);
            sb.append("->");
            sb.append(opaqueTagByToken.get(token).getClass().getName());
            sb.append(System.getProperty("line.separator"));
        }
        sb.append("Linked definitions: ");
        sb.append(System.getProperty("line.separator"));
        for (WbXmlDefinition def: linkedDefinitions.values()) {
            sb.append(def.getName());
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
}
