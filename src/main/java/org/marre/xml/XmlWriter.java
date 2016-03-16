/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.marre.xml;

import java.io.IOException;

/**
 * Simple interface to write XML documents.
 *
 * Mainly created to make it simple to create both XML and WBXML documents.
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public interface XmlWriter
{
    /**
     * Sets a public ID doctype.
     * 
     * Must be set before any of the add or flush methods.
     * 
     * @param publicID XML public ID
     */
    void setDoctype(String publicID);

    /**
     * Sets a system URI doctype.
     * 
     * Must be set before any of the add or flush methods.
     * 
     * @param name Name
     * @param systemURI System URI
     */
    void setDoctype(String name, String systemURI);

    /**
     * Sets a public ID doctype.
     * 
     * Must be set before any of the add or flush methods.
     * 
     * @param name Name
     * @param publicID PublicID
     * @param publicURI PublicURI
     */
    void setDoctype(String name, String publicID, String publicURI);

    /**
     * Adds a start element tag.
     * 
     * Ex: &lt;TAG&gt;
     * 
     * @param tag tag
     * @throws IOException io error
     */
    void addStartElement(String tag) throws IOException;

    /**
     * Adds a start element tag with attributes.
     * 
     * Ex: &lt;TAG attrib="value"&gt;
     * 
     * @param tag Tag
     * @param attribs Attributes
     * @throws IOException io error
     */
    void addStartElement(String tag, XmlAttribute[] attribs) throws IOException;

    /**
     * Adds an empty element tag.
     * 
     * Ex: &lt;TAG/&gt;
     * 
     * @param tag Tag
     * @throws IOException io error
     */
    void addEmptyElement(String tag) throws IOException;

    /**
     * Adds an empty start element tag with attributes.
     * 
     * Ex: &lt;TAG attrib="value"/&gt;
     * 
     * @param tag Tag
     * @param attribs Attributes
     * @throws IOException io error
     */
    void addEmptyElement(String tag, XmlAttribute[] attribs) throws IOException;

    /**
     * Adds an end element tag.
     * 
     * Ex &lt;/TAG&gt;
     * 
     * @throws IOException io error
     */
    void addEndElement() throws IOException;

    /**
     * Adds a segment of text.
     * 
     * @param ch The chars to add
     * @param start Start offset of the ch array.
     * @param length Number of chars to add
     * @throws IOException io error
     */
    void addCharacters(char[] ch, int start, int length) throws IOException;

    /**
     * Adds a segment of text.
     * 
     * @param str Text to add
     * @throws IOException io error
     */
    void addCharacters(String str) throws IOException;
    
    /**
     * Flushes the xml document.
     * 
     * Must be called to be sure that the document is correctly created.
     * 
     * @throws IOException io error
     */
    void flush() throws IOException;
}
