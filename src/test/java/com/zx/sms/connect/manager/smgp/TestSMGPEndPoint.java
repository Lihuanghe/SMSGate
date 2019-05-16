package com.zx.sms.connect.manager.smgp;

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
/**
 *经测试，35个连接，每个连接每200/s条消息
 *lenovoX250能承担7000/s消息编码解析无压力。
 *10000/s的消息服务不稳定，开个网页，或者打开其它程序导致系统抖动，会有大量消息延迟 (超过500ms)
 *
 *低负载时消息编码解码可控制在10ms以内。
 *
 */


public class TestSMGPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestSMGPEndPoint.class);

	@Test
	public void testSMGPEndpoint() throws Exception {
	
		final EndpointManager manager = EndpointManager.INS;

		SMGPServerEndpointEntity server = new SMGPServerEndpointEntity();
		server.setId("smgpserver");
		server.setHost("127.0.0.1");
		server.setPort(9890);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		SMGPServerChildEndpointEntity child = new SMGPServerChildEndpointEntity();
		child.setId("smgpchild");
		child.setClientID("333");
		child.setPassword("0555");

		child.setValid(true);
		child.setChannelType(ChannelType.DUPLEX);
		child.setClientVersion((byte)0x13);
		child.setMaxChannels((short)3);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(false);
		child.setIdleTimeSec((short)15);
		child.setSupportLongmsg(SupportLongMessage.SEND);  //接收长短信时不自动合并
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SMGPMessageReceiveHandler());   
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		manager.addEndpointEntity(server);
		
		SMGPClientEndpointEntity client = new SMGPClientEndpointEntity();
		client.setId("smgpclient");
		client.setHost("127.0.0.1");
		client.setPort(9890);
		client.setClientID("333");
		client.setPassword("0555");
		client.setChannelType(ChannelType.DUPLEX);

		client.setMaxChannels((short)30);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
		client.setClientVersion((byte)0x13);
//		client.setWriteLimit(200);
//		client.setReadLimit(200);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new SMGPSessionConnectedHandler(10000)); 
		client.setBusinessHandlerSet(clienthandlers);
		
		manager.addEndpointEntity(client);
		
		manager.openAll();
		
		Thread.sleep(1000);
		for(int i=0;i<child.getMaxChannels();i++)
			manager.openEndpoint(client);
		 System.out.println("start.....");
		LockSupport.park();
       
		EndpointManager.INS.close();
	}
	
}
