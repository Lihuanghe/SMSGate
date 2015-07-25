package com.zx.sms.service;

import java.util.List;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;


public interface LoadConfigFromDBService {
	
	
	List<CMPPServerEndpointEntity> loadServerEndpointEntity();//服务端加载
	
	List<CMPPClientEndpointEntity> loadClientEndpointEntity();//客服端加载
}
