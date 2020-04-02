package com.zx.sms.connect.manager.smgp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;

public class SMGPServerEndpointEntity extends EndpointEntity implements ServerEndpoint {

	private static final long serialVersionUID = -1360261807667249348L;
	private Map<String,Map<ChannelType,SMGPServerChildEndpointEntity>> childrenEndpoint = new ConcurrentHashMap<String,Map<ChannelType,SMGPServerChildEndpointEntity>>() ;
	
	
	public void addchild(EndpointEntity entity)
	{
		String username = ((SMGPServerChildEndpointEntity)entity).getClientID().trim();
		if(childrenEndpoint.containsKey(username)) {
			childrenEndpoint.get(username).put(entity.getChannelType()==null ?ChannelType.DUPLEX:entity.getChannelType(), (SMGPServerChildEndpointEntity)entity);
		}else {
			HashMap<ChannelType,SMGPServerChildEndpointEntity> child = new HashMap<ChannelType,SMGPServerChildEndpointEntity>();
			child.put(entity.getChannelType()==null ?ChannelType.DUPLEX:entity.getChannelType(), (SMGPServerChildEndpointEntity)entity);
			childrenEndpoint.put(username, child);
		}
	}
	
	public void removechild(EndpointEntity entity){
		childrenEndpoint.remove(((SMGPServerChildEndpointEntity)entity).getClientID().trim());
	}
	public EndpointEntity getChild(String userName,ChannelType chType)
	{
		return childrenEndpoint.get(userName).get(chType);
	}
	public EndpointEntity getChild(String userName)
	{
		return null;
	}
	
	public List<EndpointEntity> getAllChild()
	{
		List<EndpointEntity> list = new ArrayList<EndpointEntity>();
		for(Map.Entry<String,Map<ChannelType,SMGPServerChildEndpointEntity>> entryMap : childrenEndpoint.entrySet()){
			for(Map.Entry<ChannelType,SMGPServerChildEndpointEntity> entry : entryMap.getValue().entrySet()){
				list.add(entry.getValue());
			}
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	protected SMGPServerEndpointConnector buildConnector() {
		return new SMGPServerEndpointConnector(this);
	}

}
