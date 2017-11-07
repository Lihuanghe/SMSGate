package com.zx.sms.connect.manager.smpp;


public class SMPPServerChildEndpointEntity extends SMPPEndpointEntity {

	@Override
	public SMPPServerChildEndpointConnector buildConnector() {
		return new SMPPServerChildEndpointConnector(this);
	}

}
