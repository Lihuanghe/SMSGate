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
            default: {
            	if(value < InfoEleNameList.length){
            		return new SmsUdhIei(value, InfoEleNameList[value]);
            	}
            	return new SmsUdhIei(value, String.valueOf(value));
            }
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
    
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmsUdhIei other = (SmsUdhIei) obj;
		if (value != other.value)
			return false;
		return true;
	}

	/**
	 * 参考：
	 * <a href="https://en.wikipedia.org/wiki/User_Data_Header">协议</a>
	 */
	final static String[] InfoEleNameList =  new String[]{"0	Concatenated short messages, 8-bit reference number",
		"1	Special SMS Message Indication",
		"2	Reserved",
		"3	Not used to avoid misinterpretation as <LF> character",
		"4	Application port addressing scheme, 8 bit address",
		"5	Application port addressing scheme, 16 bit address",
		"6	SMSC Control Parameters",
		"7	UDH Source Indicator",
		"8	Concatenated short message, 16-bit reference number",
		"9	Wireless Control Message Protocol",
		"0A	Text Formatting",
		"0B	Predefined Sound",
		"0C	User Defined Sound (iMelody max 128 bytes)",
		"0D	Predefined Animation",
		"0E	Large Animation (16*16 times 4 = 32*4 =128 bytes)",
		"0F	Small Animation (8*8 times 4 = 8*4 =32 bytes)",
		"10	Large Picture (32*32 = 128 bytes)",
		"11	Small Picture (16*16 = 32 bytes)",
		"12	Variable Picture",
		"13	User prompt indicator",
		"14	Extended Object",
		"15	Reused Extended Object",
		"16	Compression Control",
		"17	Object Distribution Indicator",
		"18	Standard WVG object",
		"19	Character Size WVG object",
		"1A	Extended Object Data Request Command",
		"1B	Reserved for future EMS features",
		"1C	Reserved for future EMS features",
		"1D	Reserved for future EMS features",
		"1E	Reserved for future EMS features",
		"1F	Reserved for future EMS features",
		"20	RFC 822 E-Mail Header",
		"21	Hyperlink format element",
		"22	Reply Address Element",
		"23	Enhanced Voice Mail Information",
		"24	National Language Single Shift",
		"25	National Language Locking Shift"};
}
