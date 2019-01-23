package org.marre.sms;

import java.io.Serializable;

public class SmsSimTookitSecurityMessage extends SmsConcatMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3788194337517378301L;
	
	private byte udhIei ;
	private byte[] udhIeiData ;
	private byte[] ud;
	public SmsSimTookitSecurityMessage(byte udhIei,byte[] udhIeiData,byte[] ud) {
		this.udhIei = udhIei;
		this.ud = ud;
		this.udhIeiData = udhIeiData;
	}

	@Override
	public SmsUserData getUserData() {
		return new SmsUserData(ud,ud.length,new SmsDcs((byte)0xf6));
	}

	@Override
	public SmsUdhElement[] getUdhElements() {
		return new SmsUdhElement[] {new SmsUdhElement(SmsUdhIei.valueOf(udhIei),udhIeiData)};
	}
	@Override
	public String toString() {
		return SmsSimTookitSecurityMessage.class.toString();
	}
}
