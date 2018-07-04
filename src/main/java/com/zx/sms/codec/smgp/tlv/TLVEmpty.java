package com.zx.sms.codec.smgp.tlv;


public class TLVEmpty extends TLV {
	private boolean present = false;

	public TLVEmpty() {
		super(0, 0);
	}

	public TLVEmpty(short p_tag) {
		super(p_tag, 0, 0);
	}

	public TLVEmpty(short p_tag, boolean p_present) {
		super(p_tag, 0, 0);
		present = p_present;
		markValueSet();
	}

	public void setValue(boolean p_present) {
		present = p_present;
		markValueSet();
	}

	public boolean getValue() {
		return present;

	}

	@Override
	public byte[] getValueData() throws Exception {
		return null;
	}

	@Override
	public void setValueData(byte[] buffer) throws Exception {
		setValue(true);
		markValueSet();
	}

}