package com.zx.sms.connect.manager.sgip;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;

public abstract class SgipEndpointEntity extends EndpointEntity {
    /**
	 * 
	 */
	private static final long serialVersionUID = -441048745116970563L;
	private String loginName = GlobalConstance.emptyString;
	private String loginPassowrd = GlobalConstance.emptyString;

	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getLoginPassowrd() {
		return loginPassowrd;
	}
	public void setLoginPassowrd(String loginPassowrd) {
		this.loginPassowrd = loginPassowrd;
	}    

}
