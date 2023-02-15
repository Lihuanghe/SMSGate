package com.zx.sms.common;

import com.chinamobile.cmos.sms.SmppSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;

public class MySmppSmsDcs extends SmppSmsDcs{
	public MySmppSmsDcs(byte dcs) {
		super(dcs);
	}
	public MySmppSmsDcs(byte dcs, SmsAlphabet defa) {
		super(dcs, defa);
	}

}