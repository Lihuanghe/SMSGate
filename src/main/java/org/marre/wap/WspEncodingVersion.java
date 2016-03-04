package org.marre.wap;

public enum WspEncodingVersion {
    VERSION_1_1((byte)0x11, 0x2D),
    VERSION_1_2((byte)0x12, 0x34),
    VERSION_1_3((byte)0x13, 0x36),
    VERSION_1_4((byte)0x14, 0x3F),
    VERSION_1_5((byte)0x15, 0x4B);

    private final byte value;
    private final int maxWellKnownContentTypeId;

    private WspEncodingVersion(byte value, int maxWellKnownContentTypeId) {
        this.value = value;
        this.maxWellKnownContentTypeId = maxWellKnownContentTypeId;
    }

    public boolean isWellKnownContentTypeId(int wellKnownContentTypeId) {
        return wellKnownContentTypeId <= maxWellKnownContentTypeId;
    }
}
