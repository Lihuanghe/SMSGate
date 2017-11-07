package com.zx.sms.codec.smpp;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;

/**
 * All constants defined for the SMPP protocol.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class SmppConstants {

    //
    // SMPP Data Types
    //
    public static final int DATA_TYPE_C_OCTET_STRING = 0;
    public static final int DATA_TYPE_OCTET_STRING = 1;
    public static final int DATA_TYPE_INTEGER = 2;


    public static final int PDU_INT_LENGTH = 4;
    public static final int PDU_HEADER_LENGTH = 16;
    public static final int PDU_CMD_ID_RESP_MASK = 0x80000000;  // 31st bit set to true
    public static final Address EMPTY_ADDRESS = new Address();

    public static final byte VERSION_3_3 = 0x33;
    public static final byte VERSION_3_4 = 0x34;
    public static final byte VERSION_5_0 = 0x50;

    public static final int DEFAULT_WINDOW_SIZE = 1;
    public static final long DEFAULT_WINDOW_WAIT_TIMEOUT = 60000;
    public static final long DEFAULT_WRITE_TIMEOUT = 0; // For backwards compatibility, default to no timeout
    public static final long DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final long DEFAULT_BIND_TIMEOUT = 5000;
    public static final long DEFAULT_REQUEST_EXPIRY_TIMEOUT = -1;   // disabled
    public static final long DEFAULT_WINDOW_MONITOR_INTERVAL = -1;  // disabled
    public static final int DEFAULT_SERVER_MAX_CONNECTION_SIZE = 100;
    public static final boolean DEFAULT_SERVER_NON_BLOCKING_SOCKETS_ENABLED = true;
    public static final boolean DEFAULT_SERVER_REUSE_ADDRESS = true;

    //
    // SUBMIT_MULTI destination type flags
    //
    //public static final int SME_ADDRESS = 1;
    //public static final int DISTRIBUTION_LIST_NAME = 2;

    //
    // SMPP Command ID (Requests)
    //
    public static final int CMD_ID_BIND_RECEIVER = 0x00000001;
    public static final int CMD_ID_BIND_TRANSMITTER = 0x00000002;
    public static final int CMD_ID_QUERY_SM = 0x00000003;
    public static final int CMD_ID_SUBMIT_SM = 0x00000004;
    public static final int CMD_ID_DELIVER_SM = 0x00000005;
    public static final int CMD_ID_UNBIND = 0x00000006;
    public static final int CMD_ID_REPLACE_SM = 0x00000007;
    public static final int CMD_ID_CANCEL_SM = 0x00000008;
    public static final int CMD_ID_BIND_TRANSCEIVER = 0x00000009;
    public static final int CMD_ID_OUTBIND = 0x0000000B;
    public static final int CMD_ID_ENQUIRE_LINK = 0x00000015;
    public static final int CMD_ID_SUBMIT_MULTI = 0x00000021;
    public static final int CMD_ID_ALERT_NOTIFICATION = 0x00000102;
    public static final int CMD_ID_DATA_SM = 0x00000103;
    public static final int CMD_ID_BROADCAST_SM = 0x00000111;
    public static final int CMD_ID_QUERY_BROADCAST_SM = 0x00000112;
    public static final int CMD_ID_CANCEL_BROADCAST_SM = 0x00000113;

    //
    // SMPP Command ID (Responses)
    //
    public static final int CMD_ID_GENERIC_NACK = 0x80000000;
    public static final int CMD_ID_BIND_RECEIVER_RESP = 0x80000001;
    public static final int CMD_ID_BIND_TRANSMITTER_RESP = 0x80000002;
    public static final int CMD_ID_QUERY_SM_RESP = 0x80000003;
    public static final int CMD_ID_SUBMIT_SM_RESP = 0x80000004;
    public static final int CMD_ID_DELIVER_SM_RESP = 0x80000005;
    public static final int CMD_ID_UNBIND_RESP = 0x80000006;
    public static final int CMD_ID_REPLACE_SM_RESP = 0x80000007;
    public static final int CMD_ID_CANCEL_SM_RESP = 0x80000008;
    public static final int CMD_ID_BIND_TRANSCEIVER_RESP = 0x80000009;
    public static final int CMD_ID_ENQUIRE_LINK_RESP = 0x80000015;
    public static final int CMD_ID_SUBMIT_MULTI_RESP = 0x80000021;
    public static final int CMD_ID_DATA_SM_RESP = 0x80000103;
    public static final int CMD_ID_BROADCAST_SM_RESP = 0x80000111;
    public static final int CMD_ID_QUERY_BROADCAST_SM_RESP = 0x80000112;
    public static final int CMD_ID_CANCEL_BROADCAST_SM_RESP = 0x80000113;

    //
    // Optional TLV Tags
    //
    public static final short TAG_SOURCE_TELEMATICS_ID = 0x0010;
    public static final short TAG_PAYLOAD_TYPE = 0x0019;
    public static final short TAG_PRIVACY_INDICATOR = 0x0201;
    public static final short TAG_USER_MESSAGE_REFERENCE = 0x0204;
    public static final short TAG_USER_RESPONSE_CODE = 0x0205;
    public static final short TAG_SOURCE_PORT = 0x020A;
    public static final short TAG_DESTINATION_PORT = 0x020B;
    public static final short TAG_SAR_MSG_REF_NUM = 0x020C;
    public static final short TAG_LANGUAGE_INDICATOR = 0x020D;
    public static final short TAG_SAR_TOTAL_SEGMENTS = 0x020E;
    public static final short TAG_SAR_SEGMENT_SEQNUM = 0x020F;
    public static final short TAG_SOURCE_SUBADDRESS = 0x0202;
    public static final short TAG_DEST_SUBADDRESS = 0x0203;
    public static final short TAG_CALLBACK_NUM = 0x0381;
    public static final short TAG_MESSAGE_PAYLOAD = 0x0424;
    // SC Interface Version
    public static final short TAG_SC_INTERFACE_VERSION = 0x0210;
    // Display Time
    public static final short TAG_DISPLAY_TIME = 0x1201;
    // Validity Information
    public static final short TAG_MS_VALIDITY = 0x1204;
    // DPF Result
    public static final short TAG_DPF_RESULT = 0x0420;
    // Set DPF
    public static final short TAG_SET_DPF = 0x0421;
    // MS Availability Status
    public static final short TAG_MS_AVAIL_STATUS = 0x0422;
    // Network Error Code
    public static final short TAG_NETWORK_ERROR_CODE = 0x0423;
    // Delivery Failure Reason
    public static final short TAG_DELIVERY_FAILURE_REASON = 0x0425;
    // More Messages to Follow
    public static final short TAG_MORE_MSGS_TO_FOLLOW = 0x0426;
    // Message State
    public static final short TAG_MSG_STATE = 0x0427;
    // Congestion State
    public static final short TAG_CONGESTION_STATE = 0x0428;
    // Callback Number Presentation  Indicator
    public static final short TAG_CALLBACK_NUM_PRES_IND = 0x0302;
    // Callback Number Alphanumeric Tag
    public static final short TAG_CALLBACK_NUM_ATAG = 0x0303;
    // Number of messages in Mailbox
    public static final short TAG_NUM_MSGS = 0x0304;
    // SMS Received Alert
    public static final short TAG_SMS_SIGNAL = 0x1203;
    // Message Delivery Alert
    public static final short TAG_ALERT_ON_MSG_DELIVERY = 0x130C;
    // ITS Reply Type
    public static final short TAG_ITS_REPLY_TYPE = 0x1380;
    // ITS Session Info
    public static final short TAG_ITS_SESSION_INFO = 0x1383;
    // USSD Service Op
    public static final short TAG_USSD_SERVICE_OP = 0x0501;
    // Broadcast Channel Indicator
    public static final short TAG_BROADCAST_CHANNEL_INDICATOR = 0x0600;
    // Broadcast Content Type
    public static final short TAG_BROADCAST_CONTENT_TYPE = 0x0601;
    // Broadcast Content Type Info
    public static final short TAG_BROADCAST_CONTENT_TYPE_INFO = 0x0602;
    // Broadcast Message Class
    public static final short TAG_BROADCAST_MESSAGE_CLASS = 0x0603;
    // Broadcast Rep Num
    public static final short TAG_BROADCAST_REP_NUM = 0x0604;
    // Broadcast Frequency Interval
    public static final short TAG_BROADCAST_FREQUENCY_INTERVAL = 0x0605;
    // Broadcast Area Identifier
    public static final short TAG_BROADCAST_AREA_IDENTIFIER = 0x0606;
    // Broadcast Error Status
    public static final short TAG_BROADCAST_ERROR_STATUS = 0x0607;
    // Broadcast Area Success
    public static final short TAG_BROADCAST_AREA_SUCCESS = 0x0608;
    // Broadcast End Time
    public static final short TAG_BROADCAST_END_TIME = 0x0609;
    // Broadcast Service Group
    public static final short TAG_BROADCAST_SERVICE_GROUP = 0x060A;
    // Source Network Id
    public static final short TAG_SOURCE_NETWORK_ID = 0x060D;
    // Dest Network Id
    public static final short TAG_DEST_NETWORK_ID = 0x060E;
    // Source Node Id
    public static final short TAG_SOURCE_NODE_ID = 0x060F;
    // Dest Node Id
    public static final short TAG_DEST_NODE_ID = 0x0610;
    // Billing Identification
    public static final short TAG_BILLING_IDENTIFICATION = 0x060B;
    // Originating MSC Address
    public static final short TAG_ORIG_MSC_ADDR = (short)0x8081;
    // Destination MSC Address
    public static final short TAG_DEST_MSC_ADDR = (short)0x8082;
    // Destination Address Subunit
    public static final short TAG_DEST_ADDR_SUBUNIT = 0x0005;
    // Destination Network Type
    public static final short TAG_DEST_NETWORK_TYPE = 0x0006;
    // Destination Bearer Type
    public static final short TAG_DEST_BEAR_TYPE = 0x0007;
    // Destination Telematics ID
    public static final short TAG_DEST_TELE_ID = 0x0008;
    // Source Address Subunit
    public static final short TAG_SOURCE_ADDR_SUBUNIT = 0x000D;
    // Source Network Type
    public static final short TAG_SOURCE_NETWORK_TYPE = 0x000E;
    // Source Bearer Type
    public static final short TAG_SOURCE_BEAR_TYPE = 0x000F;
    // Source Telematics ID
    public static final short TAG_SOURCE_TELE_ID = 0x0010;
    // QOS Time to Live
    public static final short TAG_QOS_TIME_TO_LIVE = 0x0017;
    // Additional Status Info Text
    public static final short TAG_ADD_STATUS_INFO = 0x001D;
    // Receipted Message ID
    public static final short TAG_RECEIPTED_MSG_ID = 0x001E;
    // MS Message Wait Facilities
    public static final short TAG_MS_MSG_WAIT_FACILITIES = 0x0030;

    /** ESM Class */

    /** Message Mode (bits 1-0) */
    public static final byte ESM_CLASS_MM_MASK          = 0x03;  // BIN 00000011
    public static final byte ESM_CLASS_MM_DEFAULT       = 0x00;  // BIN 00000000
    public static final byte ESM_CLASS_MM_DATAGRAM      = 0x01;  // BIN 00000001
    public static final byte ESM_CLASS_MM_TRANSACTION   = 0x02;  // BIN 00000010
    public static final byte ESM_CLASS_MM_STORE_FORWARD = 0x03;  // BIN 00000011

    /** Message Type (bits 5-2) */
    public static final byte ESM_CLASS_MT_MASK = (byte)0x1C;                          // BIN:  11100
    public static final byte ESM_CLASS_MT_SMSC_DELIVERY_RECEIPT = (byte)0x04;         // BIN:    100, Recv Msg contains SMSC delivery receipt
    public static final byte ESM_CLASS_MT_ESME_DELIVERY_RECEIPT = (byte)0x08;         // BIN:   1000, Send/Recv Msg contains ESME delivery acknowledgement
    public static final byte ESM_CLASS_MT_MANUAL_USER_ACK = (byte)0x10;               // BIN:  10000, Send/Recv Msg contains manual/user acknowledgment
    public static final byte ESM_CLASS_MT_CONVERSATION_ABORT = (byte)0x18;            // BIN:  11000, Recv Msg contains conversation abort (Korean CDMA)
    // i believe this flag is separate from the types above...
    public static final byte ESM_CLASS_INTERMEDIATE_DELIVERY_RECEIPT_FLAG = (byte)0x20;  // BIN: 100000, Recv Msg contains intermediate notification
    public static final byte ESM_CLASS_UDHI_MASK = (byte)0x40;
    public static final byte ESM_CLASS_REPLY_PATH_MASK = (byte)0x80;


    /** Registered delivery */

    //   SMSC Delivery Receipt (bits 1 & 0)
    public static final byte REGISTERED_DELIVERY_SMSC_RECEIPT_MASK          = 0x03;
    public static final byte REGISTERED_DELIVERY_SMSC_RECEIPT_NOT_REQUESTED = 0x00;
    public static final byte REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED     = 0x01;
    public static final byte REGISTERED_DELIVERY_SMSC_RECEIPT_ON_FAILURE    = 0x02;
    public static final byte REGISTERED_DELIVERY_SMSC_RECEIPT_ON_SUCCESS    = 0x03;

    //   SME originated acknowledgement (bits 3 & 2)
    public static final byte REGISTERED_DELIVERY_SME_ACK_MASK               = 0x0c;
    public static final byte REGISTERED_DELIVERY_SME_ACK_NOT_REQUESTED      = 0x00;
    public static final byte REGISTERED_DELIVERY_SME_ACK_DELIVERY_REQUESTED = 0x04;
    public static final byte REGISTERED_DELIVERY_SME_ACK_MANUAL_REQUESTED   = 0x08;
    public static final byte REGISTERED_DELIVERY_SME_ACK_BOTH_REQUESTED     = 0x0c;

    // Intermediate notification (bit 4)
    // NOTE: SMPP 3.4 specs originally wrote (bit 5) but their matrix actually used bit 4
    // the confirmed value is bit 4, not 5.
    public static final byte REGISTERED_DELIVERY_INTERMEDIATE_NOTIFICATION_MASK          = 0x10;
    public static final byte REGISTERED_DELIVERY_INTERMEDIATE_NOTIFICATION_NOT_REQUESTED = 0x00;
    public static final byte REGISTERED_DELIVERY_INTERMEDIATE_NOTIFICATION_REQUESTED     = 0x10;


    // Replace if Present flag
    public static final int SM_NOREPLACE                = 0;
    public static final int SM_REPLACE                  = 1;

    // Destination flag
    public static final int SM_DEST_SME_ADDRESS         = 1;
    public static final int SM_DEST_DL_NAME             = 2;

    // Higher Layer Message Type
    public static final int SM_LAYER_WDP                = 0;
    public static final int SM_LAYER_WCMP               = 1;

    // Operation Class
    public static final int SM_OPCLASS_DATAGRAM         = 0;
    public static final int SM_OPCLASS_TRANSACTION      = 3;

    //
    // SMPP Message States
    //
    public static final byte STATE_ENROUTE = (byte) 0x01;
    public static final byte STATE_DELIVERED = (byte) 0x02;
    public static final byte STATE_EXPIRED = (byte) 0x03;
    public static final byte STATE_DELETED = (byte) 0x04;
    public static final byte STATE_UNDELIVERABLE = (byte) 0x05;
    public static final byte STATE_ACCEPTED = (byte) 0x06;
    public static final byte STATE_UNKNOWN = (byte) 0x07;
    public static final byte STATE_REJECTED = (byte) 0x08;

    //
    // SMPP TON
    //
    public static final byte TON_UNKNOWN        = (byte)0x00;
    public static final byte TON_INTERNATIONAL  = (byte)0x01;
    public static final byte TON_NATIONAL       = (byte)0x02;
    public static final byte TON_NETWORK        = (byte)0x03;
    public static final byte TON_SUBSCRIBER     = (byte)0x04;
    public static final byte TON_ALPHANUMERIC   = (byte)0x05;
    public static final byte TON_ABBREVIATED    = (byte)0x06;
    public static final byte TON_RESERVED_EXTN  = (byte)0x07;

    //
    // SMPP NPI
    //
    public static final byte NPI_UNKNOWN        = (byte)0x00;
    public static final byte NPI_E164           = (byte)0x01;
    public static final byte NPI_ISDN           = (byte)0x02;
    public static final byte NPI_X121           = (byte)0x03;
    public static final byte NPI_TELEX          = (byte)0x04;
    public static final byte NPI_LAND_MOBILE    = (byte)0x06;
    public static final byte NPI_NATIONAL       = (byte)0x08;
    public static final byte NPI_PRIVATE        = (byte)0x09;
    public static final byte NPI_ERMES          = (byte)0x0A;
    public static final byte NPI_INTERNET       = (byte)0x0E;
    public static final byte NPI_WAP_CLIENT_ID  = (byte)0x12;

    //
    // SMPP Data Coding
    //
    public static final byte DATA_CODING_DEFAULT 	= (byte)0x00;	// SMSC Default Alphabet
    public static final byte DATA_CODING_IA5		= (byte)0x01;	// IA5 (CCITT T.50)/ASCII (ANSI X3.4)

    /**
     * @deprecated May be removed in a future version
     *      Please use IA5 for DCS 0x01 or DEFAULT for DCS 0x00
     */
    @Deprecated
    public static final byte DATA_CODING_GSM		= (byte)0x01;

    public static final byte DATA_CODING_8BITA		= (byte)0x02;	// Octet unspecified (8-bit binary) defined for TDMA and/ or CDMA but not defined for GSM
    public static final byte DATA_CODING_LATIN1		= (byte)0x03;	// Latin 1 (ISO-8859-1)
    public static final byte DATA_CODING_8BIT		= (byte)0x04;	// Octet unspecified (8-bit binary) ALL TECHNOLOGIES
    public static final byte DATA_CODING_JIS		= (byte)0x05;	// JIS (X 0208-1990)
    public static final byte DATA_CODING_CYRLLIC	= (byte)0x06;	// Cyrllic (ISO-8859-5)
    public static final byte DATA_CODING_HEBREW		= (byte)0x07;	// Latin/Hebrew (ISO-8859-8)
    public static final byte DATA_CODING_UCS2		= (byte)0x08;	// UCS2 (ISO/IEC-10646)
    public static final byte DATA_CODING_PICTO		= (byte)0x09;	// Pictogram Encoding
    public static final byte DATA_CODING_MUSIC		= (byte)0x0A;	// ISO-2022-JP (Music Codes)
    public static final byte DATA_CODING_RSRVD		= (byte)0x0B;	// reserved
    public static final byte DATA_CODING_RSRVD2		= (byte)0x0C;	// reserved
    public static final byte DATA_CODING_EXKANJI	= (byte)0x0D;	// Extended Kanji JIS(X 0212-1990)
    public static final byte DATA_CODING_KSC5601	= (byte)0x0E;	// KS C 5601
    public static final byte DATA_CODING_RSRVD3		= (byte)0x0F;	// reserved

    //
    // Standard SMPP Error Codes
    //
    /** 0x00000000: No Error */
    public static final int STATUS_OK = 0x00000000;

    /** Message Length is invalid */
    public static final int STATUS_INVMSGLEN = 0x00000001;

    /** Command Length is invalid */
    public static final int STATUS_INVCMDLEN = 0x00000002;

    // Invalid Command ID
    public static final int STATUS_INVCMDID = 0x00000003;
    // Incorrect BIND Status for given command
    public static final int STATUS_INVBNDSTS = 0x00000004;
    // ESME Already in Bound State
    public static final int STATUS_ALYBND = 0x00000005;
    // Invalid Priority Flag
    public static final int STATUS_INVPRTFLG = 0x00000006;
    // Invalid Registered Delivery Flag
    public static final int STATUS_INVREGDLVFLG = 0x00000007;
    // System Error
    public static final int STATUS_SYSERR = 0x00000008;

    // 0x00000009 Reserved

    // Invalid Source Address
    public static final int STATUS_INVSRCADR = 0x0000000A;
    // Invalid Dest Addr
    public static final int STATUS_INVDSTADR = 0x0000000B;
    // Message ID is invalid
    public static final int STATUS_INVMSGID = 0x0000000C;
    // Bind Failed
    public static final int STATUS_BINDFAIL = 0x0000000D;
    // Invalid Password
    public static final int STATUS_INVPASWD = 0x0000000E;
    // Invalid System ID
    public static final int STATUS_INVSYSID = 0x0000000F;

    // 0x00000010 Reserved

    // Cancel SM Failed
    public static final int STATUS_CANCELFAIL = 0x00000011;

    // 0x00000012 Reserved

    // Replace SM Failed
    public static final int STATUS_REPLACEFAIL = 0x00000013;
    // Message Queue Full
    public static final int STATUS_MSGQFUL = 0x00000014;
    // Invalid Service Type
    public static final int STATUS_INVSERTYP = 0x00000015;

    // 0x00000016-0x00000032 Reserved

    // Invalid number of destinations
    public static final int STATUS_INVNUMDESTS = 0x00000033;
    // Invalid Distribution List name
    public static final int STATUS_INVDLNAME = 0x00000034;

    // 0x00000035-0x0000003F Reserved

    // Destination flag is invalid (submit_multi)
    public static final int STATUS_INVDESTFLAG = 0x00000040;

    // 0x00000041 Reserved

    // Invalid ‘submit with replace’ request
    // (i.e. submit_sm with replace_if_present_flag set)
    public static final int STATUS_INVSUBREP = 0x00000042;
    // Invalid esm_class field data
    public static final int STATUS_INVESMCLASS = 0x00000043;
    // Cannot Submit to Distribution List
    public static final int STATUS_CNTSUBDL = 0x00000044;
    // submit_sm or submit_multi failed
    public static final int STATUS_SUBMITFAIL = 0x00000045;

    // 0x00000046-0x00000047 Reserved

    // Invalid Source address TON
    public static final int STATUS_INVSRCTON = 0x00000048;
    // Invalid Source address NPI
    public static final int STATUS_INVSRCNPI = 0x00000049;
    // Invalid Destination address TON
    public static final int STATUS_INVDSTTON = 0x00000050;
    // Invalid Destination address NPI
    public static final int STATUS_INVDSTNPI = 0x00000051;

    // 0x00000052 Reserved

    // Invalid system_type field
    public static final int STATUS_INVSYSTYP = 0x00000053;
    // Invalid replace_if_present flag
    public static final int STATUS_INVREPFLAG = 0x00000054;
    // Invalid number of messages
    public static final int STATUS_INVNUMMSGS = 0x00000055;

    // 0x00000056-0x00000057 Reserved

    // Throttling error (ESME has exceeded allowed message limits)
    public static final int STATUS_THROTTLED = 0x00000058;

    // 0x00000059-0x00000060 Reserved

    // Invalid Scheduled Delivery Time
    public static final int STATUS_INVSCHED = 0x00000061;
    // Invalid message validity period (Expiry time)
    public static final int STATUS_INVEXPIRY = 0x00000062;
    // Predefined Message Invalid or Not Found
    public static final int STATUS_INVDFTMSGID = 0x00000063;
    // ESME Receiver Temporary App Error Code
    public static final int STATUS_X_T_APPN = 0x00000064;
    // ESME Receiver Permanent App Error Code
    public static final int STATUS_X_P_APPN = 0x00000065;
    // ESME Receiver Reject Message Error Code
    public static final int STATUS_X_R_APPN = 0x00000066;
    // query_sm request failed
    public static final int STATUS_QUERYFAIL = 0x00000067;

    // 0x00000068-0x000000BF Reserved

    // Error in the optional part of the PDU Body.
    public static final int STATUS_INVOPTPARSTREAM = 0x000000C0;
    // Optional Parameter not allowed
    public static final int STATUS_OPTPARNOTALLWD = 0x000000C1;
    // Invalid Parameter Length.
    public static final int STATUS_INVPARLEN = 0x000000C2;
    // Expected Optional Parameter missing
    public static final int STATUS_MISSINGOPTPARAM = 0x000000C3;
    // Invalid Optional Parameter Value
    public static final int STATUS_INVOPTPARAMVAL = 0x000000C4;

    // 0x000000C5-0x000000FD Reserved

    // Delivery Failure (used for data_sm_resp)
    public static final int STATUS_DELIVERYFAILURE = 0x000000FE;
    // Unknown Error
    public static final int STATUS_UNKNOWNERR = 0x000000FF;
    // ESME Not authorised to use specified service_type
    public static final int STATUS_SERTYPUNAUTH = 0x00000100;
    // ESME Prohibited from using specified operation
    public static final int STATUS_PROHIBITED = 0x00000101;
    // Specified service_type is unavailable
    public static final int STATUS_SERTYPUNAVAIL = 0x00000102;
    // Specified service_type is denied
    public static final int STATUS_SERTYPDENIED = 0x00000103;
    // Invalid Data Coding Scheme
    public static final int STATUS_INVDCS = 0x00000104;
    // Source Address Sub unit is Invalid
    public static final int STATUS_INVSRCADDRSUBUNIT = 0x00000105;
    // Destination Address Sub unit is Invalid
    public static final int STATUS_INVDSTADDRSUBUNIT = 0x00000106;
    // Broadcast Frequency Interval is invalid
    public static final int STATUS_INVBCASTFREQINT = 0x00000107;
    // Broadcast Alias Name is invalid
    public static final int STATUS_INVBCASTALIAS_NAME = 0x00000108;
    // Broadcast Area Format is invalid
    public static final int STATUS_INVBCASTAREAFMT = 0x00000109;
    // Numberof Broadcast Areas is invalid
    public static final int STATUS_INVNUMBCAST_AREAS = 0x0000010A;
    // Broadcast Content Type is invalid
    public static final int STATUS_INVBCASTCNTTYPE = 0x0000010B;
    // Broadcast Message Class is invalid
    public static final int STATUS_INVBCASTMSGCLASS = 0x0000010C;

    public static final Map<Integer,String> STATUS_MESSAGE_MAP;
    public static final Map<Short,String> TAG_NAME_MAP;

    static {
        STATUS_MESSAGE_MAP = new HashMap<Integer,String>();
        STATUS_MESSAGE_MAP.put(STATUS_OK, "OK");
        STATUS_MESSAGE_MAP.put(STATUS_INVMSGLEN, "Message length invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVCMDLEN, "Command length invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVCMDID, "Command ID invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVBNDSTS, "Incorrect bind status for given command");
        STATUS_MESSAGE_MAP.put(STATUS_ALYBND, "ESME already in bound state");
        STATUS_MESSAGE_MAP.put(STATUS_INVPRTFLG, "Priority flag invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVREGDLVFLG, "Registered delivery flag invalid");
        STATUS_MESSAGE_MAP.put(STATUS_SYSERR, "System error");
        STATUS_MESSAGE_MAP.put(STATUS_INVSRCADR, "Source address invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDSTADR, "Dest address invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVMSGID, "Message ID invalid");
        STATUS_MESSAGE_MAP.put(STATUS_BINDFAIL, "Bind failed");
        STATUS_MESSAGE_MAP.put(STATUS_INVPASWD, "Password invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVSYSID, "System ID invalid");
        STATUS_MESSAGE_MAP.put(STATUS_CANCELFAIL, "Cancel SM failed");
        STATUS_MESSAGE_MAP.put(STATUS_REPLACEFAIL, "Replace SM failed");
        STATUS_MESSAGE_MAP.put(STATUS_MSGQFUL, "Message queue full");
        STATUS_MESSAGE_MAP.put(STATUS_INVSERTYP, "Service type invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVNUMDESTS, "Number of destinations invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDLNAME, "Distribution list name invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDESTFLAG, "Destination flag is invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVSUBREP, "Submit with replace request invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVESMCLASS, "Field esm_class invalid");
        STATUS_MESSAGE_MAP.put(STATUS_CNTSUBDL, "Cannot submit to distribution list");
        STATUS_MESSAGE_MAP.put(STATUS_SUBMITFAIL, "Submit SM failed");
        STATUS_MESSAGE_MAP.put(STATUS_INVSRCTON, "Source address TON invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVSRCNPI, "Source address NPI invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDSTTON, "Dest address TON invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDSTNPI, "Dest address NPI invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVSYSTYP, "System type invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVREPFLAG, "Field replace_if_present invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVNUMMSGS, "Number of messages invalid");
        STATUS_MESSAGE_MAP.put(STATUS_THROTTLED, "Throttling error");
        STATUS_MESSAGE_MAP.put(STATUS_INVSCHED, "Scheduled delivery time invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVEXPIRY, "Message validity period invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDFTMSGID, "Predefined message invalid or not found");
        STATUS_MESSAGE_MAP.put(STATUS_X_T_APPN, "ESME receiver temporary app error");
        STATUS_MESSAGE_MAP.put(STATUS_X_P_APPN, "ESME receiver permanent app error");
        STATUS_MESSAGE_MAP.put(STATUS_X_R_APPN, "ESME receiver reject app error");
        STATUS_MESSAGE_MAP.put(STATUS_QUERYFAIL, "Query SM failed");
        STATUS_MESSAGE_MAP.put(STATUS_INVOPTPARSTREAM, "Error in the optional part of the PDU Body");
        STATUS_MESSAGE_MAP.put(STATUS_OPTPARNOTALLWD, "Optional Parameter not allowed");
        STATUS_MESSAGE_MAP.put(STATUS_INVPARLEN, "Parameter length invalid");
        STATUS_MESSAGE_MAP.put(STATUS_MISSINGOPTPARAM, "Expected optional parameter missing");
        STATUS_MESSAGE_MAP.put(STATUS_INVOPTPARAMVAL, "Optional parameter value invalid");
        STATUS_MESSAGE_MAP.put(STATUS_DELIVERYFAILURE, "Deliver SM failed");
        STATUS_MESSAGE_MAP.put(STATUS_UNKNOWNERR, "Unknown error");
        STATUS_MESSAGE_MAP.put(STATUS_SERTYPUNAUTH, "Not authorised to use specified service_type");
        STATUS_MESSAGE_MAP.put(STATUS_PROHIBITED, "Prohibited from using specified operation");
        STATUS_MESSAGE_MAP.put(STATUS_SERTYPUNAVAIL, "Specified service_type is unavailable");
        STATUS_MESSAGE_MAP.put(STATUS_SERTYPDENIED, "Specified service_type is denied");
        STATUS_MESSAGE_MAP.put(STATUS_INVDCS, "Invalid Data Coding Scheme");
        STATUS_MESSAGE_MAP.put(STATUS_INVSRCADDRSUBUNIT, "Source Address Sub unit is Invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVDSTADDRSUBUNIT, "Destination Address Sub unit is Invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVBCASTFREQINT, "Broadcast Frequency Interval is invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVBCASTALIAS_NAME, "Broadcast Alias Name is invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVBCASTAREAFMT, "Broadcast Area Format is invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVNUMBCAST_AREAS, "Number of Broadcast Areas is invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVBCASTCNTTYPE, "Broadcast Content Type is invalid");
        STATUS_MESSAGE_MAP.put(STATUS_INVBCASTMSGCLASS, "Broadcast Message Class is invalid");

        TAG_NAME_MAP = new HashMap<Short,String>();
        TAG_NAME_MAP.put(TAG_SOURCE_TELEMATICS_ID, "source_telematics_id");
        TAG_NAME_MAP.put(TAG_PAYLOAD_TYPE, "payload_type");
        TAG_NAME_MAP.put(TAG_PRIVACY_INDICATOR, "privacy_indicator");
        TAG_NAME_MAP.put(TAG_USER_MESSAGE_REFERENCE, "user_message_reference");
        TAG_NAME_MAP.put(TAG_USER_RESPONSE_CODE, "user_response_code");
        TAG_NAME_MAP.put(TAG_SOURCE_PORT, "source_port");
        TAG_NAME_MAP.put(TAG_DESTINATION_PORT, "dest_port");
        TAG_NAME_MAP.put(TAG_SAR_MSG_REF_NUM, "sar_msg_ref_num");
        TAG_NAME_MAP.put(TAG_LANGUAGE_INDICATOR, "lang_indicator");
        TAG_NAME_MAP.put(TAG_SAR_TOTAL_SEGMENTS, "sar_total_segments");
        TAG_NAME_MAP.put(TAG_SAR_SEGMENT_SEQNUM, "sar_segment_seqnum");
        TAG_NAME_MAP.put(TAG_SOURCE_SUBADDRESS, "source_subaddr");
        TAG_NAME_MAP.put(TAG_DEST_SUBADDRESS, "dest_subaddr");
        TAG_NAME_MAP.put(TAG_CALLBACK_NUM, "callback_num");
        TAG_NAME_MAP.put(TAG_MESSAGE_PAYLOAD, "message_payload");
        TAG_NAME_MAP.put(TAG_SC_INTERFACE_VERSION, "sc_interface_version");
        TAG_NAME_MAP.put(TAG_DISPLAY_TIME, "display_time");
        TAG_NAME_MAP.put(TAG_MS_VALIDITY, "ms_validity");
        TAG_NAME_MAP.put(TAG_DPF_RESULT, "dpf_result");
        TAG_NAME_MAP.put(TAG_SET_DPF, "set_dpf");
        TAG_NAME_MAP.put(TAG_MS_AVAIL_STATUS, "ms_avail_status");
        TAG_NAME_MAP.put(TAG_NETWORK_ERROR_CODE, "network_error_code");
        TAG_NAME_MAP.put(TAG_DELIVERY_FAILURE_REASON, "delivery_failure_reason");
        TAG_NAME_MAP.put(TAG_MORE_MSGS_TO_FOLLOW, "more_msgs_to_follow");
        TAG_NAME_MAP.put(TAG_MSG_STATE, "message_state");
        TAG_NAME_MAP.put(TAG_CONGESTION_STATE, "congestion_state");
        TAG_NAME_MAP.put(TAG_CALLBACK_NUM_PRES_IND, "callback_num_pres_ind");
        TAG_NAME_MAP.put(TAG_CALLBACK_NUM_ATAG, "callback_num_atag");
        TAG_NAME_MAP.put(TAG_NUM_MSGS, "num_msgs_in_mailbox");
        TAG_NAME_MAP.put(TAG_SMS_SIGNAL, "sms_signal");
        TAG_NAME_MAP.put(TAG_ALERT_ON_MSG_DELIVERY, "alert_on_msg_delivery");
        TAG_NAME_MAP.put(TAG_ITS_REPLY_TYPE, "its_reply_type");
        TAG_NAME_MAP.put(TAG_ITS_SESSION_INFO, "its_session_info");
        TAG_NAME_MAP.put(TAG_USSD_SERVICE_OP, "ussd_service_op");
        TAG_NAME_MAP.put(TAG_BROADCAST_CHANNEL_INDICATOR, "broadcast_channel_indicator");
        TAG_NAME_MAP.put(TAG_BROADCAST_CONTENT_TYPE, "broadcast_content_type");
        TAG_NAME_MAP.put(TAG_BROADCAST_CONTENT_TYPE_INFO, "broadcast_content_type_info");
        TAG_NAME_MAP.put(TAG_BROADCAST_MESSAGE_CLASS, "broadcast_message_class");
        TAG_NAME_MAP.put(TAG_BROADCAST_REP_NUM, "broadcast_rep_num");
        TAG_NAME_MAP.put(TAG_BROADCAST_FREQUENCY_INTERVAL, "broadcast_frequency_interval");
        TAG_NAME_MAP.put(TAG_BROADCAST_AREA_IDENTIFIER, "broadcast_area_identifier");
        TAG_NAME_MAP.put(TAG_BROADCAST_ERROR_STATUS, "broadcast_error_status");
        TAG_NAME_MAP.put(TAG_BROADCAST_AREA_SUCCESS, "broadcast_area_success");
        TAG_NAME_MAP.put(TAG_BROADCAST_END_TIME, "broadcast_end_time");
        TAG_NAME_MAP.put(TAG_BROADCAST_SERVICE_GROUP, "broadcast_service_group");
        TAG_NAME_MAP.put(TAG_SOURCE_NETWORK_ID, "source_network_id");
        TAG_NAME_MAP.put(TAG_DEST_NETWORK_ID, "dest_network_id");
        TAG_NAME_MAP.put(TAG_SOURCE_NODE_ID, "source_node_id");
        TAG_NAME_MAP.put(TAG_DEST_NODE_ID, "dest_node_id");
        TAG_NAME_MAP.put(TAG_BILLING_IDENTIFICATION, "billing_identification");
        TAG_NAME_MAP.put(TAG_ORIG_MSC_ADDR, "orig_msc_addr");
        TAG_NAME_MAP.put(TAG_DEST_MSC_ADDR, "dest_msc_addr");
        TAG_NAME_MAP.put(TAG_DEST_ADDR_SUBUNIT, "dest_addr_subunit");
        TAG_NAME_MAP.put(TAG_DEST_NETWORK_TYPE, "dest_network_type");
        TAG_NAME_MAP.put(TAG_DEST_BEAR_TYPE, "dest_bearer_type");
        TAG_NAME_MAP.put(TAG_DEST_TELE_ID, "dest_telematics_id");
        TAG_NAME_MAP.put(TAG_SOURCE_ADDR_SUBUNIT, "source_addr_subunit");
        TAG_NAME_MAP.put(TAG_SOURCE_NETWORK_TYPE, "source_network_type");
        TAG_NAME_MAP.put(TAG_SOURCE_BEAR_TYPE, "source_bearer_type");
        TAG_NAME_MAP.put(TAG_SOURCE_TELE_ID, "source_telematics_id");
        TAG_NAME_MAP.put(TAG_QOS_TIME_TO_LIVE, "qos_time_to_live");
        TAG_NAME_MAP.put(TAG_ADD_STATUS_INFO, "additional_status_info");
        TAG_NAME_MAP.put(TAG_RECEIPTED_MSG_ID, "receipted_message_id");
        TAG_NAME_MAP.put(TAG_MS_MSG_WAIT_FACILITIES, "ms_msg_wait_facilities");
    }

}
