package com.zx.sms.connect.manager.smpp;

import com.zx.sms.connect.manager.ServerEndpoint;



public class SMPPServerChildEndpointEntity extends SMPPEndpointEntity implements ServerEndpoint{
	private static final long serialVersionUID = -5780773960343990868L;
	@SuppressWarnings("unchecked")
	@Override
	protected SMPPServerChildEndpointConnector buildConnector() {
		return new SMPPServerChildEndpointConnector(this);
	}

}
