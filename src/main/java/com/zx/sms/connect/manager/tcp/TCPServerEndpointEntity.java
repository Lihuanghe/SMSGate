package com.zx.sms.connect.manager.tcp;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;

public class TCPServerEndpointEntity extends EndpointEntity implements ServerEndpoint {

	public TCPServerEndpointEntity(int port) {
		setPort(port);
	}

	public TCPServerEndpointEntity(String host, int port) {
		setHost(host);
		setPort(port);
	}

	@Override
	public  TCPServerEndpointConnector buildConnector() {
		return new TCPServerEndpointConnector(this);
	}
}
