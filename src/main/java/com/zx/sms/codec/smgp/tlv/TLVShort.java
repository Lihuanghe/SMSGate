package com.zx.sms.codec.smgp.tlv;

import com.zx.sms.codec.smgp.util.ByteUtil;

public class TLVShort extends TLV {
	private short value = 0;

	public TLVShort() {
		super(2, 2);
	}

	public TLVShort(short p_tag) {
		super(p_tag, 2, 2);
	}

	public TLVShort(short p_tag, short p_value) {
		super(p_tag, 2, 2);
		value = p_value;
		markValueSet();
	}

	public void setValue(short p_value) {
		value = p_value;
		markValueSet();
	}

	public short getValue() {
		return value;
	}

	@Override
	public byte[] getValueData() throws Exception {
		return ByteUtil.short2byte(value);
	}

	@Override
	public void setValueData(byte[] buffer) throws Exception {
		value = ByteUtil.byte2short(buffer);
		markValueSet();
	}

}