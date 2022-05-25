package com.zx.sms.connect.manager;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.zx.sms.handler.api.BusinessHandlerInterface;

/**
 * @author Lihuanghe(18852780@qq.com)
 * 代表一个TCP端口。或是客户端，或者是服务端
 */
public abstract class EndpointEntity implements Serializable {
	static final long serialVersionUID = 42L;
	/**
	 *唯一ID 
	 */
	private String Id;
	/**
	 *端口的描述
	 */
	private String Desc;
    
	private ChannelType channelType = ChannelType.DUPLEX;
	private String host;
	private Integer port;
	
	private String localhost;
	private Integer localport;

	/**
	 *最大连接数 
	 */
	private short maxChannels;
	/**
	 *端口是否可用
	 */
	private boolean valid = true;
	
	/**
	 *该端口是否支持接收长短信发送 
	 */
	private SupportLongMessage supportLongmsg = SupportLongMessage.BOTH;
	
	/**
	 *NONE : 接收与发送都不处理长短信 <br/>
	 *BOTH：接收与发送都处理长短信，自动合并，拆分<br/>
	 *SEND：发送是自动拆分长短信<br/>
	 *RECV ：接收时自动合并长短信<br/>
	 */
	public enum SupportLongMessage {NONE,SEND,RECV,BOTH};
	
	/**
     *表示TCP连接是单工，或者双工
     */
    public enum ChannelType {UP,DOWN,DUPLEX};
	/**
	 *该端口业务处理的handler集合， 
	 **/
	private List<BusinessHandlerInterface> businessHandlerSet;
	
	/**
	 * 是否将未收到response的消息保存在文件，以等待进程重启后读取文件中未收到response的消息进行重发
	 * 如果为true，则可能重复发送。
	 **/
	private boolean isReSendFailMsg = false; 
	
	
	/**
	 * 发送request后等待retryWaitTimeSec 秒未收到response，会重发一次请求
	 * 本参数设置总的发送次数。默认发3次
	 **/
	private short maxRetryCnt = 3;
	
	/**
	 * 发送request后，等待多少秒未收到response时再一次发送请求
	 **/
	private short retryWaitTimeSec=60;
	
	/**
	 * 连接空闲检测周期
	 **/
	private short idleTimeSec = 30;
	
	
	/**
	 * 重发request超过maxRetryCnt重试次数后，是否关闭channel
	 **/
	boolean closeWhenRetryFailed = true;  //
	/**
	 *流量整形 ，设置发送消息的速度，单位条
	 */
	private int readLimit = 0;
	
	/**
	 *流量整形 ，设置接收消息的速度，单位条
	 */
	private int writeLimit = 0;
	
	/**
	 * 是否使用SSL对连接进行加密传输
	 */
	private boolean useSSL = false;
	
	/**
	 * 设置代理地址。通过正向代理发起连接
	 */
	private String proxy;
	
	private volatile EndpointConnector connector;
	
	/**
	 *增加客户端IP校验配置 
	 */
	private List<String> allowedAddr;
	
    public String getProxy() {
		return proxy;
	}
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}
	public int getReadLimit() {
		return readLimit;
	}
	public void setReadLimit(int readLimit) {
		this.readLimit = readLimit;
	}
	public int getWriteLimit() {
		return writeLimit;
	}
	public void setWriteLimit(int writeLimit) {
		this.writeLimit = writeLimit;
	}
	public short getIdleTimeSec() {
		return idleTimeSec;
	}

	public void setIdleTimeSec(short idleTimeSec) {
		this.idleTimeSec = idleTimeSec;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getDesc() {
		return Desc;
	}
	public void setDesc(String desc) {
		Desc = desc;
	}
	public ChannelType getChannelType() {
		return channelType;
	}
	public void setChannelType(ChannelType channelType) {
		this.channelType = channelType;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	
	
	public String getLocalhost() {
		return localhost;
	}
	public void setLocalhost(String localhost) {
		this.localhost = localhost;
	}
	public Integer getLocalport() {
		return localport;
	}
	public void setLocalport(Integer localport) {
		this.localport = localport;
	}
	public boolean isUseSSL() {
		return useSSL;
	}
	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}
    
    public short getMaxChannels() {
		return maxChannels;
	}
	public void setMaxChannels(short maxChannels) {
		this.maxChannels = maxChannels;
	}
	
	public List<BusinessHandlerInterface> getBusinessHandlerSet() {
		return businessHandlerSet;
	}
	public void setBusinessHandlerSet(List<BusinessHandlerInterface> businessHandlerSet) {
		this.businessHandlerSet = businessHandlerSet;
	}
	
	
	public boolean isReSendFailMsg() {
		return isReSendFailMsg;
	}

	public void setReSendFailMsg(boolean isReSendFailMsg) {
		this.isReSendFailMsg = isReSendFailMsg;
	}
	
	public short getMaxRetryCnt() {
		return maxRetryCnt;
	}

	public void setMaxRetryCnt(short maxRetryCnt) {
		
		this.maxRetryCnt = maxRetryCnt < 0 ? 0 : maxRetryCnt;
	}
	public short getRetryWaitTimeSec() {
		return retryWaitTimeSec;
	}

	public void setRetryWaitTimeSec(short retryWaitTimeSec) {
		this.retryWaitTimeSec = retryWaitTimeSec;
	}
	
	public SupportLongMessage getSupportLongmsg() {
		return supportLongmsg;
	}
	public void setSupportLongmsg(SupportLongMessage supportLongmsg) {
		this.supportLongmsg = supportLongmsg;
	}
	
	public boolean isCloseWhenRetryFailed() {
		return closeWhenRetryFailed;
	}
	public void setCloseWhenRetryFailed(boolean closeWhenRetryFailed) {
		this.closeWhenRetryFailed = closeWhenRetryFailed;
	}
	
	
	public List<String> getAllowedAddr() {
		return allowedAddr;
	}
	public void setAllowedAddr(List<String> allowedAddr) {
		this.allowedAddr = allowedAddr;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
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
		EndpointEntity other = (EndpointEntity) obj;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		return true;
	}
	public  <T extends EndpointConnector<EndpointEntity>> T getSingletonConnector() {
		if(connector!=null) {
			return (T)connector;
		}else {
			synchronized (this) {
				if(connector!=null) {
					return (T)connector;
				}else {
					connector = buildConnector();
					return (T)connector;
				}
			}
		}
	}
	
	abstract protected <T extends EndpointConnector<EndpointEntity>> T buildConnector();
	@Override
	public String toString() {
		return "EndpointEntity [Id=" + Id + ", Desc=" + Desc + ", channelType="
				+ channelType + ", host=" + host + ", port=" + port
				+ ", maxChannels="
				+ maxChannels + ", valid=" + valid + ", businessHandlerSet="
				+ businessHandlerSet + "]";
	}
	
}
