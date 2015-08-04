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
	private String groupName;
	private String userName;
	private String password;
	//默认为3.0协议
	private short version = (short)0x30L;
	private short idleTimeSec = 30;
	private short maxRetryCnt = 3;
	private short retryWaitTimeSec=60;
	private short windows = 16;
	private Charset chartset = GlobalConstance.defaultTransportCharset;
	
	private boolean isReSendFailMsg = false; 
	
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

	public String getUserName() {
		return userName;
	}

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

	public short getIdleTimeSec() {
		return idleTimeSec;
	}

	public void setIdleTimeSec(short idleTimeSec) {
		this.idleTimeSec = idleTimeSec;
	}

	public short getMaxRetryCnt() {
		return maxRetryCnt;
	}

	public void setMaxRetryCnt(short maxRetryCnt) {
		this.maxRetryCnt = maxRetryCnt;
	}

	public short getRetryWaitTimeSec() {
		return retryWaitTimeSec;
	}

	public void setRetryWaitTimeSec(short retryWaitTimeSec) {
		this.retryWaitTimeSec = retryWaitTimeSec;
	}

	public short getWindows() {
		return windows;
	}

	public void setWindows(short windows) {
		this.windows = windows;
	}

	public Charset getChartset() {
		return chartset;
	}

	public void setChartset(Charset chartset) {
		this.chartset = chartset;
	}
	
	public boolean isReSendFailMsg() {
		return isReSendFailMsg;
	}

	public void setReSendFailMsg(boolean isReSendFailMsg) {
		this.isReSendFailMsg = isReSendFailMsg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMPPEndpointEntity other = (CMPPEndpointEntity) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CMPPEndpointEntity [groupName=" + groupName + ", userName=" + userName + ", chartset=" + chartset + ", getId()=" + getId() + ", getDesc()="
				+ getDesc() + ", getChannelType()=" + getChannelType() + ", getHost()=" + getHost() + ", getPort()=" + getPort() + ", getMaxChannels()="
				+ getMaxChannels() + "]";
	}

	
}
