package com.zx.sms.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.dao.EndPointEntityDao;
import com.zx.sms.dao.HandlesDao;
import com.zx.sms.dao.ServerEndEntityDao;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.service.LoadConfigFromDBService;
@Component
public class LoadConfigFromDBServiceImpl implements LoadConfigFromDBService{

	@Autowired
	private ServerEndEntityDao serverEndEntityDao;
	
	@Autowired
	private EndPointEntityDao endPointEntityDao;
	@Autowired
	private HandlesDao handlesDao;
	
	@Override
	public List<CMPPServerEndpointEntity> loadServerEndpointEntity() {
		 
		 List<CMPPServerEndpointEntity> results = new ArrayList<CMPPServerEndpointEntity>();
		 //读取服务器表
		 List<CMPPServerEndpointEntity> serverEntitys=serverEndEntityDao.queryAll();
			for (CMPPServerEndpointEntity endpointEntity : serverEntitys) {
				CMPPServerEndpointEntity serverEndpointEntity = new CMPPServerEndpointEntity();
				serverEndpointEntity.setId(endpointEntity.getId());
				serverEndpointEntity.setDesc(endpointEntity.getDesc());
				serverEndpointEntity.setHost(endpointEntity.getHost());
				serverEndpointEntity.setPort(endpointEntity.getPort());
				serverEndpointEntity.setValid(endpointEntity.isValid());
				serverEndpointEntity.setMaxChannels(endpointEntity.getMaxChannels());
				serverEndpointEntity.setChannelType(endpointEntity.getChannelType());
				//读取客户端表
				List<CMPPEndpointEntity> endPointEntitys=endPointEntityDao.getById(endpointEntity.getId());
				for (CMPPEndpointEntity cmppEndpointEntity : endPointEntitys) {
					CMPPClientEndpointEntity cMPPClientEndpointEntity = new CMPPClientEndpointEntity();
					cMPPClientEndpointEntity.setId(cmppEndpointEntity.getId());
					cMPPClientEndpointEntity.setDesc(cmppEndpointEntity.getDesc());
					cMPPClientEndpointEntity.setValid(cmppEndpointEntity.isValid());
					cMPPClientEndpointEntity.setGroupName(cmppEndpointEntity.getGroupName());
					cMPPClientEndpointEntity.setHost(cmppEndpointEntity.getHost());
					cMPPClientEndpointEntity.setPort(cmppEndpointEntity.getPort());
					cMPPClientEndpointEntity.setUserName(cmppEndpointEntity.getUserName());
					cMPPClientEndpointEntity.setPassword(cmppEndpointEntity.getPassword());
					cMPPClientEndpointEntity.setVersion(cmppEndpointEntity.getVersion());
					cMPPClientEndpointEntity.setIdleTimeSec(cmppEndpointEntity.getIdleTimeSec());
					cMPPClientEndpointEntity.setLiftTime(cmppEndpointEntity.getLiftTime());
					cMPPClientEndpointEntity.setMaxRetryCnt(cmppEndpointEntity.getMaxRetryCnt());
					cMPPClientEndpointEntity.setRetryWaitTimeSec(cmppEndpointEntity.getRetryWaitTimeSec());
					cMPPClientEndpointEntity.setMaxChannels(cmppEndpointEntity.getMaxChannels());
					cMPPClientEndpointEntity.setWindows(cmppEndpointEntity.getWindows());
					cMPPClientEndpointEntity.setChartset(cmppEndpointEntity.getChartset());
					//读取handle表
					List<Class<BusinessHandlerInterface>> handles= handlesDao.getById(cmppEndpointEntity.getId());
					/*for (AbstractBusinessHandler abstractBusinessHandler : handles) {
						BusinessHandlerInterface handle=new BusinessHandlerInterface();
						businessHandler.add(BusinessHandlerInterface)
					}*/
					cMPPClientEndpointEntity.setBusinessHandlerSet(handles);
				}
			}	
		
		return results;
	}

	@Override
	public List<CMPPClientEndpointEntity> loadClientEndpointEntity() {
		List<CMPPClientEndpointEntity> results = new ArrayList<CMPPClientEndpointEntity>();
		List<CMPPEndpointEntity> cMPPEndpointEntitys= new ArrayList<CMPPEndpointEntity>();
		for (CMPPEndpointEntity cmppEndpointEntity : cMPPEndpointEntitys) {
			CMPPClientEndpointEntity cMPPClientEndpointEntity = new CMPPClientEndpointEntity();
			cMPPClientEndpointEntity.setId(cmppEndpointEntity.getId());
			cMPPClientEndpointEntity.setDesc(cmppEndpointEntity.getDesc());
			cMPPClientEndpointEntity.setValid(cmppEndpointEntity.isValid());
			cMPPClientEndpointEntity.setGroupName(cmppEndpointEntity.getGroupName());
			cMPPClientEndpointEntity.setHost(cmppEndpointEntity.getHost());
			cMPPClientEndpointEntity.setPort(cmppEndpointEntity.getPort());
			cMPPClientEndpointEntity.setUserName(cmppEndpointEntity.getUserName());
			cMPPClientEndpointEntity.setPassword(cmppEndpointEntity.getPassword());
			cMPPClientEndpointEntity.setVersion(cmppEndpointEntity.getVersion());
			cMPPClientEndpointEntity.setIdleTimeSec(cmppEndpointEntity.getIdleTimeSec());
			cMPPClientEndpointEntity.setLiftTime(cmppEndpointEntity.getLiftTime());
			cMPPClientEndpointEntity.setMaxRetryCnt(cmppEndpointEntity.getMaxRetryCnt());
			cMPPClientEndpointEntity.setRetryWaitTimeSec(cmppEndpointEntity.getRetryWaitTimeSec());
			cMPPClientEndpointEntity.setMaxChannels(cmppEndpointEntity.getMaxChannels());
			cMPPClientEndpointEntity.setWindows(cmppEndpointEntity.getWindows());
			cMPPClientEndpointEntity.setChartset(cmppEndpointEntity.getChartset());
			//读取handle表
			List<Class<BusinessHandlerInterface>> handles= handlesDao.getById(cmppEndpointEntity.getId());
			/*for (AbstractBusinessHandler abstractBusinessHandler : handles) {
				BusinessHandlerInterface handle=new BusinessHandlerInterface();
				businessHandler.add(BusinessHandlerInterface)
			}*/
			cMPPClientEndpointEntity.setBusinessHandlerSet(handles);
		}
		return results;
	}


	

}
