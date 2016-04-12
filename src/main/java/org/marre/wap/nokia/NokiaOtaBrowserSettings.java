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
package org.marre.wap.nokia;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.marre.wap.wbxml.WbxmlDocument;
import org.marre.wap.wbxml.WbxmlWriter;
import org.marre.xml.XmlWriter;

public class NokiaOtaBrowserSettings implements WbxmlDocument ,	 Serializable
{    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2912925345836104645L;
	public static final String WBXML_SETTINGS_CONTENT_TYPE = "application/x-wap-prov.browser-settings";
    public static final String XML_SETTINGS_CONTENT_TYPE = "application/x-wap-prov.browser-settings";
    public static final String XML_BOOKMARKS_CONTENT_TYPE = "application/x-wap-prov.browser-bookmarks";
        
    public static final String[] OTA_TAG_TOKENS = {
        "CHARACTERISTIC-LIST", // 05
        "CHARACTERISTIC", // 06
        "PARM", // 07
    };

    public static final String[] OTA_ATTR_START_TOKENS = {
        "", // 05
        "TYPE=ADDRESS", // 06
        "TYPE=URL", // 07
        "TYPE=NAME", // 08
        "", // 09
        "", // 0A
        "", // 0B
        "", // 0C
        "", // 0D
        "", // 0E
        "", // 0F
        "NAME", // 10
        "VALUE", // 11
        "NAME=BEARER", // 12
        "NAME=PROXY", //13
        "NAME=PORT", //14
        "NAME=NAME", //15
        "NAME=PROXY_TYPE", //16
        "NAME=URL", //17
        "NAME=PROXY_AUTHNAME", //18
        "NAME=PROXY_AUTHSECRET", //19
        "NAME=SMS_SMSC_ADDRESS", //1A
        "NAME=USSD_SERVICE_CODE", //1B
        "NAME=GPRS_ACCESSPOINTNAME", //1C
        "NAME=PPP_LOGINTYPE", //1D
        "NAME=PROXY_LOGINTYPE", //1E
        "", //1F
        "", //20
        "NAME=CSD_DIALSTRING", //21
        "NAME=PPP_AUTHTYPE", //22
        "NAME=PPP_AUTHNAME", //23
        "NAME=PPP_AUTHSECRET", //24
        "", //25
        "", //26
        "", //27
        "NAME=CSD_CALLTYPE", //28
        "NAME=CSD_CALLSPEED", //29
        "", //2A
        "", //2B
        "", //2C
        "", //2D
        "", //2E
        "", //2F
        "", //30
        "", //31
        "", //32
        "", //33
        "", //34
        "", //35
        "", //36
        "", //37
        "", //38
        "", //39
        "", //3A
        "", //3B
        "", //3C
        "", //3D
        "", //3E
        "", //3F
        "", //40
        "", //41
        "", //42
        "", //43
        "", //44
        "VALUE=GSM/CSD", //45
        "VALUE=GSM/SMS", //46
        "VALUE=GSM/USSD", //47
        "VALUE=IS-136/CSD", //48
        "VALUE=GPRS", //49
        "", //4A
        "", //4B
        "", //4C
        "", //4D
        "", //4E
        "", //4F
        "", //50
        "", //51
        "", //52
        "", //53
        "", //54
        "", //55
        "", //56
        "", //57
        "", //58
        "", //59
        "", //5A
        "", //5B
        "", //5C
        "", //5D
        "", //5E
        "", //5F
        "VALUE=9200", //60
        "VALUE=9201", //61
        "VALUE=9202", //62
        "VALUE=9203", //63
        "VALUE=AUTOMATIC", //64
        "VALUE=MANUAL", //65
        "", //66
        "", //67
        "", //68
        "", //69
        "VALUE=AUTO", //6A
        "VALUE=9600", //6B
        "VALUE=14400", //6C
        "VALUE=19200", //6D
        "VALUE=28800", //6E
        "VALUE=38400", //6F
        "VALUE=PAP", //70
        "VALUE=CHAP", //71
        "VALUE=ANALOGUE", //72
        "VALUE=ISDN", //73
        "VALUE=43200", //74
        "VALUE=57600", //75
        "VALUE=MSISDN_NO", //76
        "VALUE=IPV4", //77
        "VALUE=MSCHAP", //78
        "", //79
        "", //7A
        "", //7B
        "TYPE=MMSURL", //7C
        "TYPE=ID", //7D
        "NAME=ISPNAME", //7E
        "TYPE=BOOKMARK", //7F
    };

    public static final String[] OTA_ATTR_VALUE_TOKENS = {
        "", // 85
        "", // 86
        "", // 87
    };

    /* ADDRESS, URL, NAME, ID, MMSURL, BOOKMARK */
    
    /* ADDRESS: 
     *  BEARER
     *  PPP_AUTHTYPE    
     *  PPP_AUTHSECRET
     *  PPP_LOGINTYPE
     *  PROXY
     *  PROXY_TYPE
     *  PROXY_AUTHNAME
     *  PROXY_AUTHSECRET
     *  PROXY_LOGINTYPE
     *  PORT
     *  CSD_DIALSTRING
     *  CSD_CALLTYPE
     *  CSD_CALLSPEED
     *  ISP_NAME
     *  SMS_SMSC_ADDRESS
     *  USSD_SERVICE_CODE
     *  GPRS_ACCESSPOINTNAME
     */
    
    /*
     * URL or MMSURL:
     */
    
    /*
     * NAME:
     */
    
    /*
     * BOOKMARK:
     *  NAME
     *  URL
     */
    protected final List<NokiaOtaBookmark> bookmarks_ = new LinkedList<NokiaOtaBookmark>();
    
    /*
     * ID:
     */
    
    public NokiaOtaBrowserSettings()
    {
    }
    
    protected void writeBookmarksTo(XmlWriter xmlWriter) throws IOException
    {
        for (NokiaOtaBookmark otaBookmark : bookmarks_) {
            otaBookmark.writeXmlTo(xmlWriter);
        }
    }
    
    public void addBookmark(String name, String url)
    {
        bookmarks_.add(new NokiaOtaBookmark(name, url));
    }

    public String getContentType()
    {
        return XML_SETTINGS_CONTENT_TYPE;
    }
    
    public String getWbxmlContentType()
    {
        return WBXML_SETTINGS_CONTENT_TYPE;
    }

    public XmlWriter getWbxmlWriter(OutputStream os)
    {
        return new WbxmlWriter(os, OTA_TAG_TOKENS, OTA_ATTR_START_TOKENS, OTA_ATTR_VALUE_TOKENS);
    }
    
    public void writeXmlTo(XmlWriter xmlWriter) throws IOException
    {
        xmlWriter.setDoctype("CHARACTERISTIC-LIST", "/DTD/characteristic_list.xml");
        
        // <CHARACTERISTIC-LIST>
        xmlWriter.addStartElement("CHARACTERISTIC-LIST");
        
        // <CHARACTERISTIC TYPE="BOOKMARK"> ...
        writeBookmarksTo(xmlWriter);
        
        // </CHARACTERISTIC-LIST>
        xmlWriter.flush();
    }
}
