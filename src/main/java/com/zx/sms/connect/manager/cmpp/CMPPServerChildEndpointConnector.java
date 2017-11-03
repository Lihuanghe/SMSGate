package com.zx.sms.connect.manager.cmpp;

import io.netty.channel.ChannelFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMPPServerChildEndpointConnector extends CMPPServerEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(CMPPServerChildEndpointConnector.class);
	
	public CMPPServerChildEndpointConnector(CMPPEndpointEntity endpoint) {
		super(endpoint);
	}

	@Override
	public ChannelFuture open() throws Exception {
		//TODO 子端口打开，说明有客户连接上来
		
		return null;
	}


}
