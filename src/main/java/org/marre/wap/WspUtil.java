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
package org.marre.wap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.marre.mime.MimeHeader;
import org.marre.mime.MimeHeaderParameter;

import com.zx.sms.common.util.StandardCharsets;

/**
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public final class WspUtil
{
    private static final Map<String, Integer> wspHeaders_;
    private static final Map<String, Integer> wspContentTypes_;
    private static final Map<String, Integer> wspParameters_;
    private static final Map<String, Integer> wspPushAppTypes_;
    
    /* Maps a header id to a well known id */
    private static final int[] WELL_KNOWN_HEADER_ID_WSP_11 = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
        0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, -1,
        -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        -1
    };
            
    private static final int[] WELL_KNOWN_HEADER_ID_WSP_12 = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
        0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  
        -1
    };
    
    private static final int[] WELL_KNOWN_HEADER_ID_WSP_13 = {
        0x00, 0x3B, 0x3C, 0x03, 0x04, 0x05, 0x06, 0x07, 0x3D, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x3e, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
        0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3F, 0x40, 0x41, 0x42, 0x43,
        -1
    };
    
    private static final int[] WELL_KNOWN_HEADER_ID_WSP_14 = {
        0x00, 0x3B, 0x3C, 0x03, 0x04, 0x05, 0x06, 0x07, 0x47, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x3e, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
        0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x45, 0x2F,
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x44, 0x38, 0x39, 0x3A, 0x3F, 0x40, 0x41, 0x42, 0x43,
        0x46
    };

    /* Maps a parameter id to a well known id */
    private static final int[] WELL_KNOWN_PARAMETER_ID_WSP_11 = {
        0x00, 0x01, 0x02, 0x03, 0x05, 0x06, 0x07, 0x08, -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
        -1,   -1,   -1,   -1,   -1,   -1
    };
            
    private static final int[] WELL_KNOWN_PARAMETER_ID_WSP_12 = {
        0x00, 0x01, 0x02, 0x03, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, -1,   -1,   -1,   -1,   -1,
        -1,   -1,   -1,   -1,   -1,   -1
    };
    
    private static final int[] WELL_KNOWN_PARAMETER_ID_WSP_13 = {
        0x00, 0x01, 0x02, 0x03, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
        -1,   -1,   -1,   -1,   -1,   -1
    };
    
    private static final int[] WELL_KNOWN_PARAMETER_ID_WSP_14 = {
        0x00, 0x01, 0x02, 0x03, 0x17, 0x18, 0x07, 0x08, 0x09, 0x19, 0x1A, 0x1B, 0x1C, 0x0E, 0x1D, 0x10,
        0x11, 0x12, 0x13, 0x14, 0x15, 0x16
    };
    
    /* Maps a well known parameter id to a parameter type */
    private static final int[] PARAMETER_TYPES = {
        WapConstants.WSP_PARAMETER_TYPE_Q_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_WELL_KNOWN_CHARSET,
        WapConstants.WSP_PARAMETER_TYPE_VERSION_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_INTEGER_VALUE,
        -1,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        WapConstants.WSP_PARAMETER_TYPE_FIELD_NAME,
        WapConstants.WSP_PARAMETER_TYPE_SHORT_INTEGER,
        
        WapConstants.WSP_PARAMETER_TYPE_CONSTRAINED_ENCODING,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        WapConstants.WSP_PARAMETER_TYPE_DELTA_SECONDS_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING,
        WapConstants.WSP_PARAMETER_TYPE_NO_VALUE,
        
        WapConstants.WSP_PARAMETER_TYPE_SHORT_INTEGER,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_DATE_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_DATE_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_DATE_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_INTEGER_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
        WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE,
    };
    
    static {
        // WSP 1.1
        wspHeaders_ = new HashMap<String, Integer>();
        wspHeaders_.put("accept",               Integer.valueOf(WapConstants.HEADER_ACCEPT));
        wspHeaders_.put("accept-charset",       Integer.valueOf(WapConstants.HEADER_ACCEPT_CHARSET));
        wspHeaders_.put("accept-encoding",      Integer.valueOf(WapConstants.HEADER_ACCEPT_ENCODING));
        wspHeaders_.put("accept-language",      Integer.valueOf(WapConstants.HEADER_ACCEPT_LANGUAGE));
        wspHeaders_.put("accept-ranges",        Integer.valueOf(WapConstants.HEADER_ACCEPT_RANGES));
        wspHeaders_.put("age",                  Integer.valueOf(WapConstants.HEADER_AGE));
        wspHeaders_.put("allow",                Integer.valueOf(WapConstants.HEADER_ALLOW));
        wspHeaders_.put("authorization",        Integer.valueOf(WapConstants.HEADER_AUTHORIZATION));
        wspHeaders_.put("cache-control",        Integer.valueOf(WapConstants.HEADER_CACHE_CONTROL));
        wspHeaders_.put("connection",           Integer.valueOf(WapConstants.HEADER_CONNECTION));
        wspHeaders_.put("content-base",         Integer.valueOf(WapConstants.HEADER_CONTENT_BASE));
        wspHeaders_.put("content-encoding",     Integer.valueOf(WapConstants.HEADER_CONTENT_ENCODING));
        wspHeaders_.put("content-language",     Integer.valueOf(WapConstants.HEADER_CONTENT_LANGUAGE));
        wspHeaders_.put("content-length",       Integer.valueOf(WapConstants.HEADER_CONTENT_LENGTH));
        wspHeaders_.put("content-location",     Integer.valueOf(WapConstants.HEADER_CONTENT_LOCATION));
        wspHeaders_.put("content-md5",          Integer.valueOf(WapConstants.HEADER_CONTENT_MD5));
        wspHeaders_.put("content-range",        Integer.valueOf(WapConstants.HEADER_CONTENT_RANGE));
        wspHeaders_.put("content-type",         Integer.valueOf(WapConstants.HEADER_CONTENT_TYPE));
        wspHeaders_.put("date",                 Integer.valueOf(WapConstants.HEADER_DATE));
        wspHeaders_.put("etag",                 Integer.valueOf(WapConstants.HEADER_ETAG));
        wspHeaders_.put("expires",              Integer.valueOf(WapConstants.HEADER_EXPIRES));
        wspHeaders_.put("from",                 Integer.valueOf(WapConstants.HEADER_FROM));
        wspHeaders_.put("host",                 Integer.valueOf(WapConstants.HEADER_HOST));
        wspHeaders_.put("if-modified-since",    Integer.valueOf(WapConstants.HEADER_IF_MODIFIED_SINCE));
        wspHeaders_.put("if-match",             Integer.valueOf(WapConstants.HEADER_IF_MATCH));
        wspHeaders_.put("if-none-match",        Integer.valueOf(WapConstants.HEADER_IF_NONE_MATCH));
        wspHeaders_.put("if-range",             Integer.valueOf(WapConstants.HEADER_IF_RANGE));
        wspHeaders_.put("if-unmodified-since",  Integer.valueOf(WapConstants.HEADER_IF_UNMODIFIED_SINCE));
        wspHeaders_.put("location",             Integer.valueOf(WapConstants.HEADER_LOCATION));
        wspHeaders_.put("last-modified",        Integer.valueOf(WapConstants.HEADER_LAST_MODIFIED));
        wspHeaders_.put("max-forwards",         Integer.valueOf(WapConstants.HEADER_MAX_FORWARDS));
        wspHeaders_.put("pragma",               Integer.valueOf(WapConstants.HEADER_PRAGMA));
        wspHeaders_.put("proxy-authenticate",   Integer.valueOf(WapConstants.HEADER_PROXY_AUTHENTICATE));
        wspHeaders_.put("proxy-authorization",  Integer.valueOf(WapConstants.HEADER_PROXY_AUTHORIZATION));
        wspHeaders_.put("public",               Integer.valueOf(WapConstants.HEADER_PUBLIC));
        wspHeaders_.put("range",                Integer.valueOf(WapConstants.HEADER_RANGE));
        wspHeaders_.put("referer",              Integer.valueOf(WapConstants.HEADER_REFERER));
        wspHeaders_.put("retry-after",          Integer.valueOf(WapConstants.HEADER_RETRY_AFTER));
        wspHeaders_.put("server",               Integer.valueOf(WapConstants.HEADER_SERVER));
        wspHeaders_.put("transfer-encoding",    Integer.valueOf(WapConstants.HEADER_TRANSFER_ENCODING));
        wspHeaders_.put("upgrade",              Integer.valueOf(WapConstants.HEADER_UPGRADE));
        wspHeaders_.put("user-agent",           Integer.valueOf(WapConstants.HEADER_USER_AGENT));
        wspHeaders_.put("vary",                 Integer.valueOf(WapConstants.HEADER_VARY));
        wspHeaders_.put("via",                  Integer.valueOf(WapConstants.HEADER_VIA));
        wspHeaders_.put("warning",              Integer.valueOf(WapConstants.HEADER_WARNING));
        wspHeaders_.put("www-authenticate",     Integer.valueOf(WapConstants.HEADER_WWW_AUTHENTICATE));
        wspHeaders_.put("content-disposition",  Integer.valueOf(WapConstants.HEADER_CONTENT_DISPOSITION));
        
        // WSP 1.2
        wspHeaders_.put("accept",               Integer.valueOf(WapConstants.HEADER_ACCEPT));
        wspHeaders_.put("x-wap-application-id", Integer.valueOf(WapConstants.HEADER_X_WAP_APPLICATION_ID));
        wspHeaders_.put("x-wap-content-uri",    Integer.valueOf(WapConstants.HEADER_X_WAP_CONTENT_URI));
        wspHeaders_.put("x-wap-initiator-uri",  Integer.valueOf(WapConstants.HEADER_X_WAP_INITIATOR_URI));
        wspHeaders_.put("bearer-indication",    Integer.valueOf(WapConstants.HEADER_BEARER_INDICATION));
        wspHeaders_.put("accept-application",   Integer.valueOf(WapConstants.HEADER_ACCEPT_APPLICATION));
        wspHeaders_.put("push-flag",            Integer.valueOf(WapConstants.HEADER_PUSH_FLAG));
        wspHeaders_.put("profile",              Integer.valueOf(WapConstants.HEADER_PROFILE));
        wspHeaders_.put("profile-diff",         Integer.valueOf(WapConstants.HEADER_PROFILE_DIFF));
        wspHeaders_.put("profile-warning",      Integer.valueOf(WapConstants.HEADER_PROFILE_WARNING));
        
        // WSP 1.3
        wspHeaders_.put("expect",               Integer.valueOf(WapConstants.HEADER_EXPECT));
        wspHeaders_.put("te",                   Integer.valueOf(WapConstants.HEADER_TE));
        wspHeaders_.put("trailer",              Integer.valueOf(WapConstants.HEADER_TRAILER));
        wspHeaders_.put("accept-charset",       Integer.valueOf(WapConstants.HEADER_ACCEPT_CHARSET));
        wspHeaders_.put("accept-encoding",      Integer.valueOf(WapConstants.HEADER_ACCEPT_ENCODING));
        wspHeaders_.put("cache-control",        Integer.valueOf(WapConstants.HEADER_CACHE_CONTROL));
        wspHeaders_.put("content-range",        Integer.valueOf(WapConstants.HEADER_CONTENT_RANGE));
        wspHeaders_.put("x-wap-tod",            Integer.valueOf(WapConstants.HEADER_X_WAP_TOD));
        wspHeaders_.put("content-id",           Integer.valueOf(WapConstants.HEADER_CONTENT_ID));
        wspHeaders_.put("set-cookie",           Integer.valueOf(WapConstants.HEADER_SET_COOKIE));
        wspHeaders_.put("cookie",               Integer.valueOf(WapConstants.HEADER_COOKIE));
        wspHeaders_.put("encoding-version",     Integer.valueOf(WapConstants.HEADER_ENCODING_VERSION));
        
        // WSP 1.4
        wspHeaders_.put("profile-warning",      Integer.valueOf(WapConstants.HEADER_PROFILE_WARNING));
        wspHeaders_.put("content-disposition",  Integer.valueOf(WapConstants.HEADER_CONTENT_DISPOSITION));
        wspHeaders_.put("x-wap-security",       Integer.valueOf(WapConstants.HEADER_X_WAP_SECURITY));
        wspHeaders_.put("cache-control",        Integer.valueOf(WapConstants.HEADER_CACHE_CONTROL));
        
        // http://www.wapforum.org/wina/wsp-content-type.htm
        // WSP 1.1
        wspContentTypes_ = new HashMap<String, Integer>();
        wspContentTypes_.put("*/*",                                            Integer.valueOf(0x00));
        wspContentTypes_.put("text/*",                                         Integer.valueOf(0x01));
        wspContentTypes_.put("text/html",                                      Integer.valueOf(0x02));
        wspContentTypes_.put("text/plain",                                     Integer.valueOf(0x03));
        wspContentTypes_.put("text/x-hdml",                                    Integer.valueOf(0x04));
        wspContentTypes_.put("text/x-ttml",                                    Integer.valueOf(0x05));
        wspContentTypes_.put("text/x-vCalendar",                               Integer.valueOf(0x06));
        wspContentTypes_.put("text/x-vCard",                                   Integer.valueOf(0x07));
        wspContentTypes_.put("text/vnd.wap.wml",                               Integer.valueOf(0x08));
        wspContentTypes_.put("text/vnd.wap.wmlscript",                         Integer.valueOf(0x09));
        wspContentTypes_.put("text/vnd.wap.wta-event",                         Integer.valueOf(0x0A));
        wspContentTypes_.put("multipart/*",                                    Integer.valueOf(0x0B));
        wspContentTypes_.put("multipart/mixed",                                Integer.valueOf(0x0C));
        wspContentTypes_.put("multipart/form-data",                            Integer.valueOf(0x0D));
        wspContentTypes_.put("multipart/byteranges",                           Integer.valueOf(0x0E));
        wspContentTypes_.put("multipart/alternative",                          Integer.valueOf(0x0F));
        wspContentTypes_.put("application/*",                                  Integer.valueOf(0x10));
        wspContentTypes_.put("application/java-vm",                            Integer.valueOf(0x11));
        wspContentTypes_.put("application/x-www-form-urlencoded",              Integer.valueOf(0x12));
        wspContentTypes_.put("application/x-hdmlc",                            Integer.valueOf(0x13));
        wspContentTypes_.put("application/vnd.wap.wmlc",                       Integer.valueOf(0x14));
        wspContentTypes_.put("application/vnd.wap.wmlscriptc",                 Integer.valueOf(0x15));
        wspContentTypes_.put("application/vnd.wap.wta-eventc",                 Integer.valueOf(0x16));
        wspContentTypes_.put("application/vnd.wap.uaprof",                     Integer.valueOf(0x17));
        wspContentTypes_.put("application/vnd.wap.wtls-ca-certificate",        Integer.valueOf(0x18));
        wspContentTypes_.put("application/vnd.wap.wtls-user-certificate",      Integer.valueOf(0x19));
        wspContentTypes_.put("application/x-x509-ca-cert",                     Integer.valueOf(0x1A));
        wspContentTypes_.put("application/x-x509-user-cert",                   Integer.valueOf(0x1B));
        wspContentTypes_.put("image/*",                                        Integer.valueOf(0x1C));
        wspContentTypes_.put("image/gif",                                      Integer.valueOf(0x1D));
        wspContentTypes_.put("image/jpeg",                                     Integer.valueOf(0x1E));
        wspContentTypes_.put("image/tiff",                                     Integer.valueOf(0x1F));
        wspContentTypes_.put("image/png",                                      Integer.valueOf(0x20));
        wspContentTypes_.put("image/vnd.wap.wbmp",                             Integer.valueOf(0x21));
        wspContentTypes_.put("application/vnd.wap.multipart.*",                Integer.valueOf(0x22));
        wspContentTypes_.put("application/vnd.wap.multipart.mixed",            Integer.valueOf(0x23));
        wspContentTypes_.put("application/vnd.wap.multipart.form-data",        Integer.valueOf(0x24));
        wspContentTypes_.put("application/vnd.wap.multipart.byteranges",       Integer.valueOf(0x25));
        wspContentTypes_.put("application/vnd.wap.multipart.alternative",      Integer.valueOf(0x26));
        wspContentTypes_.put("application/xml",                                Integer.valueOf(0x27));
        wspContentTypes_.put("text/xml",                                       Integer.valueOf(0x28));
        wspContentTypes_.put("application/vnd.wap.wbxml",                      Integer.valueOf(0x29));
        wspContentTypes_.put("application/x-x968-cross-cert",                  Integer.valueOf(0x2A));
        wspContentTypes_.put("application/x-x968-ca-cert",                     Integer.valueOf(0x2B));
        wspContentTypes_.put("application/x-x968-user-cert",                   Integer.valueOf(0x2C));
        wspContentTypes_.put("text/vnd.wap.si",                                Integer.valueOf(0x2D));

        // WSP 1.2
        wspContentTypes_.put("application/vnd.wap.sic",                        Integer.valueOf(0x2E));
        wspContentTypes_.put("text/vnd.wap.sl",                                Integer.valueOf(0x2F));
        wspContentTypes_.put("application/vnd.wap.slc",                        Integer.valueOf(0x30));
        wspContentTypes_.put("text/vnd.wap.co",                                Integer.valueOf(0x31));
        wspContentTypes_.put("application/vnd.wap.coc",                        Integer.valueOf(0x32));
        wspContentTypes_.put("application/vnd.wap.multipart.related",          Integer.valueOf(0x33));
        wspContentTypes_.put("application/vnd.wap.sia",                        Integer.valueOf(0x34));
                
        // WSP 1.3
        wspContentTypes_.put("text/vnd.wap.connectivity-xml",                  Integer.valueOf(0x35));
        wspContentTypes_.put("application/vnd.wap.connectivity-wbxml",         Integer.valueOf(0x36));
        
        // WSP 1.4
        wspContentTypes_.put("application/pkcs7-mime",                         Integer.valueOf(0x37));
        wspContentTypes_.put("application/vnd.wap.hashed-certificate",         Integer.valueOf(0x38));
        wspContentTypes_.put("application/vnd.wap.signed-certificate",         Integer.valueOf(0x39));
        wspContentTypes_.put("application/vnd.wap.cert-response",              Integer.valueOf(0x3A));
        wspContentTypes_.put("application/xhtml+xml",                          Integer.valueOf(0x3B));
        wspContentTypes_.put("application/wml+xml",                            Integer.valueOf(0x3C));
        wspContentTypes_.put("text/css",                                       Integer.valueOf(0x3D));
        wspContentTypes_.put("application/vnd.wap.mms-message",                Integer.valueOf(0x3E));
        wspContentTypes_.put("application/vnd.wap.rollover-certificate",       Integer.valueOf(0x3F));
        
        // WSP 1.5
        wspContentTypes_.put("application/vnd.wap.locc+wbxml",                 Integer.valueOf(0x40));
        wspContentTypes_.put("application/vnd.wap.loc+xml",                    Integer.valueOf(0x41));
        wspContentTypes_.put("application/vnd.syncml.dm+wbxml",                Integer.valueOf(0x42));
        wspContentTypes_.put("application/vnd.syncml.dm+xml",                  Integer.valueOf(0x43));
        wspContentTypes_.put("application/vnd.syncml.notification",            Integer.valueOf(0x44));
        wspContentTypes_.put("application/vnd.wap.xhtml+xml",                  Integer.valueOf(0x45));
        wspContentTypes_.put("application/vnd.wv.csp.cir",                     Integer.valueOf(0x46));
        wspContentTypes_.put("application/vnd.oma.dd+xml",                     Integer.valueOf(0x47));
        wspContentTypes_.put("application/vnd.oma.drm.message",                Integer.valueOf(0x48));
        wspContentTypes_.put("application/vnd.oma.drm.content",                Integer.valueOf(0x49));
        wspContentTypes_.put("application/vnd.oma.drm.rights+xml",             Integer.valueOf(0x4A));
        wspContentTypes_.put("application/vnd.oma.drm.rights+wbxml",           Integer.valueOf(0x4B));
        
        // WSP 1.1
        wspParameters_ = new HashMap<String, Integer>();
        wspParameters_.put("q",                    Integer.valueOf(WapConstants.PARAMETER_Q));
        wspParameters_.put("charset",              Integer.valueOf(WapConstants.PARAMETER_CHARSET));
        wspParameters_.put("level",                Integer.valueOf(WapConstants.PARAMETER_LEVEL));
        wspParameters_.put("type",                 Integer.valueOf(WapConstants.PARAMETER_TYPE));
        wspParameters_.put("name",                 Integer.valueOf(WapConstants.PARAMETER_NAME));
        wspParameters_.put("filename",             Integer.valueOf(WapConstants.PARAMETER_FILENAME));
        wspParameters_.put("differences",          Integer.valueOf(WapConstants.PARAMETER_DIFFERENCES));
        wspParameters_.put("padding",              Integer.valueOf(WapConstants.PARAMETER_PADDING));
            
        // WSP 1.2
        wspParameters_.put("type",                 Integer.valueOf(WapConstants.PARAMETER_TYPE_MULTIPART_RELATED));
        wspParameters_.put("start",                Integer.valueOf(WapConstants.PARAMETER_START_MULTIPART_RELATED));
        wspParameters_.put("start-info",           Integer.valueOf(WapConstants.PARAMETER_START_INFO_MULTIPART_RELATED));
            
        // WSP 1.3
        wspParameters_.put("comment",              Integer.valueOf(WapConstants.PARAMETER_COMMENT));
        wspParameters_.put("domain",               Integer.valueOf(WapConstants.PARAMETER_DOMAIN));
        wspParameters_.put("max-age",              Integer.valueOf(WapConstants.PARAMETER_MAX_AGE));
        wspParameters_.put("path",                 Integer.valueOf(WapConstants.PARAMETER_PATH));
        wspParameters_.put("secure",               Integer.valueOf(WapConstants.PARAMETER_SECURE));
            
        // WSP 1.4
        wspParameters_.put("sec",                  Integer.valueOf(WapConstants.PARAMETER_SEC_CONNECTIVITY));
        wspParameters_.put("mac",                  Integer.valueOf(WapConstants.PARAMETER_MAC_CONNECTIVITY));
        wspParameters_.put("creation-date",        Integer.valueOf(WapConstants.PARAMETER_CREATION_DATE));
        wspParameters_.put("modification-date",    Integer.valueOf(WapConstants.PARAMETER_MODIFICATION_DATE));
        wspParameters_.put("read-date",            Integer.valueOf(WapConstants.PARAMETER_READ_DATE));
        wspParameters_.put("size",                 Integer.valueOf(WapConstants.PARAMETER_SIZE));
        wspParameters_.put("name",                 Integer.valueOf(WapConstants.PARAMETER_NAME));
        wspParameters_.put("filename",             Integer.valueOf(WapConstants.PARAMETER_FILENAME));
        wspParameters_.put("start",                Integer.valueOf(WapConstants.PARAMETER_START_MULTIPART_RELATED));
        wspParameters_.put("start-info",           Integer.valueOf(WapConstants.PARAMETER_START_INFO_MULTIPART_RELATED));
        wspParameters_.put("comment",              Integer.valueOf(WapConstants.PARAMETER_COMMENT));
        wspParameters_.put("domain",               Integer.valueOf(WapConstants.PARAMETER_DOMAIN));
        wspParameters_.put("path",                 Integer.valueOf(WapConstants.PARAMETER_PATH));
        
        // http://www.wapforum.org/wina/push-app-id.htm
        wspPushAppTypes_ = new HashMap<String, Integer>();
        wspPushAppTypes_.put("x-wap-application:*",            Integer.valueOf(0x00));
        wspPushAppTypes_.put("x-wap-application:push.sia",     Integer.valueOf(0x01));
        wspPushAppTypes_.put("x-wap-application:wml.ua",       Integer.valueOf(0x02));
        wspPushAppTypes_.put("x-wap-application:wta.ua",       Integer.valueOf(0x03));
        wspPushAppTypes_.put("x-wap-application:mms.ua",       Integer.valueOf(0x04));
        wspPushAppTypes_.put("x-wap-application:push.syncml",  Integer.valueOf(0x05));
        wspPushAppTypes_.put("x-wap-application:loc.ua",       Integer.valueOf(0x06));
        wspPushAppTypes_.put("x-wap-application:syncml.dm",    Integer.valueOf(0x07));
        wspPushAppTypes_.put("x-wap-application:drm.ua",       Integer.valueOf(0x08));
        wspPushAppTypes_.put("x-wap-application:emn.ua",       Integer.valueOf(0x09));
        wspPushAppTypes_.put("x-wap-application:wv.ua",        Integer.valueOf(0x0A));
        
        wspPushAppTypes_.put("x-wap-microsoft:localcontent.ua",    Integer.valueOf(0x8000));
        wspPushAppTypes_.put("x-wap-microsoft:imclient.ua ",       Integer.valueOf(0x8001));
        wspPushAppTypes_.put("x-wap-docomo:imode.mail.ua ",        Integer.valueOf(0x8002));
        wspPushAppTypes_.put("x-wap-docomo:imode.mr.ua",           Integer.valueOf(0x8003));
        wspPushAppTypes_.put("x-wap-docomo:imode.mf.ua",           Integer.valueOf(0x8004));
        wspPushAppTypes_.put("x-motorola:location.ua ",            Integer.valueOf(0x8005));
        wspPushAppTypes_.put("x-motorola:now.ua",                  Integer.valueOf(0x8006));
        wspPushAppTypes_.put("x-motorola:otaprov.ua",              Integer.valueOf(0x8007));
        wspPushAppTypes_.put("x-motorola:browser.ua",              Integer.valueOf(0x8008));
        wspPushAppTypes_.put("x-motorola:splash.ua",               Integer.valueOf(0x8009));
        wspPushAppTypes_.put("x-wap-nai:mvsw.command ",            Integer.valueOf(0x800B));
        wspPushAppTypes_.put("x-wap-openwave:iota.ua",             Integer.valueOf(0x8010));
    }
    
    private WspUtil()
    {
    }

    /**
     * Converts a header name to a header type (WapConstants.HEADER_*).
     * 
     * The header name to be found must be in lower case (for performance reasons).
     * 
     * @param headerName The name of the header.
     * @return The header type, or -1 if not found.
     */
    public static int getHeaderType(String headerName)
    {
        Integer headerType = wspHeaders_.get(headerName);
        
        return (headerType != null) ? (headerType.intValue()) : (-1);
    }
   
    /**
     * Converts a header type (WapConstants.HEADER_*) to a well known header id.
     * 
     * @param wspEncodingVersion The requested wsp encoding version
     * @param headerType The header type
     * @return A well known header id or -1 if not found.
     */
    public static int getWellKnownHeaderId(WspEncodingVersion wspEncodingVersion, int headerType)
    {
        int wellKnownHeaderId;

        switch (wspEncodingVersion)
        {
        case VERSION_1_1:
            wellKnownHeaderId = WELL_KNOWN_HEADER_ID_WSP_11[headerType];
            break;
        case VERSION_1_2:
            wellKnownHeaderId = WELL_KNOWN_HEADER_ID_WSP_12[headerType];
            break;
            
        case VERSION_1_3:
            wellKnownHeaderId = WELL_KNOWN_HEADER_ID_WSP_13[headerType];
            break;
            
        case VERSION_1_4:
        case VERSION_1_5:
            wellKnownHeaderId = WELL_KNOWN_HEADER_ID_WSP_14[headerType];
            break;
        
        default:
            wellKnownHeaderId = -1;
        }

        return wellKnownHeaderId;
    }
    
    /**
     * Converts a content type to a WINA "well-known" content type id.
     * 
     * http://www.wapforum.org/wina/wsp-content-type.htm
     * 
     * @param wspEncodingVersion The requested wsp encoding version
     * @param contentType The content type
     * @return A well known content type id or -1 if not found.
     */
    public static int getWellKnownContentTypeId(WspEncodingVersion wspEncodingVersion, String contentType)
    {
        Integer contentTypeIdInt = wspContentTypes_.get(contentType);
        if (contentTypeIdInt == null)
        {
            return -1;
        }
        
        int wellKnownContentTypeId = contentTypeIdInt.intValue();
        if (wspEncodingVersion.isWellKnownContentTypeId(wellKnownContentTypeId)) {
            return wellKnownContentTypeId;
        } else {
            return -1;
        }
    }
    
    /**
     * Converts a parameter name to a parameter type (WapConstants.PARAMETER_*).
     * 
     * The header name to be found must be in lower case (for performance reasons).
     * 
     * @param parameterName The name of the parameter.
     * @return The parameter type, or -1 if not found.
     */
    public static int getParameterType(String parameterName)
    {
        Integer parameterType = wspParameters_.get(parameterName);
        
        return (parameterType != null) ? (parameterType.intValue()) : (-1);
    }

    /**
     * Converts a parameter name to a parameter type (WapConstants.WSP_PARAMETER_TYPE_*).
     * 
     * @param wellKnownParameterId The well known parameter id to lookup.
     * @return The parameter type, or -1 if not found.
     */
    public static int getWspParameterType(int wellKnownParameterId)
    {
        return PARAMETER_TYPES[wellKnownParameterId];
    }
    
    /**
     * Converts a parameter type (WapConstants.PARAMETER_*) to a well known parameter id.
     * 
     * @param wspEncodingVersion The requested wsp encoding version
     * @param parameterType The header type
     * @return A well known parameter id or -1 if not found.
     */
    public static int getWellKnownParameterId(WspEncodingVersion wspEncodingVersion, int parameterType)
    {
        int wellKnownParameterId = -1;

        if (parameterType >= 0)
        {
            switch (wspEncodingVersion)
            {
            case VERSION_1_1:
                wellKnownParameterId = WELL_KNOWN_PARAMETER_ID_WSP_11[parameterType];
                break;
            case VERSION_1_2:
                wellKnownParameterId = WELL_KNOWN_PARAMETER_ID_WSP_12[parameterType];
                break;
                
            case VERSION_1_3:
                wellKnownParameterId = WELL_KNOWN_PARAMETER_ID_WSP_13[parameterType];
                break;
                
            case VERSION_1_4:
            case VERSION_1_5:
                wellKnownParameterId = WELL_KNOWN_PARAMETER_ID_WSP_14[parameterType];
                break;
            
            default:
            }
        }
        
        return wellKnownParameterId;
    }
    
    /**
     * Converts a push app to a WINA "well-known" push app id.
     * 
     * http://www.wapforum.org/wina/push-app-id.htm
     * 
     * @param pushApp The push app
     * @return A well known push app id or -1 if not found.
     */
    public static int getWellKnownPushAppId(String pushApp)
    {
        Integer pushAppIdInt = wspPushAppTypes_.get(pushApp);

        if (pushAppIdInt == null)
        {
            return -1;
        }
        
        return pushAppIdInt.intValue();
    }
    
    /**
     * Writes a "uint8" in wsp format to the given output stream.
     * 
     * @param theOs
     *            Stream to write to
     * @param theValue
     *            Value to write
     */
    public static void writeUint8(OutputStream theOs, int theValue) throws IOException
    {
        theOs.write(theValue);
    }

    /**
     * Writes a "Uintvar" in wsp format to the given output stream.
     * 
     * @param theOs
     *            Stream to write to
     * @param theValue
     *            Value to write
     */
    public static void writeUintvar(OutputStream theOs, long theValue) throws IOException
    {
        int nOctets = 1;
        while ((theValue >> (7 * nOctets)) > 0)
        {
            nOctets++;
        }

        for (int i = nOctets; i > 0; i--)
        {
            byte octet = (byte) (theValue >> (7 * (i - 1)));
            byte byteValue = (byte) (octet & (byte) 0x7f);
            if (i > 1)
            {
                byteValue = (byte) (byteValue | (byte) 0x80);
            }
            theOs.write(byteValue);
        }
    }

    /**
     * Writes a "long integer" in wsp format to the given output stream.
     * 
     * @param theOs
     *            Stream to write to
     * @param theValue
     *            Value to write
     */
    public static void writeLongInteger(OutputStream theOs, long theValue) throws IOException
    {
        int nOctets = 0;
        while ((theValue >> (8 * nOctets)) > 0)
        {
            nOctets++;
        }
        theOs.write((byte) nOctets);

        for (int i = nOctets; i > 0; i--)
        {
            byte octet = (byte) (theValue >> (8 * (i - 1)));
            byte byteValue = (byte) (octet & (byte) (0xff));
            theOs.write(byteValue);
        }
    }

    /**
     * Writes an "integer" in wsp format to the given output stream.
     * 
     * @param theOs
     * @param theValue
     */
    public static void writeInteger(OutputStream theOs, long theValue) throws IOException
    {
        if (theValue < 128)
        {
            writeShortInteger(theOs, (int) theValue);
        }
        else
        {
            writeLongInteger(theOs, theValue);
        }
    }

    /**
     * Writes a "short integer" in wsp format to the given output stream.
     * 
     * @param theOs
     *            Stream to write to
     * @param theValue
     *            Value to write
     */
    public static void writeShortInteger(OutputStream theOs, int theValue) throws IOException
    {
        theOs.write((byte) (theValue | (byte) 0x80));
    }

    public static void writeValueLength(OutputStream theOs, long theValue) throws IOException
    {
        // ShortLength | (Length-quote Length)

        if (theValue <= 30)
        {
            // Short-length
            theOs.write((int) theValue);
        }
        else
        {
            // Length-quote == Octet 31
            theOs.write(31);
            writeUintvar(theOs, theValue);
        }
    }

    /**
     * Writes an "extension media" in pdu format to the given output stream. It
     * currently only handles ASCII chars, but should be extended to work with
     * other charsets.
     * 
     * @param theOs
     *            Stream to write to
     * @param theStr
     *            Text to write
     */
    public static void writeExtensionMedia(OutputStream theOs, String theStr) throws IOException
    {
        theOs.write(theStr.getBytes(StandardCharsets.UTF_8));
        theOs.write((byte) 0x00);
    }

    public static void writeTextString(OutputStream theOs, String theStr) throws IOException
    {
        /*
         * Text-string = [Quote] *TEXT End-of-string ; If the first character in
         * the TEXT is in the range of 128-255, a Quote character must precede
         * it. ; Otherwise the Quote character must be omitted. The Quote is not
         * part of the contents. Quote = <Octet 127> End-of-string = <Octet 0>
         */

    	 byte[] strBytes = theStr.getBytes(StandardCharsets.UTF_8);

        if ((strBytes[0] & 0x80) > 0x00)
        {
            theOs.write(0x7f);
        }

        theOs.write(strBytes);
        theOs.write(0x00);
    }

    public static void writeQuotedString(OutputStream theOs, String theStr) throws IOException
    {
        /*
         * Quoted-string = <Octet 34> *TEXT End-of-string ;The TEXT encodes an
         * RFC2616 Quoted-string with the enclosing quotation-marks <"> removed
         */

        // <Octet 34>
        theOs.write(34);

        theOs.write(theStr.getBytes(StandardCharsets.UTF_8));
        theOs.write(0x00);
    }

    public static void writeTokenText(OutputStream theOs, String theStr) throws IOException
    {
        /*
         * Token-Text = Token End-of-string
         */
        // TODO: Token => RFC2616
        theOs.write(theStr.getBytes(StandardCharsets.UTF_8));
        theOs.write(0x00);
    }

    public static void writeTextValue(OutputStream theOs, String theStr) throws IOException
    {
        /*
         * // No-value | Token-text | Quoted-string
         */
        // FIXME: Verify
        writeQuotedString(theOs, theStr);
    }

    /**
     * Writes a wsp encoded content-type as specified in
     * WAP-230-WSP-20010705-a.pdf.
     * <p>
     * Uses the "constrained media" format. <br>
     * Note! This method can only be used on simple content types (like
     * "text/plain" or "image/gif"). If a more complex content-type is needed
     * (like "image/gif; start=cid; parameter=value;") you must use the
     * MimeContentType class.
     * 
     * @param theOs
     * @param theContentType
     * @throws IOException
     */
    public static void writeContentType(WspEncodingVersion wspEncodingVersion, OutputStream theOs, String theContentType) throws IOException
    {
        int wellKnownContentType = WspUtil.getWellKnownContentTypeId(wspEncodingVersion, theContentType.toLowerCase());

        if (wellKnownContentType == -1)
        {
            writeValueLength(theOs, theContentType.length() + 1);
            writeExtensionMedia(theOs, theContentType);
        }
        else
        {
            writeShortInteger(theOs, wellKnownContentType);
        }
    }

    /**
     * Writes a wsp encoded content-type as specified in
     * WAP-230-WSP-20010705-a.pdf.
     * <p>
     * This method automatically chooses the most compact way to represent the
     * given content type.
     * 
     * @param theOs
     * @param theContentType
     * @throws IOException
     */
    public static void writeContentType(WspEncodingVersion wspEncodingVersion, OutputStream theOs, MimeHeader theContentType) throws IOException
    {
        if (theContentType.getParameters().isEmpty())
        {
            // Simple content type, use "constrained-media" format
            writeContentType(wspEncodingVersion, theOs, theContentType.getValue());
        }
        else
        {
            String theContentTypeStr = theContentType.getValue();
            // Complex, use "content-general-form"
            int wellKnownContentType = WspUtil.getWellKnownContentTypeId(wspEncodingVersion, theContentTypeStr.toLowerCase());

            // Create parameter byte array of
            // well-known-media (integer) or extension media
            // 0 or more parameters
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (wellKnownContentType == -1)
            {
                writeExtensionMedia(baos, theContentType.getValue());
            }
            else
            {
                // well-known-media (integer)
                writeInteger(baos, wellKnownContentType);
            }

            // Add Parameters
            for (MimeHeaderParameter headerParam : theContentType.getParameters()) {
                writeParameter(wspEncodingVersion, baos, headerParam.getName(), headerParam.getValue());
            }
            baos.close();

            // Write to stream

            // content-general-form
            // value length
            writeValueLength(theOs, baos.size());
            // Write parameter byte array
            theOs.write(baos.toByteArray());
        }
    }

    public static void writeTypedValue(WspEncodingVersion wspEncodingVersion, OutputStream os, int wspParamType, String value) throws IOException
    {
        switch (wspParamType)
        {
        // "Used to indicate that the parameter actually have no value,
        // eg, as the parameter "bar" in ";foo=xxx; bar; baz=xyzzy"."
        case WapConstants.WSP_PARAMETER_TYPE_NO_VALUE:
            os.write(0x00);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_TEXT_VALUE:
            writeTextValue(os, value);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_INTEGER_VALUE:
            writeInteger(os, Long.parseLong(value));
            break;

        case WapConstants.WSP_PARAMETER_TYPE_DATE_VALUE:
            /*
             * ; The encoding of dates shall be done in number of seconds from ;
             * 1970-01-01, 00:00:00 GMT.
             */
            Long l = Long.valueOf(value);
            writeLongInteger(os, l.longValue());
            break;

        case WapConstants.WSP_PARAMETER_TYPE_DELTA_SECONDS_VALUE:
            // Integer-Value
            Integer i = Integer.valueOf(value);
            writeInteger(os, i.intValue());
            break;

        case WapConstants.WSP_PARAMETER_TYPE_Q_VALUE:
            // TODO: Implement
            /*
             * ; The encoding is the same as in Uintvar-integer, but with
             * restricted size. When quality factor 0 ; and quality factors with
             * one or two decimal digits are encoded, they shall be multiplied
             * by 100 ; and incremented by one, so that they encode as a
             * one-octet value in range 1-100, ; ie, 0.1 is encoded as 11 (0x0B)
             * and 0.99 encoded as 100 (0x64). Three decimal quality ; factors
             * shall be multiplied with 1000 and incremented by 100, and the
             * result shall be encoded ; as a one-octet or two-octet uintvar,
             * eg, 0.333 shall be encoded as 0x83 0x31. ; Quality factor 1 is
             * the default value and shall never be sent.
             */
            writeTextString(os, value);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_VERSION_VALUE:
            // TODO: Implement
            /*
             * ; The three most significant bits of the Short-integer value are
             * interpreted to encode a major ; version number in the range 1-7,
             * and the four least significant bits contain a minor version ;
             * number in the range 0-14. If there is only a major version
             * number, this is encoded by ; placing the value 15 in the four
             * least significant bits. If the version to be encoded fits these ;
             * constraints, a Short-integer must be used, otherwise a
             * Text-string shall be used.
             */
            writeTextString(os, value);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_URI_VALUE:
            // Text-String
            // TODO: Verify
            /*
             * ; URI value should be encoded per [RFC2616], but service user may
             * use a different format.
             */
            writeTextString(os, value);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_TEXT_STRING:
            writeTextString(os, value);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_WELL_KNOWN_CHARSET:
            // Any-Charset | Integer-Value
            // ; Both are encoded using values from Character Set Assignments
            // table in Assigned Numbers
            // TODO: Implement correctly. Currently we always say "UTF8"
            writeInteger(os, WapConstants.MIB_ENUM_UTF_8);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_FIELD_NAME:
            // Token-text | Well-known-field-name
            // TODO: Implement
            writeTextString(os, value);
            break;

        case WapConstants.WSP_PARAMETER_TYPE_SHORT_INTEGER:
            writeShortInteger(os, Integer.parseInt(value));
            break;

        case WapConstants.WSP_PARAMETER_TYPE_CONSTRAINED_ENCODING:
            // Constrained-Encoding == Content-type
            writeContentType(wspEncodingVersion, os, value);
            break;

        default:
            // TODO: Implement
            writeTextString(os, value);
            break;
        }
    }

    public static void writeParameter(WspEncodingVersion wspEncodingVersion, OutputStream os, String name, String value) throws IOException
    {
        int parameterType = WspUtil.getParameterType(name);
        int wellKnownParameter = WspUtil.getWellKnownParameterId(wspEncodingVersion, parameterType);

        if (wellKnownParameter == -1)
        {
            // Untyped-parameter
            // Token-Text
            writeTokenText(os, name);

            // Untyped-value == Integer-Value | Text-value
            writeTextString(os, value);
        }
        else
        {                        
            // Typed-parameter

            // Well-known-parameter-token == Integer-value
            writeInteger(os, wellKnownParameter);
            // Typed-value
            writeTypedValue(wspEncodingVersion, os, getWspParameterType(wellKnownParameter), value);
        }
    }

    /**
     * Converts from a "multipart/" content type to "vnd.wap..." content type.
     * 
     * @param ct
     * @return
     */
    public static String convertMultipartContentType(String ct)
    {
        if (ct.equalsIgnoreCase("multipart/*"))
        {
            return "application/vnd.wap.multipart.*";
        }
        else if (ct.equalsIgnoreCase("multipart/mixed"))
        {
            return "application/vnd.wap.multipart.mixed";
        }
        else if (ct.equalsIgnoreCase("multipart/form-data"))
        {
            return "application/vnd.wap.multipart.form-data";
        }
        else if (ct.equalsIgnoreCase("multipart/byteranges"))
        {
            return "application/vnd.wap.multipart.byteranges";
        }
        else if (ct.equalsIgnoreCase("multipart/alternative"))
        {
            return "application/vnd.wap.multipart.alternative";
        }
        else if (ct.equalsIgnoreCase("multipart/related"))
        {
            return "application/vnd.wap.multipart.related";
        }
        else
        {
            return ct;
        }
    }
}
