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
package org.marre.wap.mms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.marre.mime.MimeHeader;
import org.marre.util.StringUtil;
import org.marre.wap.WspEncodingVersion;
import org.marre.wap.WspUtil;

/**
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public final class MmsHeaderEncoder
{
    private MmsHeaderEncoder()
    {
    }

    /**
     * Writes a wsp encoded content-location header as specified in
     * WAP-230-WSP-20010705-a.pdf.
     * 
     * @param os
     * @param contentLocation
     * @throws IOException
     */
    public static void writeHeaderContentLocation(OutputStream os, String contentLocation) throws IOException
    {
        // TODO: Verify
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_CONTENT_LOCATION);
        WspUtil.writeTextString(os, contentLocation);
    }

    public static void writeHeaderContentType(WspEncodingVersion wspEncodingVersion, OutputStream os, String contentLocation) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_CONTENT_TYPE);
        WspUtil.writeContentType(wspEncodingVersion, os, contentLocation);
    }

    public static void writeHeaderContentType(WspEncodingVersion wspEncodingVersion, OutputStream os, MimeHeader contentType) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_CONTENT_TYPE);
        WspUtil.writeContentType(wspEncodingVersion, os, contentType);
    }

    public static void writeEncodedStringValue(OutputStream os, String stringValue) throws IOException
    {
        // TODO: Charset...
        WspUtil.writeTextString(os, stringValue);
    }

    public static void writeHeaderXMmsMessageType(OutputStream os, int messageTypeId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_MESSAGE_TYPE);
        WspUtil.writeShortInteger(os, messageTypeId);
    }

    public static void writeHeaderXMmsMessageType(OutputStream os, String messageType) throws IOException
    {
        int messageTypeId = StringUtil.findString(MmsConstants.X_MMS_MESSAGE_TYPE_NAMES, messageType.toLowerCase());
        if (messageTypeId != -1)
        {
            writeHeaderXMmsMessageType(os, messageTypeId);
        }
    }

    public static void writeHeaderXMmsTransactionId(OutputStream os, String transactionId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_TRANSACTION_ID);
        WspUtil.writeTextString(os, transactionId);
    }

    public static void writeHeaderXMmsMmsVersion(OutputStream os, int versionId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_MMS_VERSION);

        switch (versionId)
        {
        case MmsConstants.X_MMS_MMS_VERSION_ID_1_0:
        default:
            WspUtil.writeShortInteger(os, 0x10);
            break;
        }
    }

    public static void writeHeaderXMmsMmsVersion(OutputStream os, String version) throws IOException
    {
        int versionId = StringUtil.findString(MmsConstants.X_MMS_MMS_VERSION_NAMES, version.toLowerCase());
        if (versionId != -1)
        {
            writeHeaderXMmsMessageType(os, versionId);
        }
    }

    public static void writeHeaderDate(OutputStream os, Date date) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_DATE);
        long time = date.getTime()/1000;
        WspUtil.writeLongInteger(os, time);
    }

    public static void writeHeaderFrom(OutputStream os, String from) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_FROM);

        if (from == null)
        {
           // WspUtil.writeValueLength(os, 1);
           // WspUtil.writeShortInteger(os, MmsConstants.FROM_INSERT_ADDRESS);
        	from = "Unknow";
        }
        else
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Write data to baos
            WspUtil.writeShortInteger(baos, MmsConstants.FROM_ADDRESS_PRESENT);
            MmsHeaderEncoder.writeEncodedStringValue(baos, from);
            baos.close();

            WspUtil.writeValueLength(os, baos.size());
            os.write(baos.toByteArray());
        }
    }

    public static void writeHeaderSubject(OutputStream os, String subject) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_SUBJECT);
        MmsHeaderEncoder.writeEncodedStringValue(os, subject);
    }
    
    public static void writeHeaderMessageId(OutputStream os, String message) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_MESSAGE_ID);
        MmsHeaderEncoder.writeEncodedStringValue(os, message);
    }

    public static void writeHeaderTo(OutputStream os, String receiver) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_TO);
        MmsHeaderEncoder.writeEncodedStringValue(os, receiver);
    }

    public static void writeHeaderCc(OutputStream os, String receiver) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_CC);
        MmsHeaderEncoder.writeEncodedStringValue(os, receiver);
    }

    public static void writeHeaderBcc(OutputStream os, String receiver) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_BCC);
        MmsHeaderEncoder.writeEncodedStringValue(os, receiver);
    }

    public static void writeHeaderXMmsReadReply(OutputStream os, int readReplyId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_READ_REPLY);
        WspUtil.writeShortInteger(os, readReplyId);
    }

    public static void writeHeaderXMmsReadReply(OutputStream os, String readReply) throws IOException
    {
        int readReplyId = StringUtil.findString(MmsConstants.X_MMS_READ_REPLY_NAMES, readReply.toLowerCase());
        if (readReplyId != -1)
        {
            writeHeaderXMmsReadReply(os, readReplyId);
        }
    }

    public static void writeHeaderXMmsPriority(OutputStream os, int priorityId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_PRIORITY);
        WspUtil.writeShortInteger(os, priorityId);
    }

    public static void writeHeaderXMmsPriority(OutputStream os, String priority_) throws IOException
    {
        int priorityId = StringUtil.findString(MmsConstants.X_MMS_PRIORITY_NAMES, priority_.toLowerCase());
        if (priorityId != -1)
        {
            writeHeaderXMmsPriority(os, priorityId);
        }
    }

    public static void writeHeaderXMmsStatus(OutputStream os, int status) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_STATUS);
        WspUtil.writeShortInteger(os, status);
    }

    public static void writeHeaderXMmsStatus(OutputStream os, String status) throws IOException
    {
        int statusId = StringUtil.findString(MmsConstants.X_MMS_STATUS_NAMES, status.toLowerCase());
        if (statusId != -1)
        {
            writeHeaderXMmsStatus(os, statusId);
        }
    }

    public static void writeHeaderXMmsMessageClass(OutputStream os, int messageClassId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_MESSAGE_CLASS);
        WspUtil.writeShortInteger(os, messageClassId);
    }

    public static void writeHeaderXMmsMessageClass(OutputStream os, String messageClass) throws IOException
    {
        int messageClassId = StringUtil.findString(MmsConstants.X_MMS_MESSAGE_CLASS_NAMES, messageClass
                .toLowerCase());
        if (messageClassId != -1)
        {
            writeHeaderXMmsMessageClass(os, messageClassId);
        }
    }

    public static void writeHeaderXMmsMessageSize(OutputStream os, long messageSize) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_MESSAGE_SIZE);
        WspUtil.writeLongInteger(os, messageSize);
    }

    public static void writeHeaderXMmsExpiryAbsolute(OutputStream os, long expiry) throws IOException
    {
        // Expiry-value = Value-length (Absolute-token Date-value |
        // Relative-token Delta-seconds-value)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // (Absolute-token Date-value)
        WspUtil.writeShortInteger(baos, MmsConstants.ABSOLUTE_TOKEN);
        WspUtil.writeLongInteger(baos, expiry);

        baos.close();

        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_EXPIRY);
        WspUtil.writeValueLength(os, baos.size());
        os.write(baos.toByteArray());
    }

    public static void writeHeaderXMmsExpiryAbsolute(OutputStream os, Date expiry) throws IOException
    {
        writeHeaderXMmsExpiryAbsolute(os, expiry.getTime());
    }

    public static void writeHeaderXMmsExpiryRelative(OutputStream os, long expiry) throws IOException
    {
        // Expiry-value = Value-length (Absolute-token Date-value |
        // Relative-token Delta-seconds-value)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        WspUtil.writeShortInteger(baos, MmsConstants.RELATIVE_TOKEN);
        WspUtil.writeLongInteger(baos, expiry);

        baos.close();

        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_EXPIRY);
        WspUtil.writeValueLength(os, baos.size());
        os.write(baos.toByteArray());
    }

    public static void writeHeaderXMmsSenderVisibility(OutputStream os, int visibilityId) throws IOException
    {
        WspUtil.writeShortInteger(os, MmsConstants.HEADER_ID_X_MMS_SENDER_VISIBILITY);
        WspUtil.writeShortInteger(os, visibilityId);
    }

    public static void writeHeaderXMmsSenderVisibility(OutputStream os, String newVisabilityId) throws IOException
    {
        int visibilityId = StringUtil.findString(MmsConstants.X_MMS_SENDER_VISIBILITY_NAMES, newVisabilityId
                .toLowerCase());

        if (visibilityId != -1)
        {
            writeHeaderXMmsSenderVisibility(os, visibilityId);
        }
    }

    public static void writeApplicationHeader(OutputStream os, String name, String value) throws IOException
    {
        WspUtil.writeTokenText(os, name);
        WspUtil.writeTextString(os, value);
    }
}
