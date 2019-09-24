package com.zx.sms.connect.manager.smgp;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractClientEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.smgp.SMGPActiveTestMessageHandler;
import com.zx.sms.handler.smgp.SMGPActiveTestRespMessageHandler;
import com.zx.sms.handler.smgp.SMGPDeliverLongMessageHandler;
import com.zx.sms.handler.smgp.SMGPExitMessageHandler;
import com.zx.sms.handler.smgp.SMGPExitRespMessageHandler;
import com.zx.sms.handler.smgp.SMGPSubmitLongMessageHandler;
import com.zx.sms.session.AbstractSessionStateManager;
import com.zx.sms.session.smgp.SMGPSessionLoginManager;
import com.zx.sms.session.smgp.SMGPSessionStateManager;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class SMGPClientEndpointConnector extends AbstractClientEndpointConnector {


	private static final Logger logger = LoggerFactory.getLogger(SMGPClientEndpointConnector.class);
	
	public SMGPClientEndpointConnector(EndpointEntity endpoint) {
		super(endpoint);
	}
	@Override
	protected AbstractSessionStateManager createSessionManager(EndpointEntity entity, ConcurrentMap storeMap, boolean preSend) {
		return new SMGPSessionStateManager(entity, storeMap, preSend);
	}

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {
		//处理长短信
		pipe.addLast( "SMGPDeliverLongMessageHandler", new SMGPDeliverLongMessageHandler(entity));
		pipe.addLast("SMGPSubmitLongMessageHandler",  new SMGPSubmitLongMessageHandler(entity));
		pipe.addLast("SMGPActiveTestMessageHandler",new SMGPActiveTestMessageHandler());
		pipe.addLast("SMGPActiveTestRespMessageHandler",new SMGPActiveTestRespMessageHandler());
		pipe.addLast("SMGPExitRespMessageHandler", new SMGPExitRespMessageHandler());
		pipe.addLast("SMGPExitMessageHandler", new SMGPExitMessageHandler());
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		EndpointEntity entity = getEndpointEntity();
		pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
		pipeline.addLast("SmgpServerIdleStateHandler", GlobalConstance.smgpidleHandler);
		pipeline.addLast(SMGPCodecChannelInitializer.pipeName(), new SMGPCodecChannelInitializer((int)((SMGPEndpointEntity)entity).getClientVersion()));
		pipeline.addLast("sessionLoginManager", new SMGPSessionLoginManager(getEndpointEntity()));
	}

}
