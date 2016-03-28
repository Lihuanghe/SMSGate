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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * <p>The initialization class is a way to load into the JVM all the language
 * definitions that the implementation is going to use. The class is maybe
 * a bit overelaborated.</p>
 * 
 * <p>The class loads all the definitions that are paced in a specified 
 * classpath location:</p>
 * 
 * <p>es/rickyepoderi/wbxml/definition/defaults</p>
 * 
 * <p>This location can be overriden using a system property:</p>
 * 
 * <p>-Des.rickyepoderi.wbxml.definition.path=new/classpath/resource/path</p>
 * 
 * <p>Remember that the languages are loaded from the classpath, not from a 
 * directory in the file system. Only JAR or normal directory can be used 
 * to place the property files.</p>
 * 
 * <p>All the definition properties file should be of the name 
 * <em>wbxml.&lt;LANG_NAME&gt;.properties</em>
 * and they are just loaded into the system. Then all the languages can be 
 * retrieved using the getDefinition* methods of this class.</p>
 * 
 * @author ricky
 */
public class WbXmlInitialization {
    
    /**
     * Logger for the class.
     */
    protected static final Logger log = Logger.getLogger(WbXmlInitialization.class.getName());

    //
    // language location and name syntax
    //
    
    /**
     * The system property name for changing default classpath location
     * of the language definition properties file,
     */
    static public final String RESOURCE_DIRECTORY_PROPERTY = "es.rickyepoderi.wbxml.definition.path";
    
    /**
     * Default location for language definitions.
     */
    static public final String DEFAULT_RESOURCE_DIRECTORY = "es/rickyepoderi/wbxml/definition/defaults";
    
    /**
     * Suffix for the properties file of definitions.
     */
    static public final String PROPERTIES_SUFFIX = ".properties";
    
    /**
     * Prefix for the properties file odf definitions.
     */
    static public final String PROPERTIES_PREFIX = "wbxml.";

    //
    // Name for keys inside the property file
    //
    
    /**
     * Name for the property that has the language name. It is compulsory.
     */
    static public final String PROP_WBXML_NAME = "wbxml.name";
    
    /**
     * Name for the property that has the publid id of the definition. It should
     * be 0x01 (unknown) if the definitions has no standard name.
     */
    static public final String PROP_WBXML_PUBLIC_ID = "wbxml.publicid";
    
    /**
     * Name for the property that has the FPI of the definition. It can
     * be omitted if the definition has no formal id but it that case the 
     * recognition of the language can be impossible and parsing/decoding 
     * needs direct specification of the definition language to use.
     */
    static public final String PROP_WBXML_XML_PUBLIC_IDENTIFIER =  "wbxml.xmlpublicidentifier";
    
    /**
     * Name for the property that has the URI for dtd files of the definition.
     * It can be null.
     */
    static public final String PROP_WBXML_XML_URI_REFERENCE =  "wbxml.xmlurireference";
    
    /**
     * Name for the property that specifies the root element of the language.
     * Please set it with some value (in case the definition has several possible
     * root element just put the most common one). This should be prefixed if
     * namespaces are used.
     */
    static public final String PROP_WBXML_ROOT_ELEMENT =  "wbxml.rootelement";
    
    /**
     * The name for the property that specifies the JAXB class for the root element.
     * This can be null. In the implementation no JAXB classes are packaged
     * inside the library. In the test part there are some of them just for testing
     * purposes.
     */
    static public final String PROP_WBXML_CLASS_ELEMENT =  "wbxml.class";
    
    /**
     * Prefix for properties that handle the language tags. The tags are in the
     * following form:
     * 
     * <pre>
     * wbxml.tag.{pageCode}.[{prefix}:]{name}={token}
     * </pre>
     */
    static public final String PROP_WBXML_TAG_PREFIX = "wbxml.tag.";
    
    /**
     * Prefix for properties that defines the namespaces used in the 
     * definition. There are a lot of languages that do not use them and in
     * case they are used, all the tags should be prefixed. The
     * format is the following:
     * 
     * <pre>
     * wbxml.namespaces.{prefix}={namespaceURI}
     * </pre>
     */
    static public final String PROP_WBXML_NAMESPACE_PREFIX = "wbxml.namespaces.";
    
    /**
     * Prefix for properties that handle the language attributes. The attributes
     * can define part of the value so the format is a bit different and it is
     * based in two properties. The first one is used to specify the page and
     * the numeric token and the second one (optional) if some value is used.
     * 
     * <pre>
     * wbxml.attr.{pageCode}.[{prefix}:]{name}[.{optional-differenciator}]={token}
     * {previous-key}.value={optional-value}
     * </pre>
     */
    static public final String PROP_WBXML_ATTR_PREFIX = "wbxml.attr.";
    
    /**
     * Prefix for properties that handle attribute values. The values are
     * common values for attributes that has a specified token in order
     * to save space in the binary WBXML representation. The format uses again
     * two properties:
     * 
     * <pre>
     * wbxml.attrvalue.{pageCode}[.{optional}]={token}
     * {previous_key}.value={value}
     * </pre>
     */
    static public final String PROP_WBXML_ATTR_VALUE_PREFIX = "wbxml.attrvalue.";
    
    /**
     * Prefix for properties that handle extensions in the language. Extensions
     * are similar to attribute values but it can be used in any string value
     * (attribute of content). Again the format consists in two properties:
     * 
     * <pre>
     * wbxml.ext.{key_differenciator}={token}
     * {previous_key}.value={value}
     * </pre>
     */
    static public final String PROP_WBXML_EXT_PREFIX = "wbxml.ext.";
    
    /**
     * Prefix used for opaques for attribute values. The opaque is a weird
     * way of encoding any special value in a wbxml file. In this case the
     * opaque will pase/encode special attribute values.
     */
    static public final String PROP_WBXML_OPAQUE_ATTR_PREFIX = "wbxml.opaque.attr.";
    
    /**
     * Prefix used for opaques that are used for tags. The opaque is a weird
     * way of encoding any special value in a wbxml file. In this case the
     * opaque will parse/encode tag contents.
     * 
     * <pre>
     * webxml.opaque.attr.{pageCode}.{name}={class}
     * </pre>
     */
    static public final String PROP_WBXML_OPAQUE_TAG_PREFIX = "wbxml.opaque.tag.";
    
    /**
     * The suffix use when a second property is needed (attributes, extensions
     * and so on).
     */
    static public final String PROP_WBXML_VALUE_SUFFIX = ".value";
    
    /**
     * Prefix used for properties that has the linked definitions. Linked 
     * definitions are not standard and just are used for weird languages
     * which mix another ones (SyncML for instance). The format is the
     * following:
     * 
     * <pre>
     * wbxml.opaque.linkeddef.{differenciator}={language_name}
     * </pre>
     */
    static public final String PROP_WBXML_LINKED_DEF = "wbxml.opaque.linkeddef.";
    
    /**
     * Static list of definitions that are loaded at initialization of this class.
     */
    static private List<WbXmlDefinition> definitions = null;
    
    //
    // Private method to load and parse the files
    //
    
    /**
     * Private method that constructs the tag using the property.
     * @param key The property key for tag
     * @param value The value (the token)
     * @return The WbXxmlTagDef that this property represents
     */
    static private WbXmlTagDef getTagDefinition(String key, String value) {
        try {
            // the line is "wbxml.tag.<pageCode>.[<prefix>:]<name>=<token>"
            String[] keys = key.split(Pattern.quote("."));
            byte pageCode = Integer.decode(keys[2]).byteValue();
            String name = keys[3];
            String prefix = null;
            int idx = name.indexOf(':');
            if (idx > 0) {
                prefix = name.substring(0, idx);
                name = name.substring(idx + 1);
            }
            byte token = Integer.decode(value).byteValue();
            return new WbXmlTagDef(prefix, name, token, pageCode);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading attribute {0}={1}", new Object[]{key, value});
            log.log(Level.SEVERE, "Exception", e);
            return null;
        }
    }
    
    /**
     * Private method that constructs the namespace using the property.
     * @param key The property key for the namespace
     * @param value The value for the namespace (the namespaceURI)
     * @return The WbXmlNamespaceDef that this property represents
     */
    static private WbXmlNamespaceDef getNamespaceDefinition(String key, String value) {
        try {
            // the line is "wbxml.namespaces.<prefix>=<namespace>"
            String[] keys = key.split(Pattern.quote("."));
            String prefix = keys[2];
            String namespace = value;
            return new WbXmlNamespaceDef(prefix, namespace);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading attribute {0}={1}", new Object[]{key, value});
            log.log(Level.SEVERE, "Exception",  e);
            return null;
        }
    }
    
    /**
     * Private method that constructs the attribute using the property. As 
     * attributes use two properties the second property will be found inside
     * the properties again.
     * @param props The properties to search for the second key
     * @param key The property key for the attribute
     * @param value The value of the key (the token)
     * @return The WbXmlAttributeDef that this property represents
     */
    static private WbXmlAttributeDef getAttrDefinition(Properties props, String key, String value) {
        try {
            // two possible lines
            // compulsory line: wbxml.attr.<pageCode>.[<prefix>:]<name>[.<optional>]=<token>
            // optional line  : <previous_key>.value=<value>
            String val = props.getProperty(key + PROP_WBXML_VALUE_SUFFIX);
            String[] keys = key.split(Pattern.quote("."));
            byte pageCode = Integer.decode(keys[2]).byteValue();
            String name = keys[3];
            String prefix = null;
            int idx = name.indexOf(':');
            if (idx > 0) {
                prefix = name.substring(0, idx);
                name = name.substring(idx + 1);
            }
            byte token = Integer.decode(value).byteValue();
            return new WbXmlAttributeDef(prefix, name, token, pageCode, val);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading attribute {0}={1}", new Object[]{key, value});
            log.log(Level.SEVERE, "Exception",  e);
            return null;
        }
    }
    
    /**
     * Private method that constructs the attribute value using the property. As 
     * attribute values use two properties the second property will be found inside
     * the properties again.
     * @param props The properties to search for the second key
     * @param The property key for the attribute value
     * @param value The value of the key (the token)
     * @return The WbXmlAttributeValueDef that this property represents
     */
    static private WbXmlAttributeValueDef getAttrValueDefinition(Properties props, String key, String value) {
        try {
            // two possible lines
            // compulsory line: wbxml.attrvalue.<pageCode>[.<optional>]=<token>
            // optional line  : <previous_key>.value=<value>
            String[] keys = key.split(Pattern.quote("."));
            byte pageCode = Integer.decode(keys[2]).byteValue();
            byte token = Integer.decode(value).byteValue();
            String val = props.getProperty(key + PROP_WBXML_VALUE_SUFFIX);
            return new WbXmlAttributeValueDef(val, token, pageCode);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading attribute value {0}={1}", new Object[]{key, value});
            log.log(Level.SEVERE, "Exception",  e);
            return null;
        }
    }
    
    /**
     * Private method that constructs the extension using the property. As 
     * extensions use two properties the second property will be found inside
     * the properties again.
     * @param props The properties to search for the second key
     * @param The property key for the extension
     * @param value The value of the key (the token)
     * @return The WbXmlExtensionDef that this property represents
     */
    static private WbXmlExtensionDef getExtDefinition(Properties props, String key, String value) {
        try {
            // two possible lines
            // compulsory line: wbxml.ext.<key_differenciator>=<token>
            // optional line  : <previous_key>.value=<value>
            byte token = Integer.decode(value).byteValue();
            String val = props.getProperty(key + PROP_WBXML_VALUE_SUFFIX);
            return new WbXmlExtensionDef(val, token);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading extension {0}={1}", new Object[] {key, value});
            log.log(Level.SEVERE, "Exception",  e);
            return null;
        }
    }
    
    /**
     * Private method that add a new opaque for attributes to the definition. 
     * @param def The definition to add the opaque to
     * @param props The properties to search the attribute the opaque is used to
     * @param key The property key for the opaque attr
     * @param value The value (class) of the property
     */
    static private void addOpaqueAttrPlugin(WbXmlDefinition def, Properties props, String key, String value) {
        try {
            // the line is: webxml.opaque.attr.<pageCode>.<name>=<class>
            String[] keys = key.split(Pattern.quote("."));
            byte pageCode = Integer.decode(keys[3]).byteValue();
            String name = keys[4];
            String tagProp = new StringBuilder(PROP_WBXML_ATTR_PREFIX)
                    .append(pageCode)
                    .append(".")
                    .append(name).toString();
            WbXmlAttributeDef attrDef = getAttrDefinition(props, tagProp, props.getProperty(tagProp));
            Class clazz = Class.forName(value);
            OpaqueAttributePlugin plugin = (OpaqueAttributePlugin) clazz.newInstance();
            def.addOpaqueAttr(attrDef, plugin);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading plugin {0}={1}", new Object[] {key, value});
            log.log(Level.SEVERE, "Exception",  e);
        }
    }
    
    /**
     * Private method that add a new opaque for tags/content to the definition. 
     * @param def The definition to add the opaque to
     * @param props The properties to search the tag the opaque is used to
     * @param key The property key for the opaque tag
     * @param value The value (class) of the property
     */
    static private void addOpaqueTagPlugin(WbXmlDefinition def, Properties props, String key, String value) {
        try {
            // the line is: webxml.opaque.tag.<pageCode>.<name>=<class>
            String[] keys = key.split(Pattern.quote("."));
            byte pageCode = Integer.decode(keys[3]).byteValue();
            String name = keys[4];
            String tagProp = new StringBuilder(PROP_WBXML_TAG_PREFIX)
                    .append(pageCode)
                    .append(".")
                    .append(name).toString();
            WbXmlTagDef tagDef = getTagDefinition(tagProp, props.getProperty(tagProp));
            Class clazz = Class.forName(value);
            OpaqueContentPlugin plugin = (OpaqueContentPlugin) clazz.newInstance();
            def.addOpaqueTag(tagDef, plugin);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading plugin {0}={1}", new Object[] {key, value});
            log.log(Level.SEVERE, "Exception",  e);
        }
    }
    
    /**
     * Private method that adds the linked definition to the current one. This
     * method is calling during the file processing, cos the linked definition
     * could not be loaded yet it just store the name. Then another method
     * will put the complete definition.
     * @param def The definition which is being processed
     * @param value The name of the linked definition
     */
    static private void addLinkedDefinition(WbXmlDefinition def, String value) {
        // just add the definition name 
        // at the end the real definiion will be added
        def.getLinkedDefinitions().put(value, null);
    }
    
    /**
     * Private method that processes a properties file and load a complete 
     * definition. Only the linked definitions need a post processing loop.
     * @param props The properties of the definition file
     * @return The WbXmlDefinition loaded (linked part is not completely done)
     */
    static private WbXmlDefinition loadDefinition(Properties props) {
        // read the main parameters for the definition
        String name = props.getProperty(PROP_WBXML_NAME);
        long publicId = Long.decode(props.getProperty(PROP_WBXML_PUBLIC_ID));
        String xmlPublicId = props.getProperty(PROP_WBXML_XML_PUBLIC_IDENTIFIER);
        String xmlUriRef = props.getProperty(PROP_WBXML_XML_URI_REFERENCE);
        String clazz = props.getProperty(PROP_WBXML_CLASS_ELEMENT);
        WbXmlDefinition def = new WbXmlDefinition(name, publicId, xmlPublicId, xmlUriRef, clazz);
        // read all the tags
        for (String key: props.stringPropertyNames()) {
            if (key.startsWith(PROP_WBXML_TAG_PREFIX)) {
                WbXmlTagDef tag = getTagDefinition(key, props.getProperty(key));
                if (tag != null) {
                    def.addTag(tag);
                }
            } else if (key.startsWith(PROP_WBXML_NAMESPACE_PREFIX)) {
                WbXmlNamespaceDef ns = getNamespaceDefinition(key, props.getProperty(key));
                if (ns != null) {
                    def.addNamespace(ns);
                }
            } else if (key.startsWith(PROP_WBXML_ATTR_PREFIX) &&
                    !key.endsWith(PROP_WBXML_VALUE_SUFFIX)) {
                WbXmlAttributeDef attr = getAttrDefinition(props, key, props.getProperty(key));
                if (attr != null) {
                    def.addAttr(attr);
                }
            } else if (key.startsWith(PROP_WBXML_ATTR_VALUE_PREFIX) &&
                    !key.endsWith(PROP_WBXML_VALUE_SUFFIX)) {
                WbXmlAttributeValueDef attrVal = getAttrValueDefinition(props, key, props.getProperty(key));
                if (attrVal != null) {
                    def.addAttrValue(attrVal);
                }
            } else if (key.startsWith(PROP_WBXML_EXT_PREFIX) &&
                    !key.endsWith(PROP_WBXML_VALUE_SUFFIX)) {
                WbXmlExtensionDef ext = getExtDefinition(props, key, props.getProperty(key));
                if (ext != null) {
                    def.addExtension(ext);
                }
            } else if (key.startsWith(PROP_WBXML_OPAQUE_ATTR_PREFIX)) {
                addOpaqueAttrPlugin(def, props, key, props.getProperty(key));
            } else if (key.startsWith(PROP_WBXML_OPAQUE_TAG_PREFIX)) {
                addOpaqueTagPlugin(def, props, key, props.getProperty(key));
            } else if (key.startsWith(PROP_WBXML_LINKED_DEF)) {
                addLinkedDefinition(def, props.getProperty(key));
            }
        }
        String root = props.getProperty(PROP_WBXML_ROOT_ELEMENT);
        def.setRoot(root);
        return def;
    }
    
    /**
     * Private method that load the properties file from a JAR file. The JAR
     * is iterated searching for the definition files and the definitions
     * are loaded.
     * @param resource The url resource that contains the JAR
     * @param path The path defined to contain the definitions
     */
    static private void loadPropertiesJar(URL resource, String path) {
        JarURLConnection jarConn;
        JarFile jar = null;
        try {
            jarConn = (JarURLConnection) resource.openConnection();
            jar = jarConn.getJarFile();
            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                if (entry.getName().startsWith(path + "/" + PROPERTIES_PREFIX)
                        && entry.getName().endsWith(PROPERTIES_SUFFIX)) {
                    Properties props = new Properties();
                    props.load(jar.getInputStream(entry));
                    definitions.add(loadDefinition(props));
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "loadPropertiesJar(): Error loading definition {0}", resource);
            log.log(Level.SEVERE, "loadPropertiesJar(): Error loading definition...", e);
        } finally {
            try {
                if (jar != null) {
                    jar.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Private method that loads the definitions using normal directory
     * container. 
     * @param file The directory that contains the definitions 
     */
    static private void loadPropertiesFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(PROPERTIES_SUFFIX)
                        && f.getName().startsWith(PROPERTIES_PREFIX)) {
                    FileReader reader = null;
                    try {
                        Properties props = new Properties();
                        reader = new FileReader(f);
                        props.load(reader);
                        definitions.add(loadDefinition(props));
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "loadPropertiesFile(): Error loading definition {0}", file.getAbsoluteFile());
                        log.log(Level.SEVERE, "loadPropertiesFile(): Error loading definition...", e);
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Private method that does the post-processing part for definitions. 
     * This method iterates all the definitions and, in case linked definitions
     * are defined, the real definition is placed in there.
     */
    static private void processLinkedDefinitions() {
        for (WbXmlDefinition def: getDefinitions()) {
            if (!def.getLinkedDefinitions().isEmpty()) {
                // iterate over the definitions and add the real one
                for (String name: def.getLinkedDefinitions().keySet()) {
                    WbXmlDefinition linkedDef = getDefinitionByName(name);
                    if (linkedDef != null) {
                        // add the real linked def
                        def.getLinkedDefinitions().put(name, linkedDef);
                    } else {
                        // the definition is not correctly defined => warn it
                        def.getLinkedDefinitions().remove(name);
                        log.log(Level.WARNING, "The linked definition {0} defined in {1} does not exists", 
                                new Object[]{name, def.getName()});
                    }
                }
            }
        }
    }

    /**
     * Private method that does the initialization and loading of all the
     * definition languages. The property files can be placed in the classpath
     * (using a JAR file or a common directory).
     */
    static private void init() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String resourcePath = System.getProperty(RESOURCE_DIRECTORY_PROPERTY);
        if (resourcePath == null) {
            resourcePath = DEFAULT_RESOURCE_DIRECTORY;
        }
        URL resource = classLoader.getResource(resourcePath);
        if (resource == null) {
            log.log(Level.SEVERE, "The definition path {0} is not found inside the classpath", resource);
            throw new IllegalStateException(
                    String.format("The definition path '%s' is not found inside the classpath", 
                    resourcePath));
        }
        if (resource.getProtocol().equals("jar")) {
            loadPropertiesJar(resource, DEFAULT_RESOURCE_DIRECTORY);
            processLinkedDefinitions();
        } else if (resource.getProtocol().equals("file")) {
            loadPropertiesFile(new File(resource.getFile()));
            processLinkedDefinitions();
        } else {
            log.log(Level.SEVERE, "Invalid protocol for the definitions directory: {0}", resource);
            throw new IllegalStateException(
                    String.format("Invalid protocol for the definitions directory '%s'", resource.getProtocol()));
        }
    }
    
    //
    // Public methods to get the definitions
    //
    
    /**
     * Method that returns all the definitions in the system.
     * @return The list of definitions
     */
    static synchronized public WbXmlDefinition[] getDefinitions() {
        return definitions.toArray(new WbXmlDefinition[0]);
    }
    
    /**
     * Method that return the definition using the FPI of the language.
     * @param fpi The FPI of the language
     * @return The definition for that language or null
     */
    static synchronized public WbXmlDefinition getDefinitionByFPI(String fpi) {
        for (WbXmlDefinition def: definitions) {
            if (def.getXmlPublicId() != null && def.getXmlPublicId().equals(fpi)) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Method that return the definition using the public id of the language.
     * @param id The public id of the language
     * @return The definition for that language or null
     */
    static synchronized public WbXmlDefinition getDefinitionByPublicId(long id) {
        for (WbXmlDefinition def: definitions) {
            if (def.getPublicId() == id) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Method that return the definition using the name of the language.
     * @param name The name of the language
     * @return The definition for that language or null
     */
    static synchronized public WbXmlDefinition getDefinitionByName(String name) {
        for (WbXmlDefinition def: definitions) {
            if (def.getName() != null && def.getName().equals(name)) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Method that return the definition using the root element of the language.
     * @param root The root name
     * @param namespaceUri The namespace of the root element
     * @return The definition for that language or null
     */
    static synchronized public WbXmlDefinition getDefinitionByRoot(String root, String namespaceUri) {
        for (WbXmlDefinition def : definitions) {
            if (namespaceUri != null && !namespaceUri.isEmpty()) {
                // get the prefix for this namespace
                String prefix = def.getPrefix(namespaceUri);
                if (prefix != null) {
                    root = new StringBuilder(prefix).append(":").append(root).toString();
                }
            }
            if (def.getRoot() != null && def.getRoot().getNameWithPrefix().equals(root)) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Method that reloads the definitions using normal processing.
     */
    static synchronized public void reload() {
        definitions.clear();
        init();
    }
    
    //
    // Initialization
    //
    
    static {
        definitions = new ArrayList<WbXmlDefinition>();
        init();
    }
}
