package com.zx.sms.connect.manager.smpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;

public class SMPPServerEndpointEntity extends EndpointEntity implements ServerEndpoint {
	private static final long serialVersionUID = -1247226404595679209L;
	private Map<String,Map<ChannelType,SMPPServerChildEndpointEntity>> childrenEndpoint = new ConcurrentHashMap<String,Map<ChannelType,SMPPServerChildEndpointEntity>>() ;
	
	
	public void addchild(EndpointEntity entity)
	{
		
		String username = ((SMPPServerChildEndpointEntity)entity).getSystemId().trim();
		if(childrenEndpoint.containsKey(username)) {
			childrenEndpoint.get(username).put(entity.getChannelType()==null ?ChannelType.DUPLEX:entity.getChannelType(), (SMPPServerChildEndpointEntity)entity);
		}else {
			HashMap<ChannelType,SMPPServerChildEndpointEntity> child = new HashMap<ChannelType,SMPPServerChildEndpointEntity>();
			child.put(entity.getChannelType()==null ?ChannelType.DUPLEX:entity.getChannelType(), (SMPPServerChildEndpointEntity)entity);
			childrenEndpoint.put(username, child);
		}
		
	}
	
	public void removechild(EndpointEntity entity){
		childrenEndpoint.remove(((SMPPServerChildEndpointEntity)entity).getSystemId().trim());
	}
	
	public EndpointEntity getChild(String userName)
	{
		return null;
	}
	public EndpointEntity getChild(String userName,ChannelType chType)
	{
		return childrenEndpoint.get(userName).get(chType);
	}
	
	public List<EndpointEntity> getAllChild()
	{
		List<EndpointEntity> list = new ArrayList<EndpointEntity>();
		for(Map.Entry<String,Map<ChannelType,SMPPServerChildEndpointEntity>> entryMap : childrenEndpoint.entrySet()){
			for(Map.Entry<ChannelType,SMPPServerChildEndpointEntity> entry : entryMap.getValue().entrySet()){
				list.add(entry.getValue());
			}
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	protected SMPPServerEndpointConnector buildConnector() {
		return new SMPPServerEndpointConnector(this);
	}

}
