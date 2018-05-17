package org.marre.sms;

/** DCS Coding Groups */
public enum DcsGroup {
    /** DCS general data coding indication group. 00xxxxxx. */
    GENERAL_DATA_CODING,
    
    /** DCS message waiting indication group: discard message. 1100xxxx. */
    MESSAGE_WAITING_DISCARD,
    
    /** DCS message waiting indication group: store message (gsm). 1101xxxx. */
    MESSAGE_WAITING_STORE_GSM,
    
    /** DCS message waiting indication group: store message (ucs2). 1110xxxx. */
    MESSAGE_WAITING_STORE_UCS2,
    
    /** DCS data coding/message class: 1111xxxx. */
    DATA_CODING_MESSAGE,
    
    /** Message Marked for Automatic Deletion Group: 01xxxxxx. */
    MSG_MARK_AUTO_DELETE,
    
    /** Reserved coding groups:  1000xxxx-1011xxxx.*/
    RESERVED
}
