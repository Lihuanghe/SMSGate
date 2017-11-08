package com.zx.sms.connect.manager.smpp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointConnector;

public class SMPPServerEndpointEntity extends EndpointEntity implements ServerEndpoint {

	
	private Map<String,SMPPServerChildEndpointEntity> childrenEndpoint = new ConcurrentHashMap<String,SMPPServerChildEndpointEntity>() ;
	
	
	public void addchild(EndpointEntity entity)
	{
		
		childrenEndpoint.put(((SMPPServerChildEndpointEntity)entity).getSystemId().trim(), (SMPPServerChildEndpointEntity)entity);
	}
	
	public void removechild(EndpointEntity entity){
		childrenEndpoint.remove(((SMPPServerChildEndpointEntity)entity).getSystemId().trim());
	}
	
	public EndpointEntity getChild(String userName)
	{
		return childrenEndpoint.get(userName);
	}
	
	public List<EndpointEntity> getAllChild()
	{
		List<EndpointEntity> list = new ArrayList<EndpointEntity>();
		for(Map.Entry<String,SMPPServerChildEndpointEntity> entry : childrenEndpoint.entrySet()){
			list.add(entry.getValue());
		}
		return list;
	}
	@Override
	public SMPPServerEndpointConnector buildConnector() {
		return new SMPPServerEndpointConnector(this);
	}

}
