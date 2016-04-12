package org.marre.sms.nokia;

import java.io.Serializable;

/**
 * Represents the know nokia item types used in a NokiaMultipartMessage.
 */
public final class NokiaItemType implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1244183223843141668L;
	public static final NokiaItemType TEXT_ISO_8859_1 = new NokiaItemType((byte)0x00, "TEXT_ISO_8859_1");
    public static final NokiaItemType TEXT_UNICODE = new NokiaItemType((byte)0x01, "TEXT_UNICODE");
    public static final NokiaItemType OTA_BITMAP = new NokiaItemType((byte)0x02, "OTA_BITMAP");
    public static final NokiaItemType RINGTONE = new NokiaItemType((byte)0x03, "RINGTONE");
    public static final NokiaItemType PROFILE_NAME = new NokiaItemType((byte)0x04, "PROFILE_NAME");
    public static final NokiaItemType SCREEN_SAVER = new NokiaItemType((byte)0x06, "SCREEN_SAVER");

    private final byte typeId;
    private final String name;

    private NokiaItemType(byte typeId, String name) {
        this.typeId = typeId;
        this.name = name;
    }

    public byte getTypeId() {
        return typeId;
    }

    /**
     * Returns one of the known NokiaItemTypes defined in this class or a new instance if type is unknown.
     */
    public static NokiaItemType valueOf(byte typeId) {
        switch (typeId) {
            case 0x00: return TEXT_ISO_8859_1;
            case 0x01: return TEXT_UNICODE;
            case 0x02: return OTA_BITMAP;
            case 0x03: return RINGTONE;
            case 0x04: return PROFILE_NAME;
            case 0x05: return SCREEN_SAVER;
            default:   return new NokiaItemType(typeId, String.valueOf(typeId));
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NokiaItemType that = (NokiaItemType) o;

        return typeId == that.typeId;

    }

    @Override
    public int hashCode() {
        return typeId;
    }
}
