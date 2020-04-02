package com.zx.sms.connect.manager.sgip;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;



public class SgipServerChildEndpointEntity extends SgipEndpointEntity implements ServerEndpoint{
	private static final long serialVersionUID = 3363361913630997213L;
	@SuppressWarnings("unchecked")
	@Override
	protected SgipServerChildEndpointConnector buildConnector() {
		return new SgipServerChildEndpointConnector(this);
	}

	@Override
	public void addchild(EndpointEntity entity) {
	}

	@Override
	public void removechild(EndpointEntity entity) {
		
	}

	@Override
	public EndpointEntity getChild(String userName) {
		return null;
	}
	public EndpointEntity getChild(String userName,ChannelType chType) {
		return null;
	}
}
