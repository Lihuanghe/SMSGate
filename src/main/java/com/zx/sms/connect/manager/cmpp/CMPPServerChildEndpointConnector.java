package com.zx.sms.connect.manager.cmpp;

import io.netty.channel.ChannelInitializer;

import com.zx.sms.common.NotSupportedException;
import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;

public class CMPPServerChildEndpointConnector extends AbstractEndpointConnector {

	public CMPPServerChildEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
	}

	@Override
	public void open() throws Exception {
		//TODO 子端口打开，说明有客户连接上来
	}

}
