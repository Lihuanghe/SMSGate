package com.zx.sms.connect.manager.cmpp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


public class ClientTestCMPPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(ClientTestCMPPEndPoint.class);

	@Test
	public void testCMPPEndpoint() throws Exception {
	
		final EndpointManager manager = EndpointManager.INS;
	
	
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("GSDT01");
		client.setHost("127.0.0.1");
		client.setPort(20003);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("GSDT01");
		client.setPassword("1234567");

		client.setSpCode("1069039128");
		client.setMaxChannels((short)10);
		client.setVersion((short)0x20);
		client.setRetryWaitTimeSec((short)30);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);
//		client.setWriteLimit(500);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new CMPPSessionConnectedHandler(10000));
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);
		
		CMPPClientEndpointEntity client1 = new CMPPClientEndpointEntity();
		client1.setId("GSDT02");
		client1.setHost("127.0.0.1");
		client1.setPort(20003);
		client1.setChartset(Charset.forName("utf-8"));
		client1.setGroupName("test");
		client1.setSpCode("1069039129");
		client1.setUserName("GSDT02");
		client1.setPassword("1qaz2wsx");
//		client1.setProxy("http://username:password@127.0.0.1:1080");

		client1.setMaxChannels((short)1);
		client1.setVersion((short)0x20);
		client1.setRetryWaitTimeSec((short)30);
		client1.setUseSSL(false);
		client1.setReSendFailMsg(true);
//		client.setWriteLimit(10);
		List<BusinessHandlerInterface> clienthandlers1 = new ArrayList<BusinessHandlerInterface>();
		clienthandlers1.add( new CMPPSessionConnectedHandler(0));
		client1.setBusinessHandlerSet(clienthandlers1);
//		manager.addEndpointEntity(client1);
		
		manager.openAll();
		//LockSupport.park();
		Thread.sleep(1000);
		manager.openEndpoint(client);

		//manager.openEndpoint(client);Thread.sleep(1000);
//		manager.startConnectionCheckTask();
		
//		while(true){
//			
//			try{
//				Thread.sleep(20000);
//			}catch(Exception e){
//				break;
//			}
//			EndpointConnector conn = manager.getEndpointConnector(client);
//			conn.fetch().close();
//		}
		
        System.out.println("start.....");
        
        LockSupport.park();
		EndpointManager.INS.close();
	}
}
