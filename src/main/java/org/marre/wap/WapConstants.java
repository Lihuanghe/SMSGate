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

public final class WapConstants
{    
    public static final int PDU_TYPE_PUSH = 0x06;

    /*
     * IANA assigned charset values
     * http://www.iana.org/assignments/character-sets
     */
    public static final int MIB_ENUM_UTF_8 = 106;

    public static final int WSP_PARAMETER_TYPE_NO_VALUE = 0x01;
    public static final int WSP_PARAMETER_TYPE_TEXT_VALUE = 0x02;
    public static final int WSP_PARAMETER_TYPE_INTEGER_VALUE = 0x03;
    public static final int WSP_PARAMETER_TYPE_DATE_VALUE = 0x04;
    public static final int WSP_PARAMETER_TYPE_DELTA_SECONDS_VALUE = 0x05;
    public static final int WSP_PARAMETER_TYPE_Q_VALUE = 0x06;
    public static final int WSP_PARAMETER_TYPE_VERSION_VALUE = 0x07;
    public static final int WSP_PARAMETER_TYPE_URI_VALUE = 0x08;
    public static final int WSP_PARAMETER_TYPE_TEXT_STRING = 0x09;
    public static final int WSP_PARAMETER_TYPE_WELL_KNOWN_CHARSET = 0x0A;
    public static final int WSP_PARAMETER_TYPE_FIELD_NAME = 0x0B;
    public static final int WSP_PARAMETER_TYPE_SHORT_INTEGER = 0x0C;
    public static final int WSP_PARAMETER_TYPE_CONSTRAINED_ENCODING = 0x0D;

    public static final int PARAMETER_Q = 0x00;
    public static final int PARAMETER_CHARSET = 0x01;
    public static final int PARAMETER_LEVEL = 0x02;
    public static final int PARAMETER_TYPE = 0x03;
    public static final int PARAMETER_NAME = 0x04;
    public static final int PARAMETER_FILENAME = 0x05;
    public static final int PARAMETER_DIFFERENCES = 0x06;
    public static final int PARAMETER_PADDING = 0x07;
    public static final int PARAMETER_TYPE_MULTIPART_RELATED = 0x08;
    public static final int PARAMETER_START_MULTIPART_RELATED = 0x09;
    public static final int PARAMETER_START_INFO_MULTIPART_RELATED = 0x0A;
    public static final int PARAMETER_COMMENT = 0x0B;
    public static final int PARAMETER_DOMAIN = 0x0C;
    public static final int PARAMETER_MAX_AGE = 0x0D;
    public static final int PARAMETER_PATH = 0x0E;
    public static final int PARAMETER_SECURE = 0x0F;
    public static final int PARAMETER_SEC_CONNECTIVITY = 0x10;
    public static final int PARAMETER_MAC_CONNECTIVITY = 0x11;
    public static final int PARAMETER_CREATION_DATE = 0x12;
    public static final int PARAMETER_MODIFICATION_DATE = 0x13;
    public static final int PARAMETER_READ_DATE = 0x14;
    public static final int PARAMETER_SIZE = 0x15;
        
    public static final int HEADER_ACCEPT = 0x00;
    public static final int HEADER_ACCEPT_CHARSET = 0x01;
    public static final int HEADER_ACCEPT_ENCODING = 0x02;
    public static final int HEADER_ACCEPT_LANGUAGE = 0x03;
    public static final int HEADER_ACCEPT_RANGES = 0x04;
    public static final int HEADER_AGE = 0x05;
    public static final int HEADER_ALLOW = 0x06;
    public static final int HEADER_AUTHORIZATION = 0x07;
    public static final int HEADER_CACHE_CONTROL = 0x08;
    public static final int HEADER_CONNECTION = 0x09;
    public static final int HEADER_CONTENT_BASE = 0x0A;
    public static final int HEADER_CONTENT_ENCODING = 0x0B;
    public static final int HEADER_CONTENT_LANGUAGE = 0x0C;
    public static final int HEADER_CONTENT_LENGTH = 0x0D;
    public static final int HEADER_CONTENT_LOCATION = 0x0E;
    public static final int HEADER_CONTENT_MD5 = 0x0F;
    public static final int HEADER_CONTENT_RANGE = 0x10;
    public static final int HEADER_CONTENT_TYPE = 0x11;
    public static final int HEADER_DATE = 0x12;
    public static final int HEADER_ETAG = 0x13;
    public static final int HEADER_EXPIRES = 0x14;
    public static final int HEADER_FROM = 0x15;
    public static final int HEADER_HOST = 0x16;
    public static final int HEADER_IF_MODIFIED_SINCE = 0x17;
    public static final int HEADER_IF_MATCH = 0x18;
    public static final int HEADER_IF_NONE_MATCH = 0x19;
    public static final int HEADER_IF_RANGE = 0x1A;
    public static final int HEADER_IF_UNMODIFIED_SINCE = 0x1B;
    public static final int HEADER_LAST_MODIFIED = 0x1C;
    public static final int HEADER_LOCATION = 0x1D;
    public static final int HEADER_MAX_FORWARDS = 0x1E;
    public static final int HEADER_PRAGMA = 0x1F;
    public static final int HEADER_PROXY_AUTHENTICATE = 0x20;
    public static final int HEADER_PROXY_AUTHORIZATION = 0x21;
    public static final int HEADER_PUBLIC = 0x22;
    public static final int HEADER_RANGE = 0x23;
    public static final int HEADER_REFERER = 0x24;
    public static final int HEADER_RETRY_AFTER = 0x25;
    public static final int HEADER_SERVER = 0x26;
    public static final int HEADER_TRANSFER_ENCODING = 0x27;
    public static final int HEADER_UPGRADE = 0x28;
    public static final int HEADER_USER_AGENT = 0x29;
    public static final int HEADER_VARY = 0x2A;
    public static final int HEADER_VIA = 0x2B;
    public static final int HEADER_WARNING = 0x2C;
    public static final int HEADER_WWW_AUTHENTICATE = 0x2D;
    public static final int HEADER_CONTENT_DISPOSITION = 0x2E;
    public static final int HEADER_X_WAP_APPLICATION_ID = 0x2F;
    public static final int HEADER_X_WAP_CONTENT_URI = 0x30;
    public static final int HEADER_X_WAP_INITIATOR_URI = 0x31;
    public static final int HEADER_ACCEPT_APPLICATION = 0x32;
    public static final int HEADER_BEARER_INDICATION = 0x33;
    public static final int HEADER_PUSH_FLAG = 0x34;
    public static final int HEADER_PROFILE = 0x35;
    public static final int HEADER_PROFILE_DIFF = 0x36;
    public static final int HEADER_PROFILE_WARNING = 0x37;
    public static final int HEADER_EXPECT = 0x38;   
    public static final int HEADER_TE = 0x39;
    public static final int HEADER_TRAILER = 0x3A;
    public static final int HEADER_X_WAP_TOD = 0x3B;
    public static final int HEADER_CONTENT_ID = 0x3C;
    public static final int HEADER_SET_COOKIE = 0x3D;
    public static final int HEADER_COOKIE = 0x3E;
    public static final int HEADER_ENCODING_VERSION = 0x3F;
    public static final int HEADER_X_WAP_SECURITY = 0x40;
        
    private WapConstants()
    {
    }    
}
