package com.zx.sms.connect.manager.smpp;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.SMPP2CMPPBusinessHandler;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
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


public class TestSMPPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestSMPPEndPoint.class);

	@Test
	public void testSMPPEndpoint() throws Exception {
	
		final EndpointManager manager = EndpointManager.INS;

		SMPPServerEndpointEntity server = new SMPPServerEndpointEntity();
		server.setId("smppserver");
		server.setHost("127.0.0.1");
		server.setPort(2776);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		SMPPServerChildEndpointEntity child = new SMPPServerChildEndpointEntity();
		child.setId("smppchild");
		child.setSystemId("901782");
		child.setPassword("ICP");

		child.setValid(true);
		child.setChannelType(ChannelType.DUPLEX);
		child.setMaxChannels((short)20);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(true);
		child.setIdleTimeSec((short)15);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SMPP2CMPPBusinessHandler());  //  将CMPP的对象转成SMPP对象，然后再经SMPP解码器处理
		serverhandlers.add( new MessageReceiveHandler());   // 复用CMPP的Handler
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		manager.addEndpointEntity(server);
		manager.openAll();
		
		SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
		client.setId("smppclient");
		client.setHost("127.0.0.1");
		client.setPort(2776);
		client.setSystemId("901782");
		client.setPassword("ICP");
		client.setChannelType(ChannelType.DUPLEX);

		client.setMaxChannels((short)12);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
//		client.setReSendFailMsg(true);
//		client.setWriteLimit(200);
//		client.setReadLimit(200);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new SMPP2CMPPBusinessHandler()); //  将CMPP的对象转成SMPP对象，然后再经SMPP解码器处理
		clienthandlers.add(new SessionConnectedHandler(600000)); //// 复用CMPP的Handler
		client.setBusinessHandlerSet(clienthandlers);
		
		manager.openEndpoint(client);
		
		//LockSupport.park();
		 MBeanServer mserver = ManagementFactory.getPlatformMBeanServer();  

        ObjectName stat = new ObjectName("com.zx.sms:name=ConnState");
        mserver.registerMBean(new ConnState(), stat);
        System.out.println("start.....");
        
		Thread.sleep(300000);
		EndpointManager.INS.close();
	}
}
