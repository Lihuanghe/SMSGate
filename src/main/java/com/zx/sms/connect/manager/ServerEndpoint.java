package com.zx.sms.connect.manager;

import com.zx.sms.connect.manager.EndpointEntity.ChannelType;

/**
 *@author Lihuanghe(18852780@qq.com)
 */
public interface ServerEndpoint {
	public void addchild(EndpointEntity entity);
	public void removechild(EndpointEntity entity);
	public EndpointEntity getChild(String userName);
	public EndpointEntity getChild(String userName,ChannelType chType);
}
