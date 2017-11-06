package com.zx.sms.connect.manager.cmpp;

import com.zx.sms.connect.manager.ServerEndpoint;


public class CMPPServerChildEndpointEntity extends CMPPEndpointEntity implements ServerEndpoint{

	@Override
	public CMPPServerChildEndpointConnector buildConnector() {
		
		return new CMPPServerChildEndpointConnector(this);
	}

}
