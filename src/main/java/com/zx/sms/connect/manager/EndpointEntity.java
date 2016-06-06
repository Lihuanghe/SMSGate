package com.zx.sms.connect.manager;

import java.io.Serializable;
import java.util.List;

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
    
	private ChannelType channelType;
	private String host;
	private Integer port;

	/**
	 *最大连接数 
	 */
	private short maxChannels;
	/**
	 *端口是否可用
	 */
	private boolean valid = true;
	
	/**
	 *该端口业务处理的handler集合， 
	 **/
	private List<BusinessHandlerInterface> businessHandlerSet;
	
	/**
	 *流量整形 
	 */
	private int readLimit = 0;
	private int writeLimit = 0;
	
	private boolean useSSL = false;
    
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
	public boolean isUseSSL() {
		return useSSL;
	}
	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}
	/**
     *表示TCP连接是单工，或者又工
     */
    public enum ChannelType {UP,DOWN,DUPLEX};
    
    
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
	
	abstract public  <T extends EndpointConnector<EndpointEntity>> T buildConnector();
	@Override
	public String toString() {
		return "EndpointEntity [Id=" + Id + ", Desc=" + Desc + ", channelType="
				+ channelType + ", host=" + host + ", port=" + port
				+ ", maxChannels="
				+ maxChannels + ", valid=" + valid + ", businessHandlerSet="
				+ businessHandlerSet + "]";
	}
	
}
