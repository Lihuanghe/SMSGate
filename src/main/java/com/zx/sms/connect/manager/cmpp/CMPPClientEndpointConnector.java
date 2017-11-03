package com.zx.sms.connect.manager.cmpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.connect.manager.AbstractClientEndpointConnector;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.handler.cmpp.CMPPChannelTrafficShapingHandler;
import com.zx.sms.handler.cmpp.CMPPMessageLogHandler;
import com.zx.sms.handler.cmpp.ReWriteSubmitMsgSrcHandler;
import com.zx.sms.session.cmpp.SessionLoginManager;
import com.zx.sms.session.cmpp.SessionStateManager;

/**
 *@author Lihuanghe(18852780@qq.com)
 */

public class CMPPClientEndpointConnector extends AbstractClientEndpointConnector {
	private static final Logger logger = LoggerFactory.getLogger(CMPPClientEndpointConnector.class);
	

	
	public CMPPClientEndpointConnector(CMPPClientEndpointEntity e)
	{
		super(e);
		
	}



	@Override
	protected void doAddChannel(Channel ch,int cnt) {
		EndpointEntity endpoint = getEndpointEntity();
		// 如果是CMPP端口
		if (endpoint instanceof CMPPEndpointEntity) {
			CMPPEndpointEntity cmppentity = (CMPPEndpointEntity)endpoint;
			Map<Long, Message> storedMap = null;
			if( cmppentity.isReSendFailMsg()){
				// 如果上次发送失败的消息要重发一次，则要创建持久化Map用于存储发送的message
				 storedMap = BDBStoredMapFactoryImpl.INS.buildMap(endpoint.getId(), "Session_" +endpoint.getId());
			}else{
				 storedMap = new HashMap<Long, Message>();
			}
			
			Map<Long, Message> preSendMap = new HashMap<Long, Message>();

			logger.debug("Channel added To Endpoint {} .totalCnt:{} ,remoteAddress: {}", endpoint, cnt, ch.remoteAddress());
			if (cnt == 1 && cmppentity.isReSendFailMsg()) {
				// 如果是第一个连接。要把上次发送失败的消息取出，再次发送一次

				if (storedMap != null && storedMap.size() > 0) {
					try {
						for (Map.Entry<Long, Message> entry : storedMap.entrySet()) {
							Message msg = entry.getValue();
							//不能重发Term消息。
							//如果上次连接关闭时Term消息未收到响应，重发会导致本次连接关闭
							if(msg.getHeader().getCommandId()!= CmppPacketType.CMPPTERMINATEREQUEST.getCommandId()&&
									msg.getHeader().getCommandId()!= CmppPacketType.CMPPTERMINATERESPONSE.getCommandId())
							{
								preSendMap.put(entry.getKey(), entry.getValue());
							}else{
								logger.warn("last CMPPTERMINATE msg may not success.");
							}
							
						}
					} catch (Exception e) {
						logger.warn("get storedMessage err ", e);
					}finally{
						//删除所有积压的消息
						storedMap.clear();
					}
				}
			}
			
			// 增加流量整形 ，每个连接每秒发送，接收消息数不超过配置的值
			ch.pipeline().addAfter(CMPPCodecChannelInitializer.codecName, "CMPPChannelTrafficAfter",
					new CMPPChannelTrafficShapingHandler(cmppentity.getWriteLimit(), cmppentity.getReadLimit(), 250));
			
			ch.pipeline().addAfter(CMPPCodecChannelInitializer.codecName, "sessionStateManager", new SessionStateManager(cmppentity, storedMap, preSendMap));
			
		}
		
	}



	@Override
	protected void doBindHandler(ChannelPipeline pipe, EndpointEntity cmppentity) {
		CMPPEndpointEntity entity = (CMPPEndpointEntity)cmppentity;

		pipe.addFirst("socketLog", new LoggingHandler(String.format(GlobalConstance.loggerNamePrefix, entity.getId()), LogLevel.TRACE));
		pipe.addLast("msgLog", new CMPPMessageLogHandler(entity));

		if (entity instanceof ClientEndpoint) {
			pipe.addLast("reWriteSubmitMsgSrcHandler", new ReWriteSubmitMsgSrcHandler(entity));
		}

		pipe.addLast("CmppActiveTestRequestMessageHandler", GlobalConstance.activeTestHandler);
		pipe.addLast("CmppActiveTestResponseMessageHandler", GlobalConstance.activeTestRespHandler);
		pipe.addLast("CmppTerminateRequestMessageHandler", GlobalConstance.terminateHandler);
		pipe.addLast("CmppTerminateResponseMessageHandler", GlobalConstance.terminateRespHandler);
		
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

}
