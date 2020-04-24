package com.zx.sms.connect.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;

public enum CMPPEndpointManager implements EndpointManagerInterface {
	INS;
	private EndpointManager manager = EndpointManager.INS;
	private ConcurrentHashMap<String, List<CMPPEndpointEntity>> groupMap = new ConcurrentHashMap<String, List<CMPPEndpointEntity>>();

	@Override
	public void openEndpoint(EndpointEntity entity) {
		manager.openEndpoint(entity);
	}

	@Override
	public void close(EndpointEntity entity) {
		manager.close(entity);

	}

	@Override
	public void openAll() throws Exception {
		manager.openAll();
	}

	public void addEndpointEntity(EndpointEntity entity) {

		manager.addEndpointEntity(entity);

		// 端口按group分组，方便按省份转发处理
		if (entity instanceof CMPPEndpointEntity) {
			CMPPEndpointEntity cmppentity = (CMPPEndpointEntity) entity;

			List<CMPPEndpointEntity> list = groupMap.get(cmppentity.getGroupName());
			if (list == null) {
				list = new ArrayList<CMPPEndpointEntity>();
				List<CMPPEndpointEntity> old = groupMap.putIfAbsent(cmppentity.getGroupName(), list);
				list = old == null ? list : old;
			}
			list.add(cmppentity);
		}
	}

	@Override
	public Set<EndpointEntity> allEndPointEntity() {
		return manager.allEndPointEntity();
	}

	public List<CMPPEndpointEntity> getEndPointEntityByGroup(String group) {

		return groupMap.get(group);
	}

	@Override
	public void remove(String id) {
		manager.remove(id);
	}

	@Override
	public EndpointEntity getEndpointEntity(String id) {
		return manager.getEndpointEntity(id);
	}

	public void addAllEndpointEntity(List<EndpointEntity> entities) {
		if (entities == null || entities.size() == 0)
			return;
		for (EndpointEntity entity : entities) {
			if (entity.isValid())
				addEndpointEntity(entity);
		}
	}

	public void close() {
		manager.close();
	}

	@Override
	public void startConnectionCheckTask() {
		manager.startConnectionCheckTask();
	}

	@Override
	public void stopConnectionCheckTask() {
		manager.stopConnectionCheckTask();
	}

	@Override
	public EndpointConnector getEndpointConnector(EndpointEntity entity) {
		return entity.getSingletonConnector();
	}

	@Override
	public EndpointConnector getEndpointConnector(String id) {
		return manager.getEndpointConnector(id);
	}
}
