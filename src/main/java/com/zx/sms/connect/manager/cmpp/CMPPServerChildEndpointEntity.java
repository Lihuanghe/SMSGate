package com.zx.sms.connect.manager.cmpp;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;


public class CMPPServerChildEndpointEntity extends CMPPEndpointEntity implements ServerEndpoint{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8696901786407841859L;

	@SuppressWarnings("unchecked")
	@Override
	protected CMPPServerChildEndpointConnector buildConnector() {
		
		return new CMPPServerChildEndpointConnector(this);
	}

	@Override
	public void addchild(EndpointEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removechild(EndpointEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EndpointEntity getChild(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointEntity getChild(String userName, ChannelType chType) {
		// TODO Auto-generated method stub
		return null;
	}
}
