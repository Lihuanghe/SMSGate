package com.zx.sms.connect.manager;

import com.chinamobile.cmos.sms.AbstractSmsDcs;

public interface SmsDcsBuilder {
	AbstractSmsDcs build(byte dcs);

}
