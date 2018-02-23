package com.zx.sms.connect.manager.sgip;

import com.zx.sms.connect.manager.ClientEndpoint;


public class SgipClientEndpointEntity extends SgipEndpointEntity implements ClientEndpoint{

	@Override
	public SgipClientEndpointConnector buildConnector() {
		
		return new SgipClientEndpointConnector(this);
	}

}
