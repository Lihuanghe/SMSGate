package com.zx.sms.connect.manager;

import io.netty.util.concurrent.Future;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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

	private volatile boolean started = false;

	public synchronized void openEndpoint(EndpointEntity entity) {
		if (!entity.isValid())
			return;

		EndpointEntity old = idMap.get(entity.getId());
		if (old == null) {
			addEndpointEntity(entity);
		}

		EndpointConnector<?> conn = entity.getSingletonConnector();

		try {
			conn.open();
		} catch (Exception e) {
			logger.error("Open Endpoint Error. {}", entity, e);
		}
	}

	public synchronized void close(EndpointEntity entity) {
		EndpointConnector<?> conn = entity.getSingletonConnector();
		if (conn == null)
			return;
		try {
			conn.close();

		} catch (Exception e) {
			logger.error("close Error", e);
		}
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

	public void addAllEndpointEntity(List<EndpointEntity> entities) {
		if (entities == null || entities.size() == 0)
			return;
		for (EndpointEntity entity : entities) {
			if (entity.isValid())
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

	public void close() {
		stopConnectionCheckTask();
		for (EndpointEntity en : endpoints) {
			close(en);
		}
		
	}

	public void stopConnectionCheckTask() {
		started = false;
	}

	public void startConnectionCheckTask() {
		if (started)
			return;

		started = true;
		// 每秒检查一次所有连接，不足数目的就新建一个连接
		EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				for (Map.Entry<String, EndpointEntity> entry : idMap.entrySet()) {

					EndpointEntity entity = entry.getValue();
					if (entity instanceof ClientEndpoint) {
						EndpointConnector conn = entity.getSingletonConnector();
						int max = entity.getMaxChannels();
						int actual = conn.getConnectionNum();
						// 客户端重连
						if (actual < max) {
							logger.debug("open connection {}", entity);
							conn.open();
						}
					}
				}
				return started;
			}

		}, new ExitUnlimitCirclePolicy<Boolean>() {

			@Override
			public boolean notOver(Future<Boolean> future) {
				return started;
			}

		}, 1000);
	}

	@Override
	public EndpointConnector getEndpointConnector(EndpointEntity entity) {

		return entity.getSingletonConnector();
	}

	@Override
	public EndpointConnector getEndpointConnector(String id) {
		EndpointEntity entity = getEndpointEntity(id);
		return entity == null ? null : entity.getSingletonConnector();
	}
}
