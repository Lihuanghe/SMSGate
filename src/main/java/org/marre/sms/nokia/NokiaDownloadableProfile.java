/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
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
 * ***** END LICENSE BLOCK ***** */
package org.marre.sms.nokia;

import org.marre.sms.SmsUserData;

import com.zx.sms.common.util.StandardCharsets;

/**
 * Nokia Downloadable Profile
 *
 * @author Markus Eriksson
 * @version $Id$
 */
public class NokiaDownloadableProfile extends NokiaMultipartMessage
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6796572895718593625L;
	private String profileName_;
    private byte[] screenSaver_;
    private byte[] ringingTone_;

    public NokiaDownloadableProfile()
    {
    }

    public NokiaDownloadableProfile(String profileName)
    {
        setProfileName(profileName);
    }

    public void setScreenSaver(byte[] bitmapData)
    {
        screenSaver_ = bitmapData;
    }

    public void setScreenSaver(OtaBitmap otaBitmap)
    {
        screenSaver_ = otaBitmap.getBytes();
    }

    public void setProfileName(String profileName)
    {
        profileName_ = profileName;
    }

    public void setRingingTone(byte[] ringingToneData)
    {
        ringingTone_ = ringingToneData;
    }

    private void addProfileName(String profileName)
    {
    }

    public SmsUserData getUserData()
    {
        // Reset message
        clear();

        // Create message
        if (profileName_ != null)
        {
            addMultipart(NokiaItemType.PROFILE_NAME, profileName_.getBytes(StandardCharsets.UTF_16BE));
        }

        if (screenSaver_ != null)
        {
            addMultipart(NokiaItemType.SCREEN_SAVER, screenSaver_);
        }

        if (ringingTone_ != null)
        {
            addMultipart(NokiaItemType.RINGTONE, ringingTone_);
        }

        return super.getUserData();
    }
}
