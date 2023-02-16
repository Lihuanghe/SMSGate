package com.zx.sms.codec.smpp;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;

public class DefaultSmppSmsDcs extends SmppSmsDcs {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4905337688664298959L;
	public DefaultSmppSmsDcs(byte dcs) {
		super(dcs,SmsAlphabet.ASCII);
	}
	public DefaultSmppSmsDcs(byte dcs, SmsAlphabet defa) {
		super(dcs, defa);
	}
	public int getMaxMsglength() {
		switch(getAlphabet()) {
			case GSM:
			case ASCII:
			case LATIN1:
				return 160;
			default:
				return 140;
		}
	}
}
