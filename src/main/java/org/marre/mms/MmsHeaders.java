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
package org.marre.mms;

import java.util.Date;

import org.marre.util.StringUtil;
import org.marre.wap.mms.MmsConstants;

public class MmsHeaders
{
    private static final int DEFAULT_TRANSACTION_ID_LENGTH = 6;
    
    protected int    messageTypeId_ = MmsConstants.X_MMS_MESSAGE_TYPE_ID_M_SEND_REQ;
    protected String transactionId_;
    protected int    versionId_     = MmsConstants.X_MMS_MMS_VERSION_ID_1_0;
    protected String subject_;
    protected String from_;
    protected String to_;
    protected String messageId_;
    protected Date date;
    
    public MmsHeaders()
    {
        transactionId_ = StringUtil.randString(DEFAULT_TRANSACTION_ID_LENGTH);
    }
    
    public int getMessageType()
    {
        return messageTypeId_;
    }
    
    public void setMessageType(int msgTypeId)
    {
        messageTypeId_ = msgTypeId;
    }
    
    public String getTransactionId()
    {
        return transactionId_;
    }
    
    public void setTransactionId(String transactionId)
    {
        transactionId_ = transactionId;
    }
    
    public int getVersion()
    {
        return versionId_;
    }
    
    public void setVersion(int versionId)
    {
        versionId_ = versionId;
    }
    
    public String getSubject()
    {
        return subject_;
    }
    
    public void setSubject(String subject)
    {
        subject_ = subject;
    }
    
    public String getFrom()
    {
        return from_;
    }
    
    public void setFrom(String from)
    {
        from_ = from;
    }
    
    public String getTo()
    {
        return to_;
    }
    
    public void setTo(String to)
    {
        to_ = to;
    }

	public String getMessageId() {
		return messageId_;
	}

	public void setMessageId(String messageId_) {
		this.messageId_ = messageId_;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}    
    
	
}
