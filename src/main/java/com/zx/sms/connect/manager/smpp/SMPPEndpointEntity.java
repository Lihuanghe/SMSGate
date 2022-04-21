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
    
    //因为中文的SMPP协议文件翻译有误，造成有部分SMPP网关在设置Submit 及 Deliver消息时要求 
    // short_message 字段必须是为 0 结尾的字节
    //为兼容此问题，增加这样一个配置。当 配置为true时，自动处理short_message字段尾部的0
    private boolean isAddZeroByte = false;
    
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
	public boolean isAddZeroByte() {
		return isAddZeroByte;
	}
	public void setAddZeroByte(boolean isAddZeroByte) {
		this.isAddZeroByte = isAddZeroByte;
	}
}
