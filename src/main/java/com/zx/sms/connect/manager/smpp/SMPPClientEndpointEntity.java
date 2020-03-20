package com.zx.sms.connect.manager.smpp;

import com.zx.sms.connect.manager.ClientEndpoint;


public class SMPPClientEndpointEntity extends SMPPEndpointEntity implements ClientEndpoint{

	@Override
	protected SMPPClientEndpointConnector buildConnector() {
		
		return new SMPPClientEndpointConnector(this);
	}

}
