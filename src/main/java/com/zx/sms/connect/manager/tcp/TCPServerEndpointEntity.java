package com.zx.sms.connect.manager.tcp;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;

public class TCPServerEndpointEntity extends EndpointEntity implements ServerEndpoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961093716034771309L;

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
	protected <T extends EndpointConnector<EndpointEntity>> T buildConnector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointEntity getChild(String userName, ChannelType chType) {
		return null;
	}
}
