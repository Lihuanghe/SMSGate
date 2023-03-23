package com.zx.sms.connect.manager;

import com.zx.sms.connect.manager.EndpointEntity.ChannelType;

public interface ServerServerEndpoint extends ServerEndpoint {
	 void addchild(EndpointEntity entity);
	 void removechild(EndpointEntity entity);
	 EndpointEntity getChild(String userName);
	 EndpointEntity getChild(String userName,ChannelType chType);
}
