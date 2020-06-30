package com.zx.sms.connect.manager.cmpp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.channel.ChannelHandlerContext;
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

public class TestCMPPDBEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestCMPPDBEndPoint.class);

	@Test
	public void testDBCMPPEndpoint() throws Exception {
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		final EndpointManager manager = EndpointManager.INS;

		DBCMPPServerEndpointEntity server = new DBCMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7891);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		manager.openEndpoint(server);
	
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		client.setLocalhost("127.0.0.1");
//		client.setLocalport(65535);
		client.setHost("127.0.0.1");
		client.setPort(7891);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901782");
		client.setPassword("ICP");


		client.setMaxChannels((short)2);
		client.setVersion((short)0x20);
		client.setRetryWaitTimeSec((short)10);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);

		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new CMPPMessageReceiveHandler());
		clienthandlers.add(new AbstractBusinessHandler() {

		    @Override
		    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		    	CMPPResponseSenderHandler handler = new CMPPResponseSenderHandler();
		    	handler.setEndpointEntity(getEndpointEntity());
		    	ctx.pipeline().addBefore("sessionStateManager", handler.name(), handler);
		    	ctx.pipeline().remove(this);
		    }
			
			@Override
			public String name() {
				return "AddCMPPResponseSenderHandler";
			}
			
		});
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);		
        System.out.println("start.....");
        manager.startConnectionCheckTask();
//		Thread.sleep(300000);
        LockSupport.park();
		EndpointManager.INS.close();
	}
	class DBCMPPServerEndpointEntity extends CMPPServerEndpointEntity{
		
		public EndpointEntity getChild(String userName)
		{
			CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
			child.setId("child");
			child.setChartset(Charset.forName("utf-8"));
			child.setGroupName("test");
			child.setUserName("901782");
			child.setPassword("ICP");

			child.setValid(true);
			child.setVersion((short)0x20);

			child.setMaxChannels((short)20);
			child.setRetryWaitTimeSec((short)30);
			child.setMaxRetryCnt((short)3);
//			child.setReSendFailMsg(true);
//			child.setWriteLimit(200);
//			child.setReadLimit(200);
			List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
			serverhandlers.add(new CMPPSessionConnectedHandler(100000));
			child.setBusinessHandlerSet(serverhandlers);
			return child;
		}
	}
}
