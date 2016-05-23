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
import com.zx.sms.handler.api.gate.RecvSendDriverHandler;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;
/**
 *为业务平台编写测试驱动，实现发送多少条，短信端口，短信渠道，短信内容配置化
 *实现速率配置化
 *
 */


public class TestCMPPEndPointDemo {
	private static final Logger logger = LoggerFactory.getLogger(TestCMPPEndPointDemo.class);

	@Test
	public void testCMPPEndpoint() throws Exception {
	
		final CMPPEndpointManager manager = CMPPEndpointManager.INS;

		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7918);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("10085-s1");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("HENAN");
		child.setUserName("901782");
		child.setPassword("ICP");
		child.setValid(true);

		
		child.setWindows((short)16);
		child.setVersion((short)0x30);
		child.setMaxChannels((short)20);
		child.setRetryWaitTimeSec((short)100);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(true);

		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new RecvSendDriverHandler());
		serverhandlers.add(new MessageReceiveHandler());
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);

		
		manager.addEndpointEntity(server);
		
		manager.openAll();
		LockSupport.park();
		//Thread.sleep(300000);
		CMPPEndpointManager.INS.close();
	}
}
