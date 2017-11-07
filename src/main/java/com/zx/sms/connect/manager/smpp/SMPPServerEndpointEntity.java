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
	
	
	public void addchild(SMPPServerChildEndpointEntity entity)
	{
		
		childrenEndpoint.put(entity.getSystemId().trim(), entity);
	}
	
	public void removechild(SMPPServerChildEndpointEntity entity){
		childrenEndpoint.remove(entity.getSystemId().trim());
	}
	
	public SMPPServerChildEndpointEntity getChild(String userName)
	{
		return childrenEndpoint.get(userName);
	}
	
	public List<SMPPServerChildEndpointEntity> getAllChild()
	{
		List<SMPPServerChildEndpointEntity> list = new ArrayList<SMPPServerChildEndpointEntity>();
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
