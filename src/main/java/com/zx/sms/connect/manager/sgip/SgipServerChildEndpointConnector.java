package com.zx.sms.connect.manager.sgip;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.sgip.ReWriteNodeIdHandler;
import com.zx.sms.handler.sgip.SgipDeliverLongMessageHandler;
import com.zx.sms.handler.sgip.SgipSubmitLongMessageHandler;
import com.zx.sms.handler.sgip.SgipUnbindRequestMessageHandler;
import com.zx.sms.handler.sgip.SgipUnbindResponseMessageHandler;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.sgip.SgipSessionStateManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

public class SgipServerChildEndpointConnector extends AbstractEndpointConnector{
	private static final Logger logger = LoggerFactory.getLogger(SgipServerChildEndpointConnector.class);
	public SgipServerChildEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
	}

	@Override
	public ChannelFuture open() throws Exception {
		return null;
	}

	@Override
	protected SslContext createSslCtx() {
		return null;
	}

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {

		pipe.addLast("reWriteNodeIdHandler", new ReWriteNodeIdHandler((SgipEndpointEntity)entity));
		//处理长短信
		pipe.addLast("SgipDeliverLongMessageHandler", new SgipDeliverLongMessageHandler(entity));
		pipe.addLast("SgipSubmitLongMessageHandler",  new SgipSubmitLongMessageHandler(entity));
		pipe.addLast("SgipUnbindResponseMessageHandler", new SgipUnbindResponseMessageHandler());
		pipe.addLast("SgipUnbindRequestMessageHandler", new SgipUnbindRequestMessageHandler());
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		
		
	}

	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		
	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		return new SgipSessionStateManager(entity, storeMap, preSend);
	}

}
