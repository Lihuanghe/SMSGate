package com.zx.sms.connect.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lihuanghe(18852780@qq.com) 系统连接的统一管理器，负责连接服务端，或者开启监听端口，等客户端连接 。
 */
public enum EndpointManager implements EndpointManagerInterface {
	INS;
	private static final Logger logger = LoggerFactory.getLogger(EndpointManager.class);

	private Set<EndpointEntity> endpoints = Collections.synchronizedSet(new HashSet<EndpointEntity>());
	
	private ConcurrentHashMap<String, EndpointEntity> idMap = new ConcurrentHashMap<String, EndpointEntity>();

	private ConcurrentHashMap<String, EndpointConnector> map = new ConcurrentHashMap<String, EndpointConnector>();

	public synchronized void openEndpoint(EndpointEntity entity) {
		if(!entity.isValid())  return ;
		
		EndpointEntity old = idMap.get(entity.getId());
		if (old == null) {
			addEndpointEntity(entity);
		}
		
		EndpointConnector conn = map.get(entity.getId());
		if (conn == null)
			conn = entity.buildConnector();

		try {
			conn.open();
			map.put(entity.getId(), conn);
		} catch (Exception e) {
			logger.error("Open Endpoint Error. {}", entity, e);
		}
	}

	public synchronized void close(EndpointEntity entity) {
		EndpointConnector conn = map.get(entity.getId());
		if (conn == null)
			return;
		try {
			conn.close();
			//关闭所有连接，并把Connector删掉
			map.remove(entity.getId());

		} catch (Exception e) {
			logger.error("close Error", e);
		}
	}

	public EndpointConnector getEndpointConnector(EndpointEntity entity) {
		return map.get(entity.getId());
	}
	public EndpointConnector getEndpointConnector(String entityId) {
		return map.get(entityId);
	}
	public EndpointEntity getEndpointEntity(String id) {
		return idMap.get(id);
	}

	public void openAll() throws Exception {
		for (EndpointEntity e : endpoints)
			openEndpoint(e);
	}

	public synchronized void addEndpointEntity(EndpointEntity entity) {
		endpoints.add(entity);
		idMap.put(entity.getId(), entity);
	}
	
	public  void addAllEndpointEntity(List<EndpointEntity> entities) {
		if(entities==null||entities.size()==0) return;
		for(EndpointEntity entity : entities){
			if(entity.isValid())
				addEndpointEntity(entity);
		}
	}

	public Set<EndpointEntity> allEndPointEntity() {
		return endpoints;
	}

	@Override
	public synchronized void remove(String id) {
		EndpointEntity entity = idMap.remove(id);
		if (entity != null) {
			endpoints.remove(entity);
			close(entity);
		}
	}
	
	public void close(){
		 for(EndpointEntity en : endpoints)
		 {
			 close(en);
		 }
	}

}
