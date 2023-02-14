package com.zx.sms.connect.manager;

import com.zx.sms.connect.manager.EndpointEntity.ChannelType;

/**
 *@author Lihuanghe(18852780@qq.com)
 */
public interface ServerEndpoint {
	 void addchild(EndpointEntity entity);
	 void removechild(EndpointEntity entity);
	 EndpointEntity getChild(String userName);
	 EndpointEntity getChild(String userName,ChannelType chType);
}
