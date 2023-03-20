package com.zx.sms.connect.manager;

import java.util.concurrent.TimeUnit;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.handler.SmsProtocolCheckHandler;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 *通用的服务端，允许一个tcp服务端口支持多种短信协议 
 */
public class MixedServerEndpointConnector extends AbstractServerEndpointConnector {

	public MixedServerEndpointConnector(EndpointEntity e) {
		super(e);
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		EndpointEntity entity = getEndpointEntity();
		pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
		pipeline.addLast(GlobalConstance.MixedServerIdleStateHandler, GlobalConstance.sgipidleHandler);
		pipeline.addLast(GlobalConstance.PreLengthFieldBasedFrameDecoder,new LengthFieldBasedFrameDecoder(4 * 1024 , 0, 4, -4, 0, true));
		pipeline.addLast(new SmsProtocolCheckHandler(entity));
	}
}
