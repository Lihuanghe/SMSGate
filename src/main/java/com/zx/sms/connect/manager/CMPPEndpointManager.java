package com.zx.sms.connect.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;

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
			synchronized (this) {

				List<CMPPEndpointEntity> list = groupMap.get(cmppentity.getGroupName());
				if (list == null) {
					list = new ArrayList<CMPPEndpointEntity>();
					groupMap.put(cmppentity.getGroupName(), list);
				}
				list.add(cmppentity);
			}
		} else if (entity instanceof CMPPServerEndpointEntity) {

			CMPPServerEndpointEntity serverentity = (CMPPServerEndpointEntity) entity;
			for (CMPPServerChildEndpointEntity child : serverentity.getAllChild()) {
				
				manager.addEndpointEntity(child);
				
				synchronized (this) {
					List<CMPPEndpointEntity> list = groupMap.get(child.getGroupName());
					if (list == null) {
						list = new ArrayList<CMPPEndpointEntity>();
						groupMap.put(child.getGroupName(), list);
					}
					list.add(child);
				}
			}
		}
	}

	@Override
	public List<EndpointEntity> allEndPointEntity() {
		return manager.allEndPointEntity();
	}

	public List<CMPPEndpointEntity> getEndPointEntityByGroup(String group) {

		return groupMap.get(group);
	}

	@Override
	public EndpointConnector getEndpointConnector(EndpointEntity entity) {

		return manager.getEndpointConnector(entity);
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

}
