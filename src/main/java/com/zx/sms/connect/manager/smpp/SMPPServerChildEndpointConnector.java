package com.zx.sms.connect.manager.smpp;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.smpp.AddZeroByteHandler;
import com.zx.sms.handler.smpp.EnquireLinkMessageHandler;
import com.zx.sms.handler.smpp.EnquireLinkRespMessageHandler;
import com.zx.sms.handler.smpp.SMPPLongMessageHandler;
import com.zx.sms.handler.smpp.UnbindMessageHandler;
import com.zx.sms.handler.smpp.UnbindRespMessageHandler;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.smpp.SMPPSessionStateManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

public class SMPPServerChildEndpointConnector extends AbstractEndpointConnector{
	private static final Logger logger = LoggerFactory.getLogger(SMPPServerChildEndpointConnector.class);
	public SMPPServerChildEndpointConnector(EndpointEntity endpoint) {
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
		
		
		pipe.addAfter(GlobalConstance.codecName,"AddZeroByteHandler",new AddZeroByteHandler(entity));
		pipe.addLast("SMPPLongMessageHandler", new SMPPLongMessageHandler(entity));
		pipe.addLast("EnquireLinkMessageHandler",new EnquireLinkMessageHandler());
		pipe.addLast("EnquireLinkRespMessageHandler",new EnquireLinkRespMessageHandler());
		pipe.addLast("UnbindRespMessageHandler", new UnbindRespMessageHandler());
		pipe.addLast("UnbindMessageHandler", new UnbindMessageHandler());
		
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		
		
	}

	@Override
	protected void initSslCtx(Channel ch, EndpointEntity entity) {
		
	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		return new SMPPSessionStateManager(entity, storeMap, preSend);
	}

}
