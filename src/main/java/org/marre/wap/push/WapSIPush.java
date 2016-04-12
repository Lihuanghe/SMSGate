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
package org.marre.wap.push;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;

import org.marre.wap.wbxml.WbxmlDocument;
import org.marre.wap.wbxml.WbxmlWriter;
import org.marre.xml.XmlAttribute;
import org.marre.xml.XmlWriter;

public class WapSIPush implements WbxmlDocument , Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4283785673451164221L;
	public static final String WBXML_CONTENT_TYPE = "application/vnd.wap.sic";
    public static final String XML_CONTENT_TYPE = "text/vnd.wap.si";
        
    public static final String[] SI_TAG_TOKENS = {
            "si", // 05
            "indication", // 06
            "info", // 07
            "item", // 08
    };

    public static final String[] SI_ATTR_START_TOKENS = {
            "action=signal-none", // 05
            "action=signal-low", // 06
            "action=signal-medium", // 07
            "action=signal-high", // 08
            "action=delete", // 09
            "created", // 0A
            "href", // 0B
            "href=http://", // 0C
            "href=http://www.", // 0D
            "href=https://", // 0E
            "href=https://www.", // 0F

            "si-expires", // 10
            "si-id", // 11
            "class", // 12
    };

    public static final String[] SI_ATTR_VALUE_TOKENS = {
            ".com/", // 85
            ".edu/", // 86
            ".net/", // 87
            ".org/", // 88
    };

    protected String uri_;
    protected String id_;
    protected Date createdDate_;
    protected Date expiresDate_;
    protected String action_;

    protected String message_;
    
    public WapSIPush(String uri, String message)
    {
        uri_ = uri;
        message_ = message;
    }

    /*
     * public static void main(String argv[]) throws Exception { SIPush push =
     * new SIPush("http://wap.tv4.se/", "TV4 Nyheter");
     * push.writeTo(getWbxmlWriter(), new FileOutputStream("si.wbxml")); }
     */

    private byte[] encodeDateTime(Date date)
    {
        return null;
        /*
         * If used, the attribute value MUST be expressed in a date/time
         * representation based on [ISO8601] as specified in [HTML4]. However,
         * SI does not allow use of time zones; the time MUST always be
         * expressed in Co-ordinated Universal Time (UTC), a 24-hour timekeeping
         * system (indicated by the ?Z?). The format is: YYYY-MM-DDThh:mm:ssZ
         * Where: YYYY = 4 digit year (?0000? ... ?9999?) MM = 2 digit month
         * (?01?=January, ?02?=February ... ?12?=December) DD = 2 digit day
         * (?01?, ?02? ... ?31?) hh = 2 digit hour, 24-hour timekeeping system
         * (00 ... 23) mm = 2 digit minute (?00? ... ?59?) ss = 2 digit second
         * (?00? ... ?59?) Note: T and Z appear literally in the string.
         * Example: 1999-04-30T06:40:00Z means 6.40 in the morning UTC on the
         * 30th of April 1999.
         */
    }

    public String getUri()
    {
        return uri_;
    }

    public void setUri(String uri)
    {
        uri_ = uri;
    }

    public String getId()
    {
        return id_;
    }

    public void setId(String id)
    {
        id_ = id;
    }

    public Date getCreated()
    {
        return createdDate_;
    }

    public void setCreated(Date created)
    {
        createdDate_ = created;
    }

    public Date getExpires()
    {
        return expiresDate_;
    }

    public void setExpires(Date expires)
    {
        expiresDate_ = (Date) expires.clone();
    }

    public String getAction()
    {
        return action_;
    }

    public void setAction(String action)
    {
        action_ = action;
    }

    public String getMessage()
    {
        return message_;
    }

    public void setMessage(String message)
    {
        message_ = message;
    }

    public void writeXmlTo(XmlWriter writer) throws IOException
    {
        writer.setDoctype("si", "-//WAPFORUM//DTD SI 1.0//EN", "http://www.wapforum.org/DTD/si.dtd");

        writer.addStartElement("si");
        writer.addStartElement("indication", new XmlAttribute[]{new XmlAttribute("href", uri_)});
        writer.addCharacters(message_);
        writer.addEndElement();
        writer.addEndElement();

        writer.flush();
    }

    public XmlWriter getWbxmlWriter(OutputStream os)
    {
        return new WbxmlWriter(os, WapSIPush.SI_TAG_TOKENS, WapSIPush.SI_ATTR_START_TOKENS, WapSIPush.SI_ATTR_VALUE_TOKENS);
    }

    public String getWbxmlContentType()
    {
        return WBXML_CONTENT_TYPE;
    }

    public String getContentType()
    {
        return XML_CONTENT_TYPE;
    }
}
