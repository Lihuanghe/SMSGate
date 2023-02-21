package com.zx.sms.transgate;

import com.chinamobile.cmos.sms.AbstractSmsDcs;
import com.zx.sms.connect.manager.SmsDcsBuilder;

public class TestSmsDcsBuilder implements SmsDcsBuilder {

	@Override
	public AbstractSmsDcs build(byte dcs) {
		return new TestSmsDcs(dcs);
	}

}
