package com.zx.sms.connect.manager.smpp;

import com.chinamobile.cmos.sms.AbstractSmsDcs;
import com.chinamobile.cmos.sms.SmsAlphabet;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.DefaultSmppSmsDcs;
import com.zx.sms.codec.smpp.SmppSplitType;
import com.zx.sms.connect.manager.EndpointEntity;

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
    
    
    /**
     *SMPP协议当 dcs Code为0时的默认的字符集 
     */
    private SmsAlphabet defauteSmsAlphabet = SmsAlphabet.ASCII;
    
    /**
     * 设置消息payLoad是放在UD里还是 OptionParameter里
     */
    
    private SmppSplitType splitType = SmppSplitType.UDH;
    
	@Override
	protected AbstractSmsDcs buildSmsDcs(byte dcs) { 
		return new DefaultSmppSmsDcs(dcs,defauteSmsAlphabet);
	}

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
	public SmsAlphabet getDefauteSmsAlphabet() {
		return defauteSmsAlphabet;
	}
	public void setDefauteSmsAlphabet(SmsAlphabet defauteSmsAlphabet) {
		this.defauteSmsAlphabet = defauteSmsAlphabet;
	}
	public SmppSplitType getSplitType() {
		//smpp34才支持OptionParameter
		return getInterfaceVersion() < 0x34 ? SmppSplitType.UDH : splitType;
	}
	public void setSplitType(SmppSplitType splitType) {
		this.splitType = splitType;
	}
	
}
