package com.zx.sms.connect.manager;

import java.util.List;

public interface EndpointManagerInterface{
	public void openEndpoint(EndpointEntity entity) ;
	public void close(EndpointEntity entity);
	public void openAll() throws Exception;
	public void addEndpointEntity(EndpointEntity entity);
	public void remove(String id);
	public List<EndpointEntity> allEndPointEntity();
	public EndpointConnector getEndpointConnector(EndpointEntity entity);
	public EndpointEntity getEndpointEntity(String id);
	public void addAllEndpointEntity(List<EndpointEntity> entities);
	public void close();
}
