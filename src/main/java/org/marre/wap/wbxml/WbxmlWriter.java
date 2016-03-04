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
package org.marre.wap.wbxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.marre.util.StringUtil;
import org.marre.wap.WapConstants;
import org.marre.wap.WspUtil;
import org.marre.xml.XmlAttribute;
import org.marre.xml.XmlWriter;

public class WbxmlWriter implements XmlWriter
{
    private final Map<String, Integer> stringTable_ = new HashMap<String, Integer>();
    private final ByteArrayOutputStream stringTableBuf_ = new ByteArrayOutputStream();

    private final OutputStream os_;
    private final ByteArrayOutputStream wbxmlBody_ = new ByteArrayOutputStream();

    private String[] tagTokens_;
    private String[] attrStartTokens_;
    private String[] attrValueTokens_;

    private String publicID_;
    
    public WbxmlWriter(OutputStream os, String[] tagTokens, String[] attrStrartTokens, String[] attrValueTokens)
    {
        os_ = os;

        setTagTokens(tagTokens);
        setAttrStartTokens(attrStrartTokens);
        setAttrValueTokens(attrValueTokens);
    }

    public WbxmlWriter(OutputStream os)
    {
        this(os, null, null, null);
    }
    
    /**
     * Writes the wbxml to stream.
     * 
     * @throws IOException
     */
    public void flush() throws IOException
    {
        // WBXML v 0.1
        WspUtil.writeUint8(os_, 0x01);
        // Public ID
        writePublicIdentifier(os_, publicID_);
        // Charset - "UTF-8"
        WspUtil.writeUintvar(os_, WapConstants.MIB_ENUM_UTF_8);
        // String table
        writeStringTable(os_);

        // Write body
        wbxmlBody_.close();
        wbxmlBody_.writeTo(os_);

        os_.flush();
    }

    /////// XmlWriter

    public void setDoctype(String name, String systemURI)
    {
        publicID_ = null; //Liquidterm: Defaults to unknown
    }

    public void setDoctype(String name, String publicID, String publicURI)
    {
        publicID_ = publicID;
    }

    public void setDoctype(String publicID)
    {
        publicID_ = publicID;
    }

    public void addStartElement(String tag) throws IOException
    {
        int tagIndex = StringUtil.findString(tagTokens_, tag);
        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            wbxmlBody_.write(WbxmlConstants.TOKEN_KNOWN_C | tagIndex);
        }
        else
        {
            // Unknown. Add as literal
            wbxmlBody_.write(WbxmlConstants.TOKEN_LITERAL_C);
            writeStrT(wbxmlBody_, tag);
        }
    }

    public void addStartElement(String tag, XmlAttribute[] attribs) throws IOException
    {
        int tagIndex = StringUtil.findString(tagTokens_, tag);
        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            wbxmlBody_.write(WbxmlConstants.TOKEN_KNOWN_AC | tagIndex);
        }
        else if (tag != null)
        {
            // Unknown. Add as literal (Liquidterm: only if not null)
            wbxmlBody_.write(WbxmlConstants.TOKEN_LITERAL_AC);
            writeStrT(wbxmlBody_, tag);
        }

        // Write attributes
        writeAttributes(wbxmlBody_, attribs);
    }

    public void addEmptyElement(String tag) throws IOException
    {
        int tagIndex = StringUtil.findString(tagTokens_, tag);
        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            wbxmlBody_.write(WbxmlConstants.TOKEN_KNOWN | tagIndex);
        }
        else if (tag != null)
        {
            // Unknown. Add as literal (Liquidterm: if not null)
            wbxmlBody_.write(WbxmlConstants.TOKEN_LITERAL);
            writeStrT(wbxmlBody_, tag);
        }
    }

    public void addEmptyElement(String tag, XmlAttribute[] attribs) throws IOException
    {
        int tagIndex = StringUtil.findString(tagTokens_, tag);

        if (tagIndex >= 0)
        {
            // Known tag
            tagIndex += 0x05; // Tag token table starts at #5
            wbxmlBody_.write(WbxmlConstants.TOKEN_KNOWN_A | tagIndex);
        }
        else
        {
            // Unknown. Add as literal
            wbxmlBody_.write(WbxmlConstants.TOKEN_LITERAL_A);
            writeStrT(wbxmlBody_, tag);
        }

        // Add attributes
        writeAttributes(wbxmlBody_, attribs);
    }

    public void addEndElement()
    {
        wbxmlBody_.write(WbxmlConstants.TOKEN_END);
    }

    public void addCharacters(char[] ch, int start, int length) throws IOException
    {
        addCharacters(new String(ch, start, length));
    }

    public void addCharacters(String str) throws IOException
    {
        wbxmlBody_.write(WbxmlConstants.TOKEN_STR_I);
        writeStrI(wbxmlBody_, str);
    }

    // WBXML specific stuff

    public void addOpaqueData(byte[] buff) throws IOException
    {
        addOpaqueData(buff, 0, buff.length);
    }

    public void addOpaqueData(byte[] buff, int off, int len) throws IOException
    {
        wbxmlBody_.write(WbxmlConstants.TOKEN_OPAQ);
        WspUtil.writeUintvar(wbxmlBody_, buff.length);
        wbxmlBody_.write(buff, off, len);
    }

    /**
     * Sets the tag tokens.
     * 
     * @param tagTokens
     *            first element in this array defines tag #5
     */
    public void setTagTokens(String[] tagTokens)
    {
        if (tagTokens != null)
        {
            tagTokens_ = new String[tagTokens.length];
            System.arraycopy(tagTokens, 0, tagTokens_, 0, tagTokens.length);
        }
        else
        {
            tagTokens_ = null;
        }
    }

    /**
     * Sets the attribute start tokens.
     * 
     * @param attrStrartTokens
     *            first element in this array defines attribute #85
     */
    public void setAttrStartTokens(String[] attrStrartTokens)
    {
        if (attrStrartTokens != null)
        {
            attrStartTokens_ = new String[attrStrartTokens.length];
            System.arraycopy(attrStrartTokens, 0, attrStartTokens_, 0, attrStrartTokens.length);
        }
        else
        {
            attrStartTokens_ = null;
        }
    }

    /**
     * Sets the attribute value tokens.
     * 
     * @param attrValueTokens
     *            first element in this array defines attribute #05
     */
    public void setAttrValueTokens(String[] attrValueTokens)
    {
        if (attrValueTokens != null)
        {
            attrValueTokens_ = new String[attrValueTokens.length];
            System.arraycopy(attrValueTokens, 0, attrValueTokens_, 0, attrValueTokens.length);
        }
        else
        {
            attrValueTokens_ = null;
        }
    }

    /////////////////////////////////////////////////////////

    private void writePublicIdentifier(OutputStream os, String publicID) throws IOException
    {
        if (publicID == null)
        {
            // "Unknown or missing public identifier."
            WspUtil.writeUintvar(os, 0x01);
        }
        else
        {
            int idx = StringUtil.findString(WbxmlConstants.KNOWN_PUBLIC_DOCTYPES, publicID);
            if (idx != -1)
            {
                // Known ID
                idx += 2; // Skip 0 and 1
                WspUtil.writeUintvar(os, idx);
            }
            else
            {
                // Unknown ID, add string
                WspUtil.writeUintvar(os, 0x00); // String reference following
                writeStrT(os, publicID);
            }
        }
    }

    private void writeStrI(OutputStream os, String str) throws IOException
    {
        //Liquidterm: protection against null values
        if (str != null)
        {
            os.write(str.getBytes("UTF-8"));
            os.write(0x00);
        }
    }

    private void writeStrT(OutputStream os, String str) throws IOException
    {
        Integer index = stringTable_.get(str);

        if (index == null)
        {
            index = new Integer(stringTableBuf_.size());
            stringTable_.put(str, index);
            writeStrI(stringTableBuf_, str);
        }

        WspUtil.writeUintvar(os, index.intValue());
    }

    private void writeStringTable(OutputStream os) throws IOException
    {
        // Write length of string table
        WspUtil.writeUintvar(os, stringTableBuf_.size());
        // Write string table
        stringTableBuf_.writeTo(os);
    }

    // FIXME: Unsure how to do this stuff with the attributes
    // more efficient...
    private void writeAttributes(OutputStream os, XmlAttribute[] attribs) throws IOException
    {
        int idx;

        for (XmlAttribute attrib : attribs) {
            // TYPE=VALUE
            String typeValue = attrib.getType() + "=" + attrib.getValue();
            idx = StringUtil.findString(attrStartTokens_, typeValue);
            if (idx >= 0) {
                // Found a matching type-value pair
                idx += 0x05; // Attr start token table starts at #5
                wbxmlBody_.write(idx);
            } else {
                // Try with separate type and values

                // TYPE
                idx = StringUtil.findString(attrStartTokens_, attrib.getType());
                if (idx >= 0) {
                    idx += 0x05; // Attr start token table starts at #5
                    wbxmlBody_.write(idx);
                } else {
                    wbxmlBody_.write(WbxmlConstants.TOKEN_LITERAL);
                    writeStrT(wbxmlBody_, attrib.getType());
                }

                // VALUE
                String attrValue = attrib.getValue();
                if (attrValue != null && (!attrValue.equals(""))) {
                    idx = StringUtil.findString(attrValueTokens_, attrValue);
                    if (idx >= 0) {
                        idx += 0x85; // Attr value token table starts at 85
                        wbxmlBody_.write(idx);
                    } else {
                        wbxmlBody_.write(WbxmlConstants.TOKEN_STR_I);
                        writeStrI(wbxmlBody_, attrValue);
                    }
                }
            }
        }

        // End of attributes
        wbxmlBody_.write(WbxmlConstants.TOKEN_END);
    }
}
