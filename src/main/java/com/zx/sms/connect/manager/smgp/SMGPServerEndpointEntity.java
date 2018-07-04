package com.zx.sms.connect.manager.smgp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointConnector;

public class SMGPServerEndpointEntity extends EndpointEntity implements ServerEndpoint {

	
	private Map<String,SMGPServerChildEndpointEntity> childrenEndpoint = new ConcurrentHashMap<String,SMGPServerChildEndpointEntity>() ;
	
	
	public void addchild(EndpointEntity entity)
	{
		
		childrenEndpoint.put(((SMGPServerChildEndpointEntity)entity).getClientID().trim(), (SMGPServerChildEndpointEntity)entity);
	}
	
	public void removechild(EndpointEntity entity){
		childrenEndpoint.remove(((SMGPServerChildEndpointEntity)entity).getClientID().trim());
	}
	
	public EndpointEntity getChild(String userName)
	{
		return childrenEndpoint.get(userName);
	}
	
	public List<EndpointEntity> getAllChild()
	{
		List<EndpointEntity> list = new ArrayList<EndpointEntity>();
		for(Map.Entry<String,SMGPServerChildEndpointEntity> entry : childrenEndpoint.entrySet()){
			list.add(entry.getValue());
		}
		return list;
	}
	@Override
	public SMGPServerEndpointConnector buildConnector() {
		return new SMGPServerEndpointConnector(this);
	}

}
