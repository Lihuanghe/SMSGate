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

public final class WbxmlConstants
{
    /**
     * Change the code page for the current token state. Followed by a single
     * u_int8 indicating the new code page number.
     */
    public static final byte TOKEN_SWITCH_PAGE = 0x00;

    /** Indicates the end of an attribute list or the end of an element. */
    public static final byte TOKEN_END = 0x01;

    /**
     * A character entity. Followed by a mb_u_int32 encoding the character
     * entity number.
     */
    public static final byte TOKEN_ENTITY = 0x02;

    /** Inline string. Followed by a termstr. */
    public static final byte TOKEN_STR_I = 0x03;

    /**
     * An unknown attribute name, or unknown tag posessing no attributes or
     * content.Followed by a mb_u_int32 that encodes an offset into the string
     * table.
     */
    public static final byte TOKEN_LITERAL = 0x04;

    /**
     * Inline string document-type-specific extension token. Token is followed
     * by a termstr.
     */
    public static final byte TOKEN_EXT_I_0 = 0x40;

    /**
     * Inline string document-type-specific extension token. Token is followed
     * by a termstr.
     */
    public static final byte TOKEN_EXT_I_1 = 0x41;

    /**
     * Inline string document-type-specific extension token. Token is followed
     * by a termstr.
     */
    public static final byte TOKEN_EXT_I_2 = 0x42;

    /** Processing instruction. */
    public static final byte TOKEN_PI = 0x43;

    /** An unknown tag posessing content but no attributes. */
    public static final byte TOKEN_LITERAL_C = 0x44;

    /**
     * Inline integer document-type-specific extension token. Token is followed
     * by a mb_u_int32.
     */
    public static final byte TOKEN_EXT_T_0 = (byte) 0x80;

    /**
     * Inline integer document-type-specific extension token. Token is followed
     * by a mb_u_int32.
     */
    public static final byte TOKEN_EXT_T_1 = (byte) 0x81;

    /**
     * Inline integer document-type-specific extension token. Token is followed
     * by a mb_u_int32.
     */
    public static final byte TOKEN_EXT_T_2 = (byte) 0x82;

    /**
     * String table reference. Followed by a mb_u_int32 encoding a byte offset
     * from the beginning of the string table.
     */
    public static final byte TOKEN_STR_T = (byte) 0x83;

    /** An unknown tag posessing attributes but no content. */
    public static final byte TOKEN_LITERAL_A = (byte) 0x84;

    /** Single-byte document-type-specific extension token. */
    public static final byte TOKEN_EXT_0 = (byte) 0xC0;

    /** Single-byte document-type-specific extension token. */
    public static final byte TOKEN_EXT_1 = (byte) 0xC1;

    /** Single-byte document-type-specific extension token. */
    public static final byte TOKEN_EXT_2 = (byte) 0xC2;

    /** Opaque document-type-specific data. */
    public static final byte TOKEN_OPAQ = (byte) 0xC3;

    /** An unknown tag posessing both attributes and content. */
    public static final byte TOKEN_LITERAL_AC = (byte) 0xC4;

    /** Tag contains content. */
    public static final byte TOKEN_KNOWN_C = (byte) 0x40;

    /** Tag contains attributes. */
    public static final byte TOKEN_KNOWN_A = (byte) 0x80;

    /** Tag contains attributes. */
    public static final byte TOKEN_KNOWN_AC = (byte) 0xC0;

    /** Tag contains attributes. */
    public static final byte TOKEN_KNOWN = (byte) 0x00;

    public static final String[] KNOWN_PUBLIC_DOCTYPES = {
            // 0 String table index follows; public identifier is encoded as a literal
            // in the string table.
            // 1 Unknown or missing public identifier.
            "-//WAPFORUM//DTD WML 1.0//EN", // (WML 1.0)
            "-//WAPFORUM//DTD WTA 1.0//EN", // (Deprecated - WTA Event 1.0)
            "-//WAPFORUM//DTD WML 1.1//EN", // (WML 1.1)
            "-//WAPFORUM//DTD SI 1.0//EN", // (Service Indication 1.0)
            "-//WAPFORUM//DTD SL 1.0//EN", // (Service Loading 1.0)
            "-//WAPFORUM//DTD CO 1.0//EN", // (Cache Operation 1.0)
            "-//WAPFORUM//DTD CHANNEL 1.1//EN", // (Channel 1.1)
            "-//WAPFORUM//DTD WML 1.2//EN", // (WML 1.2)
            "-//WAPFORUM//DTD WML 1.3//EN", // (WML 1.3)
            "-//WAPFORUM//DTD PROV 1.0//EN", // (Provisioning 1.0)
            "-//WAPFORUM//DTD WTA-WML 1.2//EN", // (WTA-WML 1.2)
            "-//WAPFORUM//DTD EMN 1.0//EN", // (Email Notification 1.0 WAP-297)
            "-//OMA//DTD DRMREL 1.0//EN", // (DRM REL 1.0)
    };
    
    private WbxmlConstants()
    {
        // Utility class
    }
}
