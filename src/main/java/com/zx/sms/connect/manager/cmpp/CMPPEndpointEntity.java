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
	//默认为3.0协议
	private short version = (short)0x30L;

	private short windows = 16;
	private Charset chartset = GlobalConstance.defaultTransportCharset;
	

	
	/**
	 *该端口是否支持接收长短信发送 
	 */
	private boolean supportLongMsg = GlobalConstance.isSupportLongMsg;
	
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

	@Deprecated
	public short getWindows() {
		return windows;
	}
	@Deprecated
	public void setWindows(short windows) {
		this.windows = windows;
	}

	public Charset getChartset() {
		return chartset;
	}

	public void setChartset(Charset chartset) {
		this.chartset = chartset;
	}
	
	public boolean isSupportLongMsg() {
		return supportLongMsg;
	}

	public void setSupportLongMsg(boolean supportLongMsg) {
		this.supportLongMsg = supportLongMsg;
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
