package com.zx.sms.connect.manager.sgip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.sgip.SgipReportRequestMessageHandler;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
/**
 *经测试，35个连接，每个连接每200/s条消息
 *lenovoX250能承担7000/s消息编码解析无压力。
 *10000/s的消息服务不稳定，开个网页，或者打开其它程序导致系统抖动，会有大量消息延迟 (超过500ms)
 *
 *低负载时消息编码解码可控制在10ms以内。
 *
 */


public class TestSgipEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestSgipEndPoint.class);

	@Test
	public void testsgipEndpoint() throws Exception {
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		final EndpointManager manager = EndpointManager.INS;

		SgipServerEndpointEntity server = new SgipServerEndpointEntity();
		server.setId("sgipserver");
		server.setHost("127.0.0.1");
		server.setPort(8001);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		SgipServerChildEndpointEntity child = new SgipServerChildEndpointEntity();
		child.setId("sgipchild");
		child.setLoginName("333");
		child.setLoginPassowrd("0555");
		child.setNodeId(3025000001L);
		child.setValid(true);
		child.setChannelType(ChannelType.DUPLEX);
		child.setMaxChannels((short)3);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(false);
		child.setIdleTimeSec((short)30);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		child.setSupportLongmsg(SupportLongMessage.BOTH);  
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SGIPMessageReceiveHandler());
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		manager.addEndpointEntity(server);
		SgipClientEndpointEntity client = new SgipClientEndpointEntity();
		client.setId("sgipclient");
		client.setHost("127.0.0.1");
		client.setPort(8001);
		client.setLoginName("333");
		client.setLoginPassowrd("0555");
		client.setChannelType(ChannelType.DUPLEX);
		client.setNodeId(3073100002L);
		client.setMaxChannels((short)10);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
		client.setIdleTimeSec((short)120);
//		client.setWriteLimit(200);
//		client.setReadLimit(200);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new SgipReportRequestMessageHandler());
		clienthandlers.add(new SGIPSessionConnectedHandler(1000000));   
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);
		manager.openAll();
		Thread.sleep(1000);
	
		System.out.println("start.....");
      
        LockSupport.park();

		EndpointManager.INS.close();
	}
}
