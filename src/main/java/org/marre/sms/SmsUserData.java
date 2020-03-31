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
package org.marre.sms;

import com.zx.sms.common.GlobalConstance;

/**
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public class SmsUserData
{
    /** The actual user data. */
    protected final byte[] data_;
    
    /** Length of data, in octets or septets depending on the dcs. */
    protected final int length_;
    
    /** Data Coding Scheme for this user data. */
    protected final AbstractSmsDcs dcs_;
    
    public SmsUserData(byte[] userData, int userDataLength, AbstractSmsDcs dataCodingScheme)
    {
        data_ = userData;
        length_ = userDataLength;
        dcs_ = dataCodingScheme;
    }
    
    public SmsUserData(byte[] userData, AbstractSmsDcs dataCodingScheme)
    {
        data_ = userData;
        length_ = userData.length;
        dcs_ = dataCodingScheme;
    }
    
    public SmsUserData(byte[] userData)
    {
        data_ = userData;
        length_ = userData.length;
        dcs_ = GlobalConstance.defaultmsgfmt;
    }
    
    public byte[] getData()
    {
        return data_;
    }
    
    /**
     * Returns the length of the user data field.
     * 
     * This can be in characters or byte depending on the message (DCS). If
     * message is 7 bit coded the length is given in septets. If 8bit or UCS2
     * the length is in octets.
     * 
     * @return The length
     */
    public int getLength()
    {
        return length_;
    }
    
    /**
     * Returns the data coding scheme.
     * 
     * @return The dcs
     */
    public AbstractSmsDcs getDcs()
    {
        return dcs_;
    }
}
