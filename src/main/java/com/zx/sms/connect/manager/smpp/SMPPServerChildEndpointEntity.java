package com.zx.sms.connect.manager.smpp;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;



public class SMPPServerChildEndpointEntity extends SMPPEndpointEntity implements ServerEndpoint{
	private static final long serialVersionUID = -5780773960343990868L;
	@SuppressWarnings("unchecked")
	@Override
	protected SMPPServerChildEndpointConnector buildConnector() {
		return new SMPPServerChildEndpointConnector(this);
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
	public EndpointEntity getChild(String userName,ChannelType chType)
	{
		return null;
	}

}
