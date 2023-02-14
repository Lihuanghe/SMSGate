package com.zx.sms.transgate;

import org.apache.commons.lang3.RandomUtils;

import com.chinamobile.cmos.sms.SmsDcs;

public class TestSmsDcs extends SmsDcs{
	
	public TestSmsDcs(byte dcs) {
		super(dcs);
	}

	public int getMaxMsglength() {
		return RandomUtils.nextInt(70,140);
	}
}
