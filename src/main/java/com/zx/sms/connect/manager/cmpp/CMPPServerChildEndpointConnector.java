package com.zx.sms.connect.manager.cmpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;

public class CMPPServerChildEndpointConnector extends AbstractEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(CMPPServerChildEndpointConnector.class);
	public CMPPServerChildEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
	}

	@Override
	public void open() throws Exception {
		//TODO 子端口打开，说明有客户连接上来
	}

	@Override
	protected SslContext createSslCtx() {
			return null;
	}

	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		
	}

}
