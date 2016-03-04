package org.marre.sms;

/**
 * Collection of known SMS UDH Identity Element Identifier.
 */
public final class SmsUdhIei {
    /** Concatenated short messages, 8-bit reference number. */
    public static final SmsUdhIei CONCATENATED_8BIT = new SmsUdhIei((byte)0x00, "CONCATENATED_8BIT");
    /** Special SMS Message Indication. */
    public static final SmsUdhIei SPECIAL_MESSAGE = new SmsUdhIei((byte)0x01, "SPECIAL_MESSAGE");
    /** Application port addressing scheme, 8 bit address. */
    public static final SmsUdhIei APP_PORT_8BIT = new SmsUdhIei((byte)0x04, "APP_PORT_8BIT");
    /** Application port addressing scheme, 16 bit address. */
    public static final SmsUdhIei APP_PORT_16BIT = new SmsUdhIei((byte)0x05, "APP_PORT_16BIT");
    /** SMSC Control Parameters. */
    public static final SmsUdhIei SMSC_CONTROL_PARAMS = new SmsUdhIei((byte)0x06, "SMSC_CONTROL_PARAMS");
    /** UDH Source Indicator. */
    public static final SmsUdhIei UDH_SOURCE_INDICATOR = new SmsUdhIei((byte)0x07, "UDH_SOURCE_INDICATOR");
    /** Concatenated short message, 16-bit reference number. */
    public static final SmsUdhIei CONCATENATED_16BIT = new SmsUdhIei((byte)0x08, "CONCATENATED_16BIT");
    /** Wireless Control Message Protocol. */
    public static final SmsUdhIei WCMP = new SmsUdhIei((byte)0x09, "WCMP");

    /** RFC 822 E-Mail Header. */
    public static final SmsUdhIei RFC822_EMAIL_HEADER = new SmsUdhIei((byte)0x20, "RFC822_EMAIL_HEADER");
    /** Hyperlink format element. */
    public static final SmsUdhIei HYPERLINK_FORMAT = new SmsUdhIei((byte)0x21, "HYPERLINK_FORMAT");

    private final byte value;
    private final String name;

    private SmsUdhIei(byte value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Convert a UDH IEI value into an SmsUdhIei object.
     * @param value The UDH IEI value as specified in the GSM spec.
     * @return one of the statically defined SmsNpi or a new SmsNpi if unknown.
     */
    public static SmsUdhIei valueOf(byte value) {
        switch (value) {
            case 0x00: return CONCATENATED_8BIT;
            case 0x01: return SPECIAL_MESSAGE;
            case 0x04: return APP_PORT_8BIT;
            case 0x05: return APP_PORT_16BIT;
            case 0x06: return SMSC_CONTROL_PARAMS;
            case 0x07: return UDH_SOURCE_INDICATOR;
            case 0x08: return CONCATENATED_16BIT;
            case 0x09: return WCMP;
            case 0x20: return RFC822_EMAIL_HEADER;
            case 0x21: return HYPERLINK_FORMAT;
            default: return new SmsUdhIei(value, String.valueOf(value));
        }
    }

    /**
     * Returns the UDH IEI value as specified in the GSM spec.
     * @return
     */
    public byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
