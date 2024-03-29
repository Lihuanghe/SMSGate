package com.zx.sms.connect.manager.cmpp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chinamobile.cmos.sms.AbstractSmsDcs;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerServerEndpoint;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public class CMPPServerEndpointEntity extends EndpointEntity implements ServerServerEndpoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7570800382297773838L;
	private Map<String,CMPPServerChildEndpointEntity> childrenEndpoint = new ConcurrentHashMap<String,CMPPServerChildEndpointEntity>() ;
	
	
	public void addchild(EndpointEntity entity)
	{
		
		childrenEndpoint.put(((CMPPServerChildEndpointEntity)entity).getUserName().trim(), (CMPPServerChildEndpointEntity)entity);
	}
	
	public void removechild(EndpointEntity entity){
		childrenEndpoint.remove(((CMPPServerChildEndpointEntity)entity).getUserName());
	}
	
	public EndpointEntity getChild(String userName)
	{
		return childrenEndpoint.get(userName);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CMPPServerEndpointConnector buildConnector() {
		return new CMPPServerEndpointConnector(this);
	}

	@Override
	public EndpointEntity getChild(String userName, ChannelType chType) {
		return null;
	}
	
	protected AbstractSmsDcs buildSmsDcs(byte dcs) {
		return null;
	}

}
