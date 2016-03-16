package org.marre.sms;

/**
 * SMS Type-Of-Number.
 */
public final class SmsTon {
    /**
     * "Unknown" is used when the user or network has
     * no priori information about the numbering plan. In this case, the
     * Address-Value field is organized according to the network dialling plan,
     * e.g. prefix or escape digits might be present.
     */
    public static final SmsTon UNKNOWN = new SmsTon(0x00, "UNKNOWN");
    /**
     * The international format shall be
     * accepted also when the message is destined to a recipient in the same
     * country as the MSC or as the SGSN.
     * <p>
     * Most common.
     */
    public static final SmsTon INTERNATIONAL = new SmsTon(0x01, "INTERNATIONAL");
    /**
     * Prefix or escape digits shall not be included.
     */
    public static final SmsTon NATIONAL = new SmsTon(0x02, "NATIONAL");
    /**
     * "Network specific number" is used to
     * indicate administration/service number specific to the serving network,
     * e.g. used to access an operator.
     */
    public static final SmsTon NETWORK_SPECIFIC = new SmsTon(0x03, "NETWORK_SPECIFIC");
    /**
     * "Subscriber number" is used when a specific
     * short number representation is stored in one or more SCs as part of a
     * higher layer application. (Note that "Subscriber number" shall only be
     * used in connection with the proper PID referring to this application).
     */
    public static final SmsTon SUBSCRIBER = new SmsTon(0x04, "SUBSCRIBER");
    /**
     * Type-Of-Number - Alphanumeric. Number must be coded according to 3GPP TS
     * 23.038 GSM 7-bit default alphabet.
     * <p>
     * NPI Must be set to UNKNOWN.
     */
    public static final SmsTon ALPHANUMERIC = new SmsTon(0x05, "ALPHANUMERIC");
    /**
     * Type-Of-Number - Abbreviated.
     */
    public static final SmsTon ABBREVIATED = new SmsTon(0x06, "ABBREVIATED");

    private final int value;
    private final String name;

    private SmsTon(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Converts a TON value into a SmsTon class.
     * @param value
     * @return one of the statically defined SmsTon or a new SmsTon if no matching SmsTon was found.
     */
    public static SmsTon valueOf(int value) {
        switch (value) {
            case 0x00: return UNKNOWN;
            case 0x01: return INTERNATIONAL;
            case 0x02: return NATIONAL;
            case 0x03: return NETWORK_SPECIFIC;
            case 0x04: return SUBSCRIBER;
            case 0x05: return ALPHANUMERIC;
            case 0x06: return ABBREVIATED;
            default: return new SmsTon(value, String.valueOf(value));
        }
    }

    /**
     * Returns the value of the TON as specified in the GSM spec.
     * @return
     */
    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmsTon smsTon = (SmsTon) o;

        return value == smsTon.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}
