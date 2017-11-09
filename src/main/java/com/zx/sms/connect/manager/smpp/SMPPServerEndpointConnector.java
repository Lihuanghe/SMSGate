package com.zx.sms.connect.manager.smpp;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import com.zx.sms.codec.smpp.SMPPMessageCodec;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractServerEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.smpp.SMPPSessionLoginManager;

public class SMPPServerEndpointConnector extends AbstractServerEndpointConnector{

	public SMPPServerEndpointConnector(EndpointEntity e) {
		super(e);
	}

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		EndpointEntity entity = getEndpointEntity();
		pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
		pipeline.addLast("SmppServerIdleStateHandler", GlobalConstance.smppidleHandler);
		pipeline.addLast(SMPPCodecChannelInitializer.pipeName(), new SMPPCodecChannelInitializer());
		pipeline.addLast("sessionLoginManager", new SMPPSessionLoginManager(getEndpointEntity()));
		
	}

	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, Map storeMap, Map preSend) {
		// TODO Auto-generated method stub
		return null;
	}
}
