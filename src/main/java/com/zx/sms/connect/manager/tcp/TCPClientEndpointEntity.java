package com.zx.sms.connect.manager.tcp;

import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;

public class TCPClientEndpointEntity extends EndpointEntity implements ClientEndpoint {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6580678038168372304L;

	public TCPClientEndpointEntity(String host, int port) {
		setHost(host);
		setPort(port);
	}

	@Override
	protected TCPClientEndpointConnector buildConnector() {

		return new TCPClientEndpointConnector(this);
	}

}
