/**
 * <p>This package is intended to manage WBXML language definitions. The WBXML
 * format specification assigns binary tokens (usually one byte per token)
 * to different elements of a typical XML format (tags, attribute names and
 * so on). So it can be said that like a XML uses a DTD or XSD to define a
 * specific XM language the WBXML uses (besides) another definition. Here
 * that definition will be called language definition. There are several
 * WBXML language definitions like: SyncML (Synchronization Markup Language), 
 * WV (Wireless Village), Exchange ActiveSync (EAS), WML (Wireless Markup
 * Language) and many more.</p> 
 * 
 * <p>So in order to parse and encode a WBXML format it is absolutely necessary
 * a way of understanding, defining and loading the language. This packages does
 * all of these things. The main ideas have been copied from 
 * <a href="https://libwbxml.opensync.org/">libwbxml</a> C implementation.</p>
 * 
 * <h3>Definition</h3>
 * 
 * <p>A language definition is done using a simple properties file (it was
 * thought like this cos it is absolutely simple and it does not depend
 * on anything weird). The following keys are fixed keys used to define a 
 * language:</p>
 * 
 * <ul>
 * <li>wbxml.name: Compulsory key that is used to identify the language
 * (should be unique among all languages, for example <em>SyncML 1.1</em> or
 * <em>WV CSP 1.1</em></li>
 * <li>wbxml.publicid: WBXML specification defines that a language should use 
 * a standard integer identifier (mb_u_int32 integer). This id is encoded at the
 * begining of the document to know the language the doc is using (SyncML is
 * for instance 0x0FD3). Although it is not absolutely compulsory it is
 * very recommended (see next key).</li>
 * <li>wbxml.xmlpublicidentifier: The XML FPI (Formal Public Identifier) which
 * is used in the Doctype of any XML document. In this case is a String id
 * which is defined in the DTD of the language (again SyncML uses 
 * <em>//SYNCML//DTD SyncML 1.1//EN</em> as FPI). WBXML specification let
 * encode the language reference using this id if the previous integer id
 * is unknown.</li>
 * <li>wbxml.xmlurireference: Optional reference to the DTD that defines the
 * XML structure of the language (usually the standard languages are defined
 * using a DTD).</li>
 * <li>wbxml.rootelement: The root element of the document. It is used in the
 * WBXML to guess the language used if the definition is not given previously.
 * It is compulsory to define this key. If the language uses namespaces
 * the root element should be prefixed.</li>
 * <li>wbxml.class: Optional key used when JAXB is used (this let us know how
 * class represents the root element of the language).</li>
 * </ul>
 * 
 * <p>Besides those fixed keys a WBXML language defines list of tokens
 * to encode in binary a XML document. All those tokens are the following:</p>
 * 
 * <h4>Namespaces</h4>
 * 
 * <p>Some WBXML languages uses namespaces (one or more than one) to define
 * the tags and attributes. A list of keys are used to define all the namespaces
 * the language defines (if the language does not use namespaces no keys
 * are used). The format of a key that defines a namespace is the following:</p>
 * 
 * <pre>
 * wbxml.namespaces.{prefix}={namespaceURI}
 * 
 * · The prefix will be used for all the rest of properties (tags, attributes).
 * · The namespaceURI is the namespace the prefix refers to.
 * </pre>
 * 
 * <p>For example SyncML defines two namesapaces and the definition is as
 * follows:</p>
 * 
 * <pre>
 * wbxml.namespaces.syncml=SYNCML:SYNCML1.1
 * wbxml.namespaces.metinf=syncml:metinf
 * 
 * Two namespaces ("syncml" and "metinf" prefix) that will be used in the
 * following keys (keys for tags and attributes).
 * </pre>
 * 
 * <h4>Tags</h4>
 * 
 * <p>The XML tags (following WBXML specification) are encoded by 5 bits 
 * (in reality in a WBXML a tag token is one byte but the 7 and 6 bit
 * marks if it has attributes and contents respectively, so only 5 bits
 * remain for token definition). Besides tag tokens are grouped in a page
 * code (another byte). So a XML tag is always defined by two numbers:
 * page code (one byte) and the tag token (5 bits). Tags encoding is explained
 * in the WBXML specification in chaper <em>5.8.2. Tag Code Space</em>. In 
 * the properties file each tag is only defined by one key with the following 
 * format:</p>
 * 
 * <pre>
 * wbxml.tag.{pageCode}.[{prefix}:]{name}={token}
 * 
 * · pageCode is the byte for the page the tag token is grouped 
 *   (usually is presented in decimal format altough is parsed with decode).
 * · prefix is the prefix of the namespace if the token is defined inside
 *   a namespace (it is not used if the language does not use namespaces).
 * · name is the tag name.
 * · token is the 5 bits of the token (usually in hexa).
 * </pre>
 * 
 * <p>Some examples from the SyncML language (a prefixed one):</p>
 * 
 * <pre>
 * wbxml.tag.0.syncml\:Add=0x05
 * wbxml.tag.0.syncml\:Alert=0x06
 * wbxml.tag.0.syncml\:Archive=0x07
 * ...
 * wbxml.tag.1.metinf\:Anchor=0x05
 * wbxml.tag.1.metinf\:EMI=0x06
 * wbxml.tag.1.metinf\:Format=0x07
 * 
 * Examples of the first tags of every namespace (which in SyncML are also
 * different pages).
 * </pre>
 * 
 * <p>SI language does not use namespaces, in that case tags are not prefixed:</p>
 * 
 * <pre>
 * wbxml.tag.0.si=0x05
 * wbxml.tag.0.indication=0x06
 * wbxml.tag.0.info=0x07
 * wbxml.tag.0.item=0x08
 * 
 * SI is very little language and those are the only four tags it uses.
 * </pre>
 * 
 * <h4>Attributes</h4>
 * 
 * <p>The attribute names in the WBXML specification are encoded using 
 * again one byte but they have to be less 128 (the 7 should be 0 cos
 * 1 is used for attribute values). Besides the attribute names can specified
 * part of the whole value. A token could represent only the attribute name
 * <em>URL=</em> or the name and part of the value <em>PUBLIC="TRUE"</em>. 
 * This way the same XML attribute name can have several tokens (each one
 * will represent a different value). The attributes are also grouped in pages.
 * The chapter that explains attributes in the WBXML documentation is 
 * <em>5.8.3. Attribute Code Space (ATTRSTART and ATTRVALUE)</em>. The
 * attributes in the properties file use two different keys:</p>
 * 
 * <pre>
 * wbxml.attr.{pageCode}.[{prefix}:]{name}[.{optional-differenciator}]={token}
 * {previous-key}.value={optional-value}
 * 
 * The first key defines the token for a attribute name.
 * The second the optional value part.
 * </pre>
 * 
 * <p>The SI language for example defines several tokens (all of them in the
 * page 0) for some different value parts. Obviously the longest attribute
 * should be used when it matches (this way more characters are encoded in just
 * one byte).</p>
 * 
 * <pre>
 * wbxml.attr.0.href=0x0b
 * wbxml.attr.0.href.httpwww=0x0d
 * wbxml.attr.0.href.httpwww.value=http://www.
 * wbxml.attr.0.href.http=0x0c
 * wbxml.attr.0.href.http.value=http://
 * </pre>
 * 
 * <h4>Attribute Values</h4>
 * 
 * <p>The WBXML specification also lets encode different values (or part of
 * the value) in a byte token. An attribute value uses a token byte (in
 * that case the token should be greater or equal to 128) and also belongs
 * to a page. A token just represent a string with the value (or part of
 * the value) that it encodes. The same chapter 
 * <em>5.8.3. Attribute Code Space (ATTRSTART and ATTRVALUE)</em> of the
 * specification deals with them. In the property file they use two keys
 * very similar to attribute keys:</p>
 * 
 * <pre>
 * wbxml.attrvalue.{pageCode}[.{optional}]={token}
 * {previous_key}.value={value}
 * 
 * The first key marks the token and page.
 * The second the string value (it is compulsory for values).
 * </pre>
 * 
 * <p>The following example of attribute values are from the SI language:</p>
 * 
 * <pre>
 * wbxml.attrvalue.0.com=0x85
 * wbxml.attrvalue.0.com.value=.com/
 * wbxml.attrvalue.0.edu=0x86
 * wbxml.attrvalue.0.edu.value=.edu/
 * wbxml.attrvalue.0.net=0x87
 * wbxml.attrvalue.0.net.value=.net/
 * wbxml.attrvalue.0.org=0x88
 * wbxml.attrvalue.0.org.value=.org/
 * </pre>
 * 
 * <h4>Extensions</h4>
 * 
 * <p>WBXML also defines extensions which are tokens that can be used to 
 * encode any string value in attributes or tag contents. The extensions
 * are explained in the chapter <em>5.8.4.2. Global Extension Tokens</em> and,
 * although the specification talks about three type of extensions, the
 * <em>libwbxml</em> only uses one of them (it is supposed that no languages
 * are using the other two). In the properties file the extension is also
 * defined by two keys:</p>
 * 
 * <pre>
 * wbxml.ext.{key_differenciator}={token}
 * {previous_key}.value={value}
 * 
 * The first key defines the token number of the extension.
 * The second key the value for that extension.
 * </pre>
 * 
 * <p> In the languages which have been added only one of them uses 
 * extensions (WV - Wireless Village). They are used to encode some attribute
 * values specified as enum list of values and similar things. The following are 
 * some of them:</p>
 * 
 * <pre>
 * wbxml.ext.appvnd=0x04
 * wbxml.ext.appvnd.value=application/vnd.wap.mms-message
 * wbxml.ext.wirelessuri=0x30
 * wbxml.ext.wirelessuri.value=www.wireless-village.org
 * wbxml.ext.GROUP_USER_ID_AUTOJOIN=0x50
 * wbxml.ext.GROUP_USER_ID_AUTOJOIN.value=GROUP_USER_ID_AUTOJOIN
 * wbxml.ext.GROUP_USER_ID_JOINED=0x40
 * wbxml.ext.GROUP_USER_ID_JOINED.value=GROUP_USER_ID_JOINED
 * ...
 * </pre>
 * 
 * <h4>Opaque Plugins</h4>
 * 
 * <p>The final element that can be specified in a language definition properties
 * file are the opaque plugins. The WBXML defines the opaque data in the chapter
 * <em>5.8.4.6. Opaque Data</em>. It is a special way to encode tag contents
 * or attribute values in an unknown binary format. Obviously this is weird
 * cos it is a way of encoding data in a way that cannot be decoded by the
 * standard (you have to know how the opaque data is written). Nevertheless 
 * several languages uses them to encode DateTime values or even more strange
 * things.</p>
 * 
 * <p>The <em>libwbxml</em> library adds custom code in order to parse or encode
 * the opaque data (the code is full of defines that check if some language is
 * supported to add some methods that encode or decode the opaque). In order
 * to add this feature in the Java library a OpaquePlugin interface is used. 
 * The language specification file could add this plugin to attributes or
 * tags. This way when a WBXML document is encoded the plugin receive the
 * data to encode (for the specified tags or attributes) and when the document
 * is parsed the plugin receives the opaque byte array. Obviously in the
 * language definition properties file two keys are used:</p>
 * 
 * <pre>
 * Key for attribute plugins:
 * 
 * webxml.opaque.attr.{pageCode}.{name}={class}
 *
 * · The page code of the attribute the plugin is associated to
 * · The name of the attribute
 * · The class the implements the OpaquePlugin interface
 *
 * Key for tag plugins:
 * 
 * webxml.opaque.tag.{pageCode}.{name}={class}
 * 
 * · The page code of the tag the plugin is associated to
 * · The name of the tag
 * · The class the implements the OpaquePlugin interface
 * </pre>
 * 
 * <p>This way the library gives a way of attacking those weird opaque data
 * that can be found in every language. The WV for example uses opaque data
 * to encode integers and DateTimes (it really is optional, you can use
 * normal string values or opaque data). For that tag attributes two
 * different plugins have been added to the library:</p>
 * 
 * <pre>
 * wbxml.opaque.tag.0.Code=es.rickyepoderi.wbxml.document.opaque.WVIntegerOpaque
 * wbxml.opaque.tag.0.ContentSize=es.rickyepoderi.wbxml.document.opaque.WVIntegerOpaque
 * ...
 * wbxml.opaque.tag.0.DateTime=es.rickyepoderi.wbxml.document.opaque.WVDateTimeOpaque
 * wbxml.opaque.tag.6.DeliveryTime=es.rickyepoderi.wbxml.document.opaque.WVDateTimeOpaque
 * </pre>
 * 
 * <h4>Linked Definitions</h4>
 * 
 * <p>This is a non standard feature. Some languages (like SyncML) has some
 * tags that contains inside it another WBXML file of another language (devinf
 * in case of the SyncML language). In order to handle this a new concept 
 * was added: <em>Linked Defnition</em>. A linked definition is just that,
 * another definition that can be used inside this definition someway (using
 * and opaque for sure). This way the parsing/encoding process can know the 
 * prefix, namespaces and tags that can be appear in any document of the 
 * language.</p>
 * 
 * <p>This properties are only used in SyncML (several versions) definitions.</p>
 * 
 * <p>The format is very simple:</p>
 * 
 * <pre>
 * wbxml.opaque.linkeddef.{def_differenciator}={name_of_the_linked_definition}
 * 
 * · def_differenciator is just something to link several definitions.
 * · name_of_the_linked_definition is the name of the linked definition (the
 *   specified in the linked properties file for that definition.
 * </pre>
 * 
 * <p>For example the <em>SyncML 1.2</em> language uses two linked definitions
 * in the following way:</p>
 * 
 * <pre>
 * wbxml.opaque.linkeddef.devinf12=DevInf 1.2
 * wbxml.opaque.linkeddef.dmddf12=DMDDF 1.2
 * </pre>
 * 
 * <h3>Initialization</h3>
 * 
 * <p>The initialization or loading of all the definitions into the JVM is done
 * at the initialization of the WbXmlInitialization class. There is a location 
 * where all the properties file should be placed. This location should be 
 * inside the classpath (it can be a JAR file or normal directory).</p>
 * 
 * <p>By default the <em>wbxml-stream</em> contains the default language
 * definitions in the following path:
 * <em>es/rickyepoderi/wbxml/definition/defaults</em> 
 * and they are loaded into the system by default.</p>
 * 
 * <p>If it is needed to load different definitions the files should be packed
 * inside the classpath (inside a JAR or using a directory) and a system
 * property can be used to denote that this path should be used now instead of 
 * the default one:</p>
 * 
 * <pre>
 * -Des.rickyepoderi.wbxml.definition.path=new/classpath/resource/path
 * </pre>
 * 
 * <p>Remember that the path is a path inside the classpath, not a file system
 * path.</p>
 * 
 */
package es.rickyepoderi.wbxml.definition;
