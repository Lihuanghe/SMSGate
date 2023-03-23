package com.zx.sms.connect.manager.sgip;

import com.zx.sms.connect.manager.ServerEndpoint;



public class SgipServerChildEndpointEntity extends SgipEndpointEntity implements ServerEndpoint{
	private static final long serialVersionUID = 3363361913630997213L;
	@SuppressWarnings("unchecked")
	@Override
	protected SgipServerChildEndpointConnector buildConnector() {
		return new SgipServerChildEndpointConnector(this);
	}
}
