package org.marre.sms;

/**
 * SMS Numbering Plan Identification.
 */
public final class SmsNpi {
    /**
     * Numbering-Plan-Identification - Unknown.
     */
    public static final SmsNpi UNKNOWN = new SmsNpi(0x00, "UNKNOWN");
    /**
     * Numbering-Plan-Identification - ISDN/Telephone. (E.164 /E.163)
     * <p>
     * Most common.
     */
    public static final SmsNpi ISDN_TELEPHONE = new SmsNpi(0x01, "ISDN_TELEPHONE");
    /**
     * Numbering-Plan-Identification - Data Numbering Plan (X.121)
     */
    public static final SmsNpi DATA = new SmsNpi(0x03, "DATA");
    /**
     * Numbering-Plan-Identification - Telex.
     */
    public static final SmsNpi TELEX = new SmsNpi(0x04, "TELEX");
    /**
     * Numbering-Plan-Identification - National.
     */
    public static final SmsNpi NATIONAL = new SmsNpi(0x08, "NATIONAL");
    /**
     * Numbering-Plan-Identification - Private.
     */
    public static final SmsNpi PRIVATE = new SmsNpi(0x09, "PRIVATE");
    /**
     * Numbering-Plan-Identification - Unknown.
     */
    public static final SmsNpi ERMES = new SmsNpi(0x10, "ERMES");

    private final int value;
    private final String name;

    private SmsNpi(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Convert a NPI value into an SmsNpi object.
     * @param value The NPI value as specified in the GSM spec.
     * @return one of the statically defined SmsNpi or a new SmsNpi if unknown.
     */
    public static SmsNpi valueOf(int value) {
        switch (value) {
            case 0x00: return UNKNOWN;
            case 0x01: return ISDN_TELEPHONE;
            case 0x03: return DATA;
            case 0x04: return TELEX;
            case 0x08: return NATIONAL;
            case 0x09: return PRIVATE;
            case 0x10: return ERMES;
            default: return new SmsNpi(value, String.valueOf(value));
        }
    }

    /**
     * Returns the NPI value as specified in the GSM spec.
     * @return
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmsNpi smsNpi = (SmsNpi) o;

        return value == smsNpi.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}
