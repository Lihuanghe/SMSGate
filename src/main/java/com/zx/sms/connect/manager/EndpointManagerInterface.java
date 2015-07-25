package com.zx.sms.connect.manager;

import java.util.List;

public interface EndpointManagerInterface<T extends EndpointEntity> {
	public void openEndpoint(T entity) ;
	public void close(T entity);
	public void openAll() throws Exception;
	public void addEndpointEntity(EndpointEntity entity);
	public void remove(String id);
	public List<T> allAllEndPointEntity();
	public List<T> getEndPointEntityByGroup(String group);
	public EndpointConnector getEndpointConnector(T entity);
	public EndpointEntity getEndpointEntity(String id);
	public  void addAllEndpointEntity(List<EndpointEntity> entities);
}
