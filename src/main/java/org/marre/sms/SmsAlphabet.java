package org.marre.sms;

/**
 * The different alphabet types.
 */
public enum SmsAlphabet {
    /**
     * Alphabet as defined in GSM 03.38. It contains all characters needed for most
     * Western European languages. It also contains upper case Greek characters.
     */
    GSM,
    
    ASCII,  //CMPP 协议，不使用GSM septet编码

    /** ISO 8859-1 (ISO Latin-1). */
    LATIN1,

    /** Unicode UCS-2. */
    UCS2,

    /** Reserved. */
    RESERVED;
}
