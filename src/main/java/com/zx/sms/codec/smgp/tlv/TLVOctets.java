package com.zx.sms.codec.smgp.tlv;


public class TLVOctets extends TLV {
	private byte[] value = null;

	public TLVOctets() {
		super();
	}

	public TLVOctets(short p_tag) {
		super(p_tag);
	}

	public TLVOctets(short p_tag, int min, int max) {
		super(p_tag, min, max);
	}

	public TLVOctets(short p_tag, byte[] p_value) throws Exception {
		super(p_tag);
		setValueData(p_value);
	}

	public TLVOctets(short p_tag, int min, int max, byte[] p_value) throws Exception {
		super(p_tag, min, max);
		setValueData(p_value);
	}

	public void setValue(byte[] p_value) {
		value = p_value;
		markValueSet();
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public byte[] getValueData() throws Exception {
		return value;
	}

	@Override
	public void setValueData(byte[] buffer) throws Exception {
		value = buffer;
		markValueSet();

	}

}
