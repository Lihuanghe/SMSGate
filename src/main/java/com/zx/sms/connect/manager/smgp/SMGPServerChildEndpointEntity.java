package com.zx.sms.connect.manager.smgp;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;


public class SMGPServerChildEndpointEntity extends SMGPEndpointEntity implements ServerEndpoint{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6946326952395639937L;

	@Override
	public SMGPServerChildEndpointConnector buildConnector() {
		return new SMGPServerChildEndpointConnector(this);
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
