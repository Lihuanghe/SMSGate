package org.marre.sms;

import org.apache.commons.codec.binary.Hex;

public class SmsUnkownTypeMessage implements SmsMessage {

	private byte[] ud;
	private byte dcs ;
	
	public SmsUnkownTypeMessage(byte dcs,byte[] ud) {
		this.ud = ud;
		this.dcs = dcs;
	}
	@Override
	public SmsPdu[] getPdus() {
		SmsUserData sud = new SmsUserData(ud,ud.length,new SmsDcs(dcs));
		return new SmsPdu[] {new SmsPdu(new SmsUdhElement[] {},sud)};
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SmsUnkownTypeMessage:0x").append(Hex.encodeHexString(ud));
		return sb.toString();
	}
	
	
}
