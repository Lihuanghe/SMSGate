package com.zx.sms.connect.manager.smpp;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;


public class SMPPServerChildEndpointEntity extends SMPPEndpointEntity implements ServerEndpoint{

	@Override
	public SMPPServerChildEndpointConnector buildConnector() {
		return new SMPPServerChildEndpointConnector(this);
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
