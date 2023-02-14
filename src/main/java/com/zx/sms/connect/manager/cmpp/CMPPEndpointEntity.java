package com.zx.sms.connect.manager.cmpp;

import java.nio.charset.Charset;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public abstract class CMPPEndpointEntity extends EndpointEntity {

	private static final long serialVersionUID = -6571699260501337643L;
	
	private long liftTime;
	private String groupName="";
	
	private String userName;
	private String password;
	
	
	private String spCode = ""; 
	private String serviceId = ""; //服务代码
	private String msgSrc = ""; //企业代码，可能跟userName相同
	//默认为3.0协议
	private short version = (short)0x30L;
	private Charset chartset = GlobalConstance.defaultTransportCharset;
	
	/**
	 * 最大消息序列数
	 */
	private short maxMsgQueue;
	
	public long getLiftTime() {
		return liftTime;
	}

	public void setLiftTime(long liftTime) {
		this.liftTime = liftTime;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	/**
	 * 业务代码 ：填写进submit的service_id里
	 */
	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	/**
	 * 企业代码 ：填写进submit的msg_src里
	 */
	public String getMsgSrc() {
		return msgSrc;
	}

	public void setMsgSrc(String msgSrc) {
		this.msgSrc = msgSrc;
	}

	/**
	 *服务代码：如10696101 
	 */
	public String getSpCode() {
		return spCode;
	}

	/**
	 *服务代码：如10696101 
	 */
	public void setSpCode(String spCode) {
		this.spCode = spCode;
	}
	/**
	 *企业代码：如902104
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 *企业代码：如902104
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public Charset getChartset() {
		return chartset;
	}

	public void setChartset(Charset chartset) {
		this.chartset = chartset;
	}
	
    public short getMaxMsgQueue() {
		return maxMsgQueue;
	}
	public void setMaxMsgQueue(short maxMsgQueue) {
		this.maxMsgQueue = maxMsgQueue;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"[groupName=" + groupName + ", userName=" + userName + ", chartset=" + chartset + ", getId()=" + getId() + ", getDesc()="
				+ getDesc() + ", getChannelType()=" + getChannelType() + ", getHost()=" + getHost() + ", getPort()=" + getPort() + ", getMaxChannels()="
				+ getMaxChannels() + "]";
	}

	
}
