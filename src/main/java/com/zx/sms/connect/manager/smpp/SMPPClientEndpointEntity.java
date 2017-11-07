package com.zx.sms.connect.manager.smpp;


public class SMPPClientEndpointEntity extends SMPPEndpointEntity {

	@Override
	public SMPPClientEndpointConnector buildConnector() {
		
		return new SMPPClientEndpointConnector(this);
	}

}
