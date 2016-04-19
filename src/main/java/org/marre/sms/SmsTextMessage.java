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

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.zx.sms.common.util.CMPPCommonUtil;

/**
 * Represents a text message.
 * <p>
 * The text can be sent in unicode (max 70 chars/SMS), 8-bit (max 140 chars/SMS)
 * or GSM encoding (max 160 chars/SMS).
 *
 * @author Markus Eriksson
 * @version $Id$
 */
public class SmsTextMessage extends SmsConcatMessage implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2655575183111164853L;
	private String text_;
    private SmsDcs dcs_;
    
    /**
     * Creates an SmsTextMessage with the given dcs.
     * 
     * @param msg The message
     * @param dcs The data coding scheme
     */
    public SmsTextMessage(String msg, SmsDcs dcs)
    {
        setText(msg, dcs);
    }
    
    /**
     * Creates an SmsTextMessage with the given alphabet and message class.
     *
     * @param msg The message
     * @param alphabet The alphabet
     * @param messageClass The messageclass
     */
    public SmsTextMessage(String msg, SmsAlphabet alphabet, SmsMsgClass messageClass)
    { 
        this(msg, SmsDcs.getGeneralDataCodingDcs(alphabet, messageClass));
    }

    /**
     * Creates an SmsTextMessage with default 7Bit GSM Alphabet
     *
     * @param msg The message
     */
    public SmsTextMessage(String msg)
    {
    	if(haswidthChar(msg))
    		 setText(msg, SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.UCS2, SmsMsgClass.CLASS_UNKNOWN));
    	else
    		 setText(msg, SmsDcs.getGeneralDataCodingDcs(SmsAlphabet.ASCII, SmsMsgClass.CLASS_UNKNOWN));
    }
    
    /**
     * Returns the text message. 
     */
    public String getText()
    {
        return text_;
    }
    
    /**
     * Sets the text.
     * 
     * @param text
     */
    public void setText(String text)
    {
        if (text == null)
        {
            throw new IllegalArgumentException("Text cannot be null, use an empty string instead.");
        }
        
        text_ = text;
    }

    /**
     * Sets the text.
     * 
     * @param text
     */
    public void setText(String text, SmsDcs dcs)
    {
        // Check input for null
        if (text == null)
        {
        	 text_ = "";
        }
        
        if (dcs == null)
        {
            throw new IllegalArgumentException("dcs cannot be null.");
        }
        
        text_ = text;
        dcs_ = dcs;
    }
    
    /**
     * Returns the dcs.
     */
    public SmsDcs getDcs()
    {
        return dcs_;
    }

    /**
     * Returns the user data.
     * @return user data
     */
    public SmsUserData getUserData()
    {
        SmsUserData ud;
        
        switch (dcs_.getAlphabet())
        {
        case GSM:
        	byte[] bs = SmsPduUtil.getSeptets(text_);
            ud = new SmsUserData(bs, bs.length*8/7, dcs_);
            break;
        case ASCII:
        case LATIN1:
            ud = new SmsUserData(text_.getBytes(CMPPCommonUtil.switchCharset(dcs_.getAlphabet())), text_.length(), dcs_);
            break;

        case UCS2:
            ud = new SmsUserData(text_.getBytes(CMPPCommonUtil.switchCharset(dcs_.getAlphabet())), text_.length() * 2, dcs_);
            break;

        default:
            ud = null;
            break;
        }

        return ud;
    }

    /**
     * Returns null.
     */
    public SmsUdhElement[] getUdhElements()
    {
        return null;
    }
    
	
	private static boolean haswidthChar(String content) {
		if (StringUtils.isEmpty(content))
			return false;

		byte[] bytes = content.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			// 判断最高位是否为1
			if ((bytes[i] & (byte) 0x80) == (byte) 0x80) {
				return true;
			}
		}
		return false;
	}
}
