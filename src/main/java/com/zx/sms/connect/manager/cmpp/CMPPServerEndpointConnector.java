package com.zx.sms.connect.manager.cmpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractServerEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.cmpp.SessionLoginManager;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public class CMPPServerEndpointConnector extends AbstractServerEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(CMPPServerEndpointConnector.class);
	public CMPPServerEndpointConnector(EndpointEntity e) {
		super(e);
	}
	
	protected ChannelInitializer<?> initPipeLine() {
		return new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();

				if (getEndpointEntity().isUseSSL() && getSslCtx() != null ) {
					initSslCtx(ch, getEndpointEntity());
				}

				CMPPCodecChannelInitializer codec = null;
				if (getEndpointEntity() instanceof CMPPEndpointEntity) {
					pipeline.addLast(GlobalConstance.IdleCheckerHandlerName,
							new IdleStateHandler(0, 0, ((CMPPEndpointEntity) getEndpointEntity()).getIdleTimeSec(), TimeUnit.SECONDS));
					codec = new CMPPCodecChannelInitializer(((CMPPEndpointEntity) getEndpointEntity()).getVersion());

				} else {
					pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
					codec = new CMPPCodecChannelInitializer();
				}

				pipeline.addLast("CmppServerIdleStateHandler", GlobalConstance.idleHandler);
				pipeline.addLast(codec.pipeName(), codec);

				pipeline.addLast("sessionLoginManager", new SessionLoginManager(getEndpointEntity()));
			}
		};
	}

	@Override
	protected void doAddChannel(Channel ch, int cnt) {

	}

	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity entity) {

	}

}
