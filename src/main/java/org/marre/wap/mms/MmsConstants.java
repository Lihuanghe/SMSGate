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

/**
 * @author Markus Eriksson
 */
public final class MmsConstants
{
    public static final int HEADER_ID_BCC = 0x01;
    public static final int HEADER_ID_CC = 0x02;
    public static final int HEADER_ID_X_MMS_CONTENT_LOCATION = 0x03;
    public static final int HEADER_ID_CONTENT_TYPE = 0x04;
    public static final int HEADER_ID_DATE = 0x05;
    public static final int HEADER_ID_X_MMS_DELIVERY_REPORT = 0x06;
    public static final int HEADER_ID_X_MMS_DELIVERY_TIME = 0x07;
    public static final int HEADER_ID_X_MMS_EXPIRY = 0x08;
    public static final int HEADER_ID_FROM = 0x09;
    public static final int HEADER_ID_X_MMS_MESSAGE_CLASS = 0x0A;
    public static final int HEADER_ID_MESSAGE_ID = 0x0B;
    public static final int HEADER_ID_X_MMS_MESSAGE_TYPE = 0x0C;
    public static final int HEADER_ID_X_MMS_MMS_VERSION = 0x0D;
    public static final int HEADER_ID_X_MMS_MESSAGE_SIZE = 0x0E;
    public static final int HEADER_ID_X_MMS_PRIORITY = 0x0F;

    public static final int HEADER_ID_X_MMS_READ_REPLY = 0x10;
    public static final int HEADER_ID_X_MMS_REPORT_ALLOWED = 0x11;
    public static final int HEADER_ID_X_MMS_RESPONSE_STATUS = 0x12;
    public static final int HEADER_ID_X_MMS_RESPONSE_TEXT = 0x13;
    public static final int HEADER_ID_X_MMS_SENDER_VISIBILITY = 0x14;
    public static final int HEADER_ID_X_MMS_STATUS = 0x15;
    public static final int HEADER_ID_SUBJECT = 0x16;
    public static final int HEADER_ID_TO = 0x17;
    public static final int HEADER_ID_X_MMS_TRANSACTION_ID = 0x18;

    public static final String[] HEADER_NAMES = {null, "bcc", "cc", "x-mms-content-location", "content-type", "date",
            "x-mms-delivery-report", "x-mms-delivery-time", "x-mms-expiry", "from", "x-mms-message-class",
            "message-id", "x-mms-message-type", "x-mms-mms-version", "x-mms-message-size", "x-mms-priority",
            "x-mms-read-reply", "x-mms-report-allowed", "x-mms-response-status", "x-mms-response-text",
            "x-mms-sender-visibility", "x-mms-status", "subject", "to", "x-mms-transaction-id"};

    public static final int X_MMS_MMS_VERSION_1_3                 = ((1 << 4) | 3);
    public static final int X_MMS_MMS_VERSION_1_2                 = ((1 << 4) | 2);
    public static final int X_MMS_MMS_VERSION_1_1                 = ((1 << 4) | 1);
    public static final int X_MMS_MMS_VERSION_1_0                 = ((1 << 4) | 0);

    public static final int X_MMS_MESSAGE_TYPE_ID_M_SEND_REQ = 0;
    public static final int X_MMS_MESSAGE_TYPE_ID_M_SEND_CONF = 1;
    public static final int X_MMS_MESSAGE_TYPE_ID_M_NOTIFICATION_IND = 2;
    public static final int X_MMS_MESSAGE_TYPE_ID_M_NOTIFYRESP_IND = 3;
    public static final int X_MMS_MESSAGE_TYPE_ID_M_RETRIEVE_CONF = 4;
    public static final int X_MMS_MESSAGE_TYPE_ID_M_ACKNOWLEDGE_IND = 5;
    public static final int X_MMS_MESSAGE_TYPE_ID_M_DELIVERY_IND = 6;

    public static final String[] X_MMS_MESSAGE_TYPE_NAMES = {"m-send-req", "m-send-conf", "m-notification-ind",
            "m-notifyresp-ind", "m-retrieve-conf", "m-acknowledge-ind", "m-delivery-ind"};

    public static final int FROM_ADDRESS_PRESENT = 0x00;
    public static final int FROM_INSERT_ADDRESS = 0x01;

    public static final int EXPIRY_TIME_ABSOLUTE = 0x00;
    public static final int EXPIRY_TIME_RELATIVE = 0x01;

    public static final short ABSOLUTE_TOKEN = 0x00;
    public static final short RELATIVE_TOKEN = 0x01;

    public static final int X_MMS_READ_REPLY_ID_YES = 0;
    public static final int X_MMS_READ_REPLY_ID_NO = 1;

    public static final String[] X_MMS_READ_REPLY_NAMES = {"yes", "no"};

    public static final int X_MMS_PRIORITY_ID_LOW = 0;
    public static final int X_MMS_PRIORITY_ID_NORMAL = 1;
    public static final int X_MMS_PRIORITY_ID_HIGH = 2;

    public static final String[] X_MMS_PRIORITY_NAMES = {"low", "normal", "high"};

    public static final int X_MMS_STATUS_ID_EXPIRED = 0;
    public static final int X_MMS_STATUS_ID_RETRIEVED = 1;
    public static final int X_MMS_STATUS_ID_REJECTED = 2;
    public static final int X_MMS_STATUS_ID_DEFERRED = 3;
    public static final int X_MMS_STATUS_ID_UNRECOGNISED = 4;

    public static final String[] X_MMS_STATUS_NAMES = {"expired", "retrieved", "rejected", "deferred", "unrecognised"};

    public static final int X_MMS_MESSAGE_CLASS_ID_PERSONAL = 0;
    public static final int X_MMS_MESSAGE_CLASS_ID_ADVERTISMENT = 1;
    public static final int X_MMS_MESSAGE_CLASS_ID_INFORMATIONAL = 2;
    public static final int X_MMS_MESSAGE_CLASS_ID_AUTO = 3;

    public static final String[] X_MMS_MESSAGE_CLASS_NAMES = {"personal", "advertisment", "informational", "auto"};

    public static final int X_MMS_SENDER_VISIBILITY_ID_HIDE = 0;
    public static final int X_MMS_SENDER_VISIBILITY_ID_SHOW = 1;

    public static final String[] X_MMS_SENDER_VISIBILITY_NAMES = {"hide", "show"};

    private MmsConstants()
    {
    }
}
