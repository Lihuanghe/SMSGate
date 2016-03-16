package org.marre.sms;

/**
 * SMS message classes.
 */
public enum SmsMsgClass {
    /** Class 0 SMS. Sometimes called FLASH message. */
    CLASS_0,
    /** Class 1 SMS. Default meaning: ME-specific. */
    CLASS_1,
    /** Class 2 SMS, SIM specific message. */
    CLASS_2,
    /** Class 3 SMS. Default meaning: TE specific (See GSM TS 07.05). */
    CLASS_3,
    /** Message with no specific message class (Often handled as an class 1 SMS). */
    CLASS_UNKNOWN;
}
