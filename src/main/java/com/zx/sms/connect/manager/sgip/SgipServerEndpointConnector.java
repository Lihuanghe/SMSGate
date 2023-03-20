package com.zx.sms.connect.manager.sgip;

import java.util.concurrent.TimeUnit;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractServerEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.sgip.SgipSessionLoginManager;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

public class SgipServerEndpointConnector extends AbstractServerEndpointConnector{

	public SgipServerEndpointConnector(EndpointEntity e) {
		super(e);
	}


	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		EndpointEntity entity = getEndpointEntity();
		pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
		pipeline.addLast("SgipServerIdleStateHandler", GlobalConstance.sgipidleHandler);
		pipeline.addLast(SgipCodecChannelInitializer.pipeName(), new SgipCodecChannelInitializer());
		pipeline.addLast(GlobalConstance.sessionLoginManager, new SgipSessionLoginManager(getEndpointEntity()));
	}

}
