package com.zx.sms.connect.manager.cmpp;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;
import com.zx.sms.mbean.ConnState;
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
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("901782");
		child.setPassword("ICP");
		child.setValid(true);
		
		child.setWindows((short)1);
		child.setVersion((short)0x30);
		child.setMaxChannels((short)20);
		child.setRetryWaitTimeSec((short)100);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(true);
	
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SessionConnectedHandler());
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
		client.setWindows((short)1);
		client.setVersion((short)0x30);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(true);

		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new MessageReceiveHandler());
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);
		
		manager.openAll();
		//LockSupport.park();
		 MBeanServer mserver = ManagementFactory.getPlatformMBeanServer();  

        ObjectName stat = new ObjectName("com.zx.sms:name=ConnState");
        mserver.registerMBean(new ConnState(), stat);
        System.out.println("start.....");
		Thread.sleep(300000);
		CMPPEndpointManager.INS.close();
	}
}
