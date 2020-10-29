package com.zx.sms.connect.manager;

import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.Set;

public interface EndpointManagerInterface{
	public ChannelFuture openEndpoint(EndpointEntity entity) ;
	public void close(EndpointEntity entity);
	public List<ChannelFuture> openAll() throws Exception;
	public void addEndpointEntity(EndpointEntity entity);
	public void remove(String id);
	public Set<EndpointEntity> allEndPointEntity();
	public EndpointEntity getEndpointEntity(String id);
	public EndpointConnector getEndpointConnector(EndpointEntity entity);
	public EndpointConnector getEndpointConnector(String entity);
	public void addAllEndpointEntity(List<EndpointEntity> entities);
	public void close();

	public void startConnectionCheckTask(); //启动自动重连任务
	public void stopConnectionCheckTask();  //关闭自动重连任务
}
