package com.zx.sms.connect.manager.cmpp;

import com.zx.sms.connect.manager.ServerEndpoint;


public class CMPPServerChildEndpointEntity extends CMPPEndpointEntity implements ServerEndpoint{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8696901786407841859L;

	@SuppressWarnings("unchecked")
	@Override
	protected CMPPServerChildEndpointConnector buildConnector() {
		
		return new CMPPServerChildEndpointConnector(this);
	}
}
