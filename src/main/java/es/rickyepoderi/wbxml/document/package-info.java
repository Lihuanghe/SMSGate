/**
 * <p>This package is a Java/Object/Memory representation of a WBXML document.
 * The whole package contains the different elements that can be part a WBXML
 * (element, attributes, PI,...) and the final WbXMLDocument is the main 
 * object that represents a final WBXML document. There are two special classes:
 * WbXMLParser (class that reads a input stream and constructs the memory
 * representation of the WBXML document) and WbXmlEncoder (class that writes
 * a WbXmlDocument from the memory representation to an output stream).</p>
 * 
 * <p>The WBXML format specification defines a WBXML document like this 
 * (chapter <em>5.3. BNF for Document Structure</em>):</p>
 * 
 * <pre>
 * start       = version publicid charset strtbl body
 * strtbl      = length *byte
 * body        = *pi element *pi
 * element     = stag [ 1*attribute END ] [ *content END ]
 * 
 * content     = element | string | extension | entity | pi | opaque
 * 
 * stag        = TAG | ( LITERAL index )
 * attribute   = attrStart *attrValue
 * attrStart   = ATTRSTART | ( LITERAL index )
 * attrValue   = ATTRVALUE | string | extension | entity
 * 
 * extension   = ( EXT_I termstr ) | ( EXT_T index ) | EXT
 * 
 * string      = inline | tableref
 * inline      = STR_I termstr
 * tableref    = STR_T index
 * 
 * entity      = ENTITY entcode
 * entcode     = mb_u_int32  // UCS-4 character code
 * 
 * pi          = PI attrStart *attrValue END
 * 
 * opaque      = OPAQUE length *byte
 * 
 * version     = u_int8 containing WBXML version number
 * publicid    = mb_u_int32 | ( zero index )
 * charset     = mb_u_int32
 * termstr     = charset-dependent string with termination
 * index       = mb_u_int32  // integer index into string table.
 * length      = mb_u_int32  // integer length.
 * zero        = u_int8      // containing the value zero (0).
 * </pre>
 * 
 * <p>The different classes in this package represents a part of the previous
 * specification (usually a class is thought to be as simple as possible). In 
 * summary this package is a way of passing from a WBXML stream to a more
 * easy to manage memory representation and vice-versa. The stream package
 * always convert first to this internal representation (both, reader and
 * writer). It is fair to say that the idea is copied from the 
 * <a href="https://libwbxml.opensync.org/">libwbxml</a> C library. It is
 * obvious that managing an intermediate structure is not the best in terms
 * of performance but the WBXML specification is so complicated that it seems
 * the most reasonable way.</p>
 * 
 * <p>Just as an example the following SI XML document is presented:</p>
 * 
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE si PUBLIC "-//WAPFORUM//DTD SI 1.0//EN" "http://www.wapforum.org/DTD/si.dtd"&gt;
 * &lt;si&gt;
 *   &lt;indication href="http://www.xyz.com/email/123/abc.wml"
 *             created="1999-06-25T15:23:15Z"
 *             si-expires="1999-06-30T00:00:00Z"&gt;
 *       You have 4 new emails
 *   &lt;/indication&gt;
 * &lt;/si&gt;
 * </pre>
 * 
 * <p>It is expressed in this intermediate form as follows:</p>
 * 
 * <pre>
 * new WbXmlDocument(
 *     WbXmlVersion.VERSION_1_3, 
 *     WbXmlInitialization.getDefinitionByFPI("-//WAPFORUM//DTD SI 1.0//EN");, 
 *     IanaCharset.UTF_8, 
 *     new WbXmlBody(
 *         new WbXmlElement(
 *             "si", 
 *             new WbXmlElement(
 *                 "indication",
 *                 new WbXmlAttribute[] {
 *                     new WbXmlAttribute("href", "http://www.xyz.com/email/123/abc.wml"),
 *                     new WbXmlAttribute("created", "1999-06-25T15:23:15Z"),
 *                     new WbXmlAttribute("si-expires", "1999-06-30T00:00:00Z")
 *                 },
 *                 "You have 4 new emails"
 *             )
 *         )
 *     )
 * );
 * </pre>
 * 
 */
package es.rickyepoderi.wbxml.document;
