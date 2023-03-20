package com.zx.sms.connect.manager;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

public class ProxyProtocolCMPPClientEndpointEntity extends CMPPClientEndpointEntity {
	
	private static final long serialVersionUID = -4436330300737932229L;
	private String srcAddress ;
	
	
	public ProxyProtocolCMPPClientEndpointEntity(String srcAddress) {
		super();
		this.srcAddress = srcAddress;
	}


	@Override
	protected ProxyProtocolEndpointConnector buildConnector() {
		return new ProxyProtocolEndpointConnector(this,srcAddress);
	}

}
