package com.zx.sms.connect.manager;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

public class ProxyProtocolCMPPClientEndpointEntity extends CMPPClientEndpointEntity {
	
	@Override
	protected ProxyProtocolEndpointConnector buildConnector() {
		return new ProxyProtocolEndpointConnector(this);
	}

}
