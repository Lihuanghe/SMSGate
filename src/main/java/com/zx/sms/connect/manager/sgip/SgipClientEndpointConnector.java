package com.zx.sms.connect.manager.sgip;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractClientEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.sgip.ReWriteNodeIdHandler;
import com.zx.sms.handler.sgip.SgipDeliverLongMessageHandler;
import com.zx.sms.handler.sgip.SgipSubmitLongMessageHandler;
import com.zx.sms.handler.sgip.SgipUnbindRequestMessageHandler;
import com.zx.sms.handler.sgip.SgipUnbindResponseMessageHandler;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.sgip.SgipSessionLoginManager;
import com.zx.sms.session.sgip.SgipSessionStateManager;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class SgipClientEndpointConnector extends AbstractClientEndpointConnector {


	private static final Logger logger = LoggerFactory.getLogger(SgipClientEndpointConnector.class);
	
	public SgipClientEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
	}
	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		return new SgipSessionStateManager(entity, storeMap, preSend);
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
		EndpointEntity entity = getEndpointEntity();
		pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
		pipeline.addLast("SgipServerIdleStateHandler", GlobalConstance.sgipidleHandler);
		pipeline.addLast(SgipCodecChannelInitializer.pipeName(), new SgipCodecChannelInitializer());
		pipeline.addLast("sessionLoginManager", new SgipSessionLoginManager(getEndpointEntity()));
	}

}
