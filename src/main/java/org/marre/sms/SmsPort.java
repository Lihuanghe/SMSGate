package org.marre.sms;

import java.io.Serializable;

/**
 * Collection of some known Sms port numbers.
 */
public final class SmsPort implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5328952526114005361L;

	/** Port 0, often used as originator port. */
    public static final SmsPort ZERO = new SmsPort(0, "ZERO");

    /** WAP Push. */
    public static final SmsPort WAP_PUSH = new SmsPort(2948, "WAP_PUSH");

    /** Nokia Internet access configuration data. */
    public static final SmsPort NOKIA_IAC = new SmsPort(5503, "NOKIA_IAC");
    /** Nokia Ring Tone. */
    public static final SmsPort NOKIA_RING_TONE = new SmsPort(5505, "NOKIA_RING_TONE");
    /** Nokia Operator Logo. */
    public static final SmsPort NOKIA_OPERATOR_LOGO = new SmsPort(5506, "NOKIA_OPERATOR_LOGO");
    /** Nokia Calling Line Identification Logo. */
    public static final SmsPort NOKIA_CLI_LOGO = new SmsPort(5507, "NOKIA_CLI_LOGO");
    /** Nokia Email notification. */
    public static final SmsPort NOKIA_EMAIL_NOTIFICATION = new SmsPort(5512, "NOKIA_EMAIL_NOTIFICATION");
    /** Nokia Multipart Message. */
    public static final SmsPort NOKIA_MULTIPART_MESSAGE = new SmsPort(5514, "NOKIA_MULTIPART_MESSAGE");

    /** WAP connectionless session service. */
    public static final SmsPort WAP_WSP = new SmsPort(9200, "WAP_WSP");
    /** WAP session service. */
    public static final SmsPort WAP_WSP_WTP = new SmsPort(9201, "WAP_WSP_WTP");

    /** WAP vCard. */
    public static final SmsPort WAP_VCARD = new SmsPort(9204, "WAP_VCARD");
    /** WAP vCalendar. */
    public static final SmsPort WAP_VCALENDAR = new SmsPort(9205, "WAP_VCALENDAR");

    /** OTA Settings - Browser. */
    public static final SmsPort OTA_SETTINGS_BROWSER = new SmsPort(49999, "OTA_SETTINGS_BROWSER");

    /** OTA Settings - SyncML. */
    public static final SmsPort OTA_SETTINGS_SYNCML = new SmsPort(49996, "OTA_SETTINGS_SYNCML");

    private final int port;
    private final String name;

    public SmsPort(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public SmsPort(int port) {
        this.port = port;
        this.name = String.valueOf(port);
    }

    /**
     * Returns the port number.
     * @return
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmsPort smsPort = (SmsPort) o;

        return port == smsPort.port;

    }

    @Override
    public int hashCode() {
        return port;
    }
}
