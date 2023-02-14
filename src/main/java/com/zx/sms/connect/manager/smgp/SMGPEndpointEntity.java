package com.zx.sms.connect.manager.smgp;

import java.nio.charset.Charset;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;

public abstract class SMGPEndpointEntity extends EndpointEntity {
    /**
	 * 
	 */
	private static final long serialVersionUID = -441048745116970563L;
	private String clientID;
    private String password;
    private byte clientVersion = 0x30;  // interface version requested by us or them
    
    private Charset chartset = GlobalConstance.defaultTransportCharset;
	public String getClientID() {
		return clientID;
	}
	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public byte getClientVersion() {
		return clientVersion;
	}
	public void setClientVersion(byte clientVersion) {
		this.clientVersion = clientVersion;
	}
	public Charset getChartset() {
		return chartset;
	}
	public void setChartset(Charset chartset) {
		this.chartset = chartset;
	}

}
