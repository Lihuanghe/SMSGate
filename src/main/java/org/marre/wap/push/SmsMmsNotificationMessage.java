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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.marre.mime.MimeBodyPart;
import org.marre.sms.SmsUserData;
import org.marre.util.StringUtil;
import org.marre.wap.mms.MmsConstants;
import org.marre.wap.mms.MmsHeaderEncoder;

/**
 * Simple MMS notification message sent over Sms.
 * 
 * @version $Id$
 */
public class SmsMmsNotificationMessage extends SmsWapPushMessage implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3659719878719271285L;
	private static final int DEFAULT_TRANSACTION_ID_LENGTH = 5;
    private static final long DEFAULT_EXPIRY = 3 * 24 * 60 * 60; // 3 days

    protected String transactionId_="";
    protected String from_="";
    protected String subject_="";
    protected int messageClassId_ = MmsConstants.X_MMS_MESSAGE_CLASS_ID_PERSONAL;
    protected final long size_ ;
    protected long expiry_=172800;
    protected final String contentLocation_;

    public SmsMmsNotificationMessage(String contentLocation, long size)
    {
        super();

        contentLocation_ = contentLocation;
        transactionId_ = StringUtil.randString(DEFAULT_TRANSACTION_ID_LENGTH);
        expiry_ = DEFAULT_EXPIRY;
        size_ = size;
    }
    
    protected void writeNotificationTo(OutputStream os) throws IOException
    {
        // X-Mms-Message-Type (m-notification-ind)
        MmsHeaderEncoder.writeHeaderXMmsMessageType(os, MmsConstants.X_MMS_MESSAGE_TYPE_ID_M_NOTIFICATION_IND);
        MmsHeaderEncoder.writeHeaderXMmsTransactionId(os, transactionId_);
        MmsHeaderEncoder.writeHeaderXMmsMmsVersion(os, MmsConstants.X_MMS_MMS_VERSION_1_2);
        MmsHeaderEncoder.writeHeaderContentLocation(os, contentLocation_);
        MmsHeaderEncoder.writeHeaderXMmsExpiryRelative(os, expiry_);
        if ((subject_ != null) && (subject_.length() > 0))
        {
            MmsHeaderEncoder.writeHeaderSubject(os, subject_);
        }

        if ((from_ != null) && (from_.length() > 0))
        {
            MmsHeaderEncoder.writeHeaderFrom(os, from_);
        }
        MmsHeaderEncoder.writeHeaderXMmsMessageClass(os, messageClassId_);
        MmsHeaderEncoder.writeHeaderXMmsMessageSize(os, size_);
       // MmsHeaderEncoder.writeHeaderXMmsExpiryAbsolute(os, expiry_ + System.currentTimeMillis()/1000);
    }

    public void setMessageClass(int messageClassId)
    {
        messageClassId_ = messageClassId;
    }

    public void setSubject(String subject)
    {
        subject_ = subject;
    }

    public void setExpiry(int i)
    {
        expiry_ = i;
    }

    public void setFrom(String string)
    {
        from_ = string;
    }

    public void setTransactionId(String transactionId)
    {
        transactionId_ = transactionId;
    }
    
    public SmsUserData getUserData()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        
        try
        {
            writeNotificationTo(baos);
            baos.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }        
        
        pushMsg_ = new MimeBodyPart(baos.toByteArray(), "application/vnd.wap.mms-message");        
        setXWapApplicationId("x-wap-application:mms.ua");

        return super.getUserData();
    }

	public String getTransactionId_() {
		return transactionId_;
	}

	public String getFrom_() {
		return from_;
	}

	public String getSubject_() {
		return subject_;
	}

	public int getMessageClassId_() {
		return messageClassId_;
	}

	public long getSize_() {
		return size_;
	}

	public long getExpiry_() {
		return expiry_;
	}

	public String getContentLocation_() {
		return contentLocation_;
	}
    
}
