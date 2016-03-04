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
 * Portions created by the Initial Developer are Copyright (C) 2002, 2003, 2004
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
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

/**
 * XmlWriter that creates text xml documents.
 * 
 * NOTE! Not completed yet.
 *  
 * @author Markus Eriksson
 * @version $Id$
 */
public class TextXmlWriter implements XmlWriter
{
    /**
     * The writer that is used internally to store the xml document.
     */
    private final Writer writer_;
    
    /**
     * Stack of tags. Used by addEndElement to know what tag to add.
     */
    private final Deque<String> tagStack_ = new ArrayDeque<String>();

    /**
     * Used by addElement to insert a \r\n.
     */
    protected boolean charsAddedBetweenTags_ = true;

    /**
     * Constructor.
     * 
     * @param writer The writer to write to.
     */
    public TextXmlWriter(Writer writer)
    {
        writer_ = writer;
    }

    /**
     * Sets the doctype.
     * 
     * @see org.marre.xml.XmlWriter#setDoctype(java.lang.String)
     */
    public void setDoctype(String publicID)
    {
        /* TODO: Not implemented yet */
    }

    /**
     * Sets the doctype.
     * 
     * @see org.marre.xml.XmlWriter#setDoctype(java.lang.String, java.lang.String)
     */
    public void setDoctype(String name, String systemURI)
    {
        /* TODO: Not implemented yet */
    }

    /**
     * Sets the doctype.
     * 
     * @see org.marre.xml.XmlWriter#setDoctype(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setDoctype(String name, String publicID, String publicURI)
    {
        /* TODO: Not implemented yet */
    }

    /**
     * Adds a start element tag.
     * 
     * @see org.marre.xml.XmlWriter#addStartElement(java.lang.String)
     */
    public void addStartElement(String tag) throws IOException
    {
        if (!charsAddedBetweenTags_)
        {
            writer_.write("\r\n");
        }
        charsAddedBetweenTags_ = false;

        writer_.write("<" + tag + ">");
        tagStack_.push(tag);
    }

    /**
     *  Adds a start element tag.
     *
     * @see org.marre.xml.XmlWriter#addStartElement(java.lang.String, org.marre.xml.XmlAttribute[])
     */
    public void addStartElement(String tag, XmlAttribute[] attribs) throws IOException
    {
        throw new IOException("Not implemented");
    }

    /**
     *  Adds an empty start element tag.
     *
     * @see org.marre.xml.XmlWriter#addEmptyElement(java.lang.String)
     */
    public void addEmptyElement(String tag) throws IOException
    {
        if (!charsAddedBetweenTags_)
        {
            writer_.write("\r\n");
        }
        charsAddedBetweenTags_ = false;

        writer_.write("<" + tag + "/>\r\n");
    }

    /**
     * Adds an empty start element tag with attributes.
     * 
     * @see org.marre.xml.XmlWriter#addEmptyElement(java.lang.String, org.marre.xml.XmlAttribute[])
     */
    public void addEmptyElement(String tag, XmlAttribute[] attribs) throws IOException
    {
        throw new IOException("Not implemented");
    }

    /**
     * Adds an end tag.
     * 
     * @see org.marre.xml.XmlWriter#addEndElement()
     */
    public void addEndElement() throws IOException
    {
        String tag = tagStack_.pop();
        writer_.write("</" + tag + ">\r\n");
    }

    /**
     * Adds characters to the xml document.
     * 
     * @see org.marre.xml.XmlWriter#addCharacters(char[], int, int)
     */
    public void addCharacters(char[] ch, int start, int length) throws IOException
    {
        charsAddedBetweenTags_ = true;
        writer_.write(ch, start, length);
    }

    /**
     * Adds a string to the xml document.
     * 
     * @see org.marre.xml.XmlWriter#addCharacters(java.lang.String)
     */
    public void addCharacters(String str) throws IOException
    {
        charsAddedBetweenTags_ = true;
        writer_.write(str);
    }

    /**
     * Flushes the writer.
     * 
     * @see org.marre.xml.XmlWriter#flush()
     */
    public void flush() throws IOException
    {
        writer_.flush();
    }
}
