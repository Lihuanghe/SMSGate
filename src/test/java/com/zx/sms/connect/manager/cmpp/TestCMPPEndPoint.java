package com.zx.sms.connect.manager.cmpp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;
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
	
		final CMPPEndpointManager manager = CMPPEndpointManager.INS;

		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7891);
		server.setValid(true);
		
		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("901782");
		child.setPassword("ICP");
		child.setValid(true);
		child.setWindows((short)160);
		child.setVersion((short)48);
		child.setMaxChannels((short)20);
		child.setRetryWaitTimeSec((short)100);
		child.setMaxRetryCnt((short)3);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SessionConnectedHandler());
		serverhandlers.add(new MessageReceiveHandler());
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		
		manager.addEndpointEntity(server);
	
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		client.setHost("127.0.0.1");
		client.setPort(7891);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901782");
		client.setPassword("ICP");
		client.setWindows((short)16);
		client.setVersion((short)48);
		client.setRetryWaitTimeSec((short)100);
		
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new MessageReceiveHandler());
		clienthandlers.add(new SessionConnectedHandler());
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);

		
		CMPPClientEndpointEntity clientErr = new CMPPClientEndpointEntity();
		clientErr.setId("clienterr");
		clientErr.setHost("127.0.0.1");
		clientErr.setPort(7891);
		clientErr.setChartset(Charset.forName("utf-8"));
		clientErr.setGroupName("test");
		clientErr.setUserName("123456");
		clientErr.setPassword("1234456");
		clientErr.setWindows((short)16);
		clientErr.setVersion((short)48);
		clientErr.setValid(false);
		List<BusinessHandlerInterface> clientclientErrhandlers = new ArrayList<BusinessHandlerInterface>();
		clientclientErrhandlers.add(new MessageReceiveHandler());
		clientErr.setBusinessHandlerSet(clientclientErrhandlers);
	//	manager.addEndpointEntity(clientErr);
		
		manager.openAll();
		LockSupport.park();
//		Thread.sleep(300000);
		CMPPEndpointManager.INS.close();
	}
}
