package com.zx.sms.codec.smgp.tlv;

import com.zx.sms.codec.smgp.util.ByteUtil;

public class TLVString extends TLV {
	private String value;

	public TLVString() {
		super();
	}

	public TLVString(short tag) {
		super(tag);
	}

	public TLVString(short tag, int min, int max) {
		super(tag, min, max);
	}

	public TLVString(short tag, String value) throws Exception {
		super(tag);
		setValue(value);
	}

	public TLVString(short tag, int min, int max, String value) throws Exception {
		super(tag, min, max);
		setValue(value);
	}

	@Override
	public void setValueData(byte[] buffer) throws Exception {
		value = new String(ByteUtil.rtrimBytes(buffer));
		markValueSet();
	}

	@Override
	public byte[] getValueData() throws Exception {
		if(value==null)return null;
		byte[] valueBytes = value.getBytes();
		byte[] buffer = new byte[valueBytes.length + 1];
		System.arraycopy(valueBytes, 0, buffer, 0, valueBytes.length);
		buffer[valueBytes.length] = 0;
		return buffer;
	}

	public void setValue(String value) {
		this.value = value;
		if(value!=null)
		   markValueSet();
	}

	public String getValue() {
		return value;

	}

}