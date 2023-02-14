package com.zx.sms.connect.manager;

import java.util.List;
import java.util.Set;

public interface EndpointManagerInterface{
	 void openEndpoint(EndpointEntity entity) ;
	 void close(EndpointEntity entity);
	 void openAll() throws Exception;
	 void addEndpointEntity(EndpointEntity entity);
	 void remove(String id);
	 void removeAll();
	 Set<EndpointEntity> allEndPointEntity();
	 EndpointEntity getEndpointEntity(String id);
	 EndpointConnector getEndpointConnector(EndpointEntity entity);
	 EndpointConnector getEndpointConnector(String entity);
	 void addAllEndpointEntity(List<EndpointEntity> entities);
	 void close();
	
	 void startConnectionCheckTask(); //启动自动重连任务
	 void stopConnectionCheckTask();  //关闭自动重连任务
}
