package com.zx.sms.connect.manager.smpp;

import com.zx.sms.codec.smpp.Address;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;

public abstract class SMPPEndpointEntity extends EndpointEntity {
    /**
	 * 
	 */
	private static final long serialVersionUID = -441048745116970563L;
	private String systemId;
    private String password;
    private String systemType;
    private byte interfaceVersion;  // interface version requested by us or them
    private Address addressRange;
    
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSystemType() {
		return systemType;
	}
	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}
	public byte getInterfaceVersion() {
		return interfaceVersion;
	}
	public void setInterfaceVersion(byte interfaceVersion) {
		this.interfaceVersion = interfaceVersion;
	}
	public Address getAddressRange() {
		return addressRange;
	}
	public void setAddressRange(Address addressRange) {
		this.addressRange = addressRange;
	}

}
