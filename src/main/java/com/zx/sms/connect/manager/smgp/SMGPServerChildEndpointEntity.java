package com.zx.sms.connect.manager.smgp;

import com.zx.sms.connect.manager.ServerEndpoint;


public class SMGPServerChildEndpointEntity extends SMGPEndpointEntity implements ServerEndpoint{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6946326952395639937L;

	@SuppressWarnings("unchecked")
	@Override
	protected SMGPServerChildEndpointConnector buildConnector() {
		return new SMGPServerChildEndpointConnector(this);
	}
}
