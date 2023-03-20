package com.zx.sms.connect.manager.smgp;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.smgp.SMGPActiveTestMessageHandler;
import com.zx.sms.handler.smgp.SMGPActiveTestRespMessageHandler;
import com.zx.sms.handler.smgp.SMGPDeliverLongMessageHandler;
import com.zx.sms.handler.smgp.SMGPExitMessageHandler;
import com.zx.sms.handler.smgp.SMGPExitRespMessageHandler;
import com.zx.sms.handler.smgp.SMGPSubmitLongMessageHandler;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.smgp.SMGPSessionStateManager;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;

public class SMGPServerChildEndpointConnector extends AbstractEndpointConnector{
	private static final Logger logger = LoggerFactory.getLogger(SMGPServerChildEndpointConnector.class);
	public SMGPServerChildEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
	}

	@Override
	public ChannelFuture open() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {
		
		pipe.addLast("SMGPDeliverLongMessageHandler", new SMGPDeliverLongMessageHandler(entity));
		pipe.addLast("SMGPSubmitLongMessageHandler",  new SMGPSubmitLongMessageHandler(entity));
		pipe.addLast("SMGPActiveTestMessageHandler",new SMGPActiveTestMessageHandler());
		pipe.addLast("SMGPActiveTestRespMessageHandler",new SMGPActiveTestRespMessageHandler());
		pipe.addLast("SMGPExitRespMessageHandler", new SMGPExitRespMessageHandler());
		pipe.addLast("SMGPExitMessageHandler", new SMGPExitMessageHandler());
		
	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		return new SMGPSessionStateManager(entity, storeMap, preSend);
	}

}
