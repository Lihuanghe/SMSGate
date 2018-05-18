package com.zx.sms.connect.manager.sgip;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;


public class SgipServerChildEndpointEntity extends SgipEndpointEntity implements ServerEndpoint{

	@Override
	public SgipServerChildEndpointConnector buildConnector() {
		return new SgipServerChildEndpointConnector(this);
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

}
