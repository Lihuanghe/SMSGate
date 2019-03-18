package com.zx.sms.connect.manager.cmpp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.handler.api.BusinessHandlerInterface;

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

public class TestCMPPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestCMPPEndPoint.class);

	@Test
	public void testCMPPEndpoint() throws Exception {
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		final EndpointManager manager = EndpointManager.INS;

		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7890);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);

		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("901783");
		child.setPassword("ICP001");

		child.setValid(true);
		child.setVersion((short)0x30);

		child.setMaxChannels((short)4);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(true);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new CMPPMessageReceiveHandler());
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		manager.addEndpointEntity(server);
	
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		client.setHost("127.0.0.1");
//		client.setLocalhost("127.0.0.1");
//		client.setLocalport(65521);
		client.setPort(7890);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901783");
		client.setPassword("ICP001");

		client.setMaxChannels((short)2);
		client.setVersion((short)0x30);
		client.setRetryWaitTimeSec((short)30);
		client.setUseSSL(false);
//		client.setWriteLimit(100);
		client.setReSendFailMsg(true);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new CMPPSessionConnectedHandler(10000));
		client.setBusinessHandlerSet(clienthandlers);
		
		manager.addEndpointEntity(client);
		
		manager.openEndpoint(server);
		
		Thread.sleep(1000);
		for(int i=0;i<=child.getMaxChannels()+1;i++)
			manager.openEndpoint(client);

        System.out.println("start.....");
        
//		Thread.sleep(300000);
        LockSupport.park();
		EndpointManager.INS.close();
	}
}
