package com.zx.sms.connect.manager.cmpp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public class CMPPServerEndpointEntity extends CMPPEndpointEntity implements ServerEndpoint {

	private Map<String,CMPPServerChildEndpointEntity> childrenEndpoint = new ConcurrentHashMap<String,CMPPServerChildEndpointEntity>() ;
	
	
	public void addchild(CMPPServerChildEndpointEntity entity)
	{
		
		childrenEndpoint.put(entity.getUserName().trim(), entity);
	}
	
	public void removechild(CMPPServerChildEndpointEntity entity){
		childrenEndpoint.remove(entity.getUserName());
	}
	
	public CMPPServerChildEndpointEntity getChild(String userName)
	{
		return childrenEndpoint.get(userName);
	}
	
	public List<CMPPServerChildEndpointEntity> getAllChild()
	{
		List<CMPPServerChildEndpointEntity> list = new ArrayList<CMPPServerChildEndpointEntity>();
		for(Map.Entry<String,CMPPServerChildEndpointEntity> entry : childrenEndpoint.entrySet()){
			list.add(entry.getValue());
		}
		return list;
	}
	@Override
	public CMPPServerEndpointConnector buildConnector() {
		return new CMPPServerEndpointConnector(this);
	}

}
