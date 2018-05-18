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
	public void addchild(EndpointEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removechild(EndpointEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EndpointEntity getChild(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends EndpointConnector<EndpointEntity>> T buildConnector() {
		// TODO Auto-generated method stub
		return null;
	}
}
