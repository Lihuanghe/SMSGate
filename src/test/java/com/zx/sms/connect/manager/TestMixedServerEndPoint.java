package com.zx.sms.connect.manager;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.SmppSplitType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPMessageReceiveHandler;
import com.zx.sms.connect.manager.cmpp.CMPPResponseSenderHandler;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPSessionConnectedHandler;
import com.zx.sms.connect.manager.sgip.SGIPMessageReceiveHandler;
import com.zx.sms.connect.manager.sgip.SGIPSessionConnectedHandler;
import com.zx.sms.connect.manager.sgip.SgipClientEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerChildEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPClientEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPMessageReceiveHandler;
import com.zx.sms.connect.manager.smgp.SMGPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPSessionConnectedHandler;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPMessageReceiveHandler;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPSessionConnectedHandler;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;
import com.zx.sms.handler.sgip.SgipReportRequestMessageHandler;

import io.netty.channel.ChannelHandlerContext;

/**
 *测试多种协议共用一个tcp端口的情况
 */

public class TestMixedServerEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestMixedServerEndPoint.class);
	int port = 36890;
	int count =  TestConstants.Count/4;
	int writeLimit = 2500;
	boolean useSSL = true;
	MixedServerEndpointEntity server = new MixedServerEndpointEntity();
	
	CMPPMessageReceiveHandler cmppreceiver = new CMPPMessageReceiveHandler();
	MessageReceiveHandler sgipreceiver = new SGIPMessageReceiveHandler();
	MessageReceiveHandler sgmpreceiver = new SMGPMessageReceiveHandler();
	MessageReceiveHandler smppreceiver = new SMPPMessageReceiveHandler();

	@Before
	public void prepareServer() {

		EndpointManager.INS.removeAll();
		
		server.setId("server");
		server.setHost("0.0.0.0");
		server.setPort(port);
		server.setValid(true);
		// 使用ssl加密数据流
		server.setUseSSL(useSSL);

		CMPPServerChildEndpointEntity cmppchild = new CMPPServerChildEndpointEntity();
		cmppchild.setId("mixCmppChild");
		cmppchild.setChartset(Charset.forName("utf-8"));
		cmppchild.setGroupName("test");
		cmppchild.setUserName("test01");
		cmppchild.setPassword("1qaz2wsx");

		cmppchild.setValid(true);
		cmppchild.setVersion((short) 0x20);

		cmppchild.setMaxChannels((short) 1);
		cmppchild.setRetryWaitTimeSec((short) 30);
		cmppchild.setMaxRetryCnt((short) 3);

		cmppchild.setReSendFailMsg(TestConstants.isReSendFailMsg);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();

		serverhandlers.add(new AbstractBusinessHandler() {

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				CMPPResponseSenderHandler handler = new CMPPResponseSenderHandler();
				handler.setEndpointEntity(getEndpointEntity());
				ctx.pipeline().addAfter(GlobalConstance.sessionStateManager, handler.name(), handler);
				ctx.pipeline().remove(this);
			}

			@Override
			public String name() {
				return "AddCMPPResponseSenderHandler";
			}

		});

		serverhandlers.add(cmppreceiver);
		cmppchild.setBusinessHandlerSet(serverhandlers);
		server.addchild(cmppchild);

		SgipServerChildEndpointEntity sgipchild = new SgipServerChildEndpointEntity();
		sgipchild.setId("mixsgipchild");
		sgipchild.setLoginName("33301");
		sgipchild.setLoginPassowrd("0555");
		sgipchild.setNodeId(3025000001L);
		sgipchild.setValid(true);
		sgipchild.setChannelType(ChannelType.DUPLEX);
		sgipchild.setMaxChannels((short) 3);
		sgipchild.setRetryWaitTimeSec((short) 30);
		sgipchild.setMaxRetryCnt((short) 3);
		sgipchild.setReSendFailMsg(TestConstants.isReSendFailMsg);
		sgipchild.setIdleTimeSec((short) 30);
		sgipchild.setSupportLongmsg(SupportLongMessage.BOTH);
		serverhandlers = new ArrayList<BusinessHandlerInterface>();

		serverhandlers.add(sgipreceiver);
		sgipchild.setBusinessHandlerSet(serverhandlers);
		server.addchild(sgipchild);

		SMGPServerChildEndpointEntity sgmpchild = new SMGPServerChildEndpointEntity();
		sgmpchild.setId("mixsmgpchild");
		sgmpchild.setClientID("333");
		sgmpchild.setPassword("0555");
		sgmpchild.setValid(true);
		sgmpchild.setChannelType(ChannelType.DUPLEX);
		sgmpchild.setClientVersion((byte) 0x13);
		sgmpchild.setMaxChannels((short) 3);
		sgmpchild.setRetryWaitTimeSec((short) 30);
		sgmpchild.setMaxRetryCnt((short) 3);
		sgmpchild.setReSendFailMsg(TestConstants.isReSendFailMsg);
		sgmpchild.setIdleTimeSec((short) 15);
		sgmpchild.setSupportLongmsg(SupportLongMessage.SEND); // 接收长短信时不自动合并
		serverhandlers = new ArrayList<BusinessHandlerInterface>();

		serverhandlers.add(sgmpreceiver);
		sgmpchild.setBusinessHandlerSet(serverhandlers);
		server.addchild(sgmpchild);

		SMPPServerChildEndpointEntity smppchild = new SMPPServerChildEndpointEntity();
		smppchild.setId("mixsmppchild");
		smppchild.setSystemId("901782");
		smppchild.setPassword("ICP");
		smppchild.setValid(true);
		smppchild.setChannelType(ChannelType.DUPLEX);
		smppchild.setMaxChannels((short) 3);
		smppchild.setRetryWaitTimeSec((short) 30);
		smppchild.setMaxRetryCnt((short) 3);
		smppchild.setReSendFailMsg(TestConstants.isReSendFailMsg);
		smppchild.setIdleTimeSec((short) 15);
		serverhandlers = new ArrayList<BusinessHandlerInterface>();

		serverhandlers.add(smppreceiver);
		smppchild.setBusinessHandlerSet(serverhandlers);
		server.addchild(smppchild);

	}
	ExecutorService es = Executors.newFixedThreadPool(4);
	@Test
	public void testMixed() throws Exception {
		EndpointManager.INS.openEndpoint(server);
		
		
		Future<Boolean> cmppf = es.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				
				testCMPPEndpoint();
				return Boolean.TRUE;
			}	
			
		});
		
		Future<Boolean> sgipf = es.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				
				testsgipEndpoint();
				return Boolean.TRUE;
			}	
			
		});
		Future<Boolean> smgpf = es.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				
				testSMGPEndpoint();
				return Boolean.TRUE;
			}	
			
		});
		Future<Boolean> smppf = es.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				
				testSMPPEndpoint();
				return Boolean.TRUE;
			}	
			
		});
		cmppf.get();
		sgipf.get();
		smgpf.get();
		smppf.get();
		
		EndpointManager.INS.close(server);
	}

	private void testCMPPEndpoint() throws Exception {

		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("mixcmppclient");
//		client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");
		client.setPort(port);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("test01");
		client.setPassword("1qaz2wsx");

		client.setMaxChannels((short) 1);
		client.setVersion((short) 0x20);
		client.setRetryWaitTimeSec((short) 30);
		client.setMaxRetryCnt((short) 1);
		client.setCloseWhenRetryFailed(false);
		client.setUseSSL(useSSL);
		client.setWriteLimit(writeLimit);
		client.setWindow(16);
		client.setReSendFailMsg(TestConstants.isReSendFailMsg);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();

		CMPPSessionConnectedHandler sender = new CMPPSessionConnectedHandler(count);
		clienthandlers.add(sender);
		client.setBusinessHandlerSet(clienthandlers);

		EndpointManager.INS.openEndpoint(client);

//		manager.startConnectionCheckTask();
		System.out.println("start.....");
		sender.getSendover().get();
		boolean connection = client.getSingletonConnector().getConnectionNum() > 0;
		while (client.getSingletonConnector().getConnectionNum() > 0 && cmppreceiver.getCnt().get() < count) {
			Thread.sleep(1000);
			connection = true;
			System.out.println("cmppwait.....　" + cmppreceiver.getCnt().get());
		}
		Assert.assertEquals(true, cmppreceiver.getCnt().get() == count || connection);
		Thread.sleep(1000);

		EndpointManager.INS.close(client);
		Assert.assertEquals(count, cmppreceiver.getCnt().get());
		System.out.println("end.....");
	}

	private void testsgipEndpoint() throws Exception {

		SgipClientEndpointEntity client = new SgipClientEndpointEntity();
		client.setId("mixsgipclient");
		client.setHost("127.0.0.1");
		client.setPort(port);
		client.setLoginName("33301");
		client.setLoginPassowrd("0555");
		client.setChannelType(ChannelType.DUPLEX);
		client.setNodeId(3073100002L);
		client.setMaxChannels((short) 1);
		client.setRetryWaitTimeSec((short) 100);
		client.setUseSSL(useSSL);
		client.setReSendFailMsg(TestConstants.isReSendFailMsg);
		client.setIdleTimeSec((short) 120);
		client.setWriteLimit(writeLimit);
//		client.setReadLimit(200);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new SgipReportRequestMessageHandler());
		SGIPSessionConnectedHandler sender = new SGIPSessionConnectedHandler(count);
		clienthandlers.add(sender);
		client.setBusinessHandlerSet(clienthandlers);
		EndpointManager.INS.openEndpoint(client);

		System.out.println("sgip start.....");
		sender.getSendover().get();
		boolean connection = client.getSingletonConnector().getConnectionNum() > 0;
		while (client.getSingletonConnector().getConnectionNum() > 0 && sgipreceiver.getCnt().get() < count) {
			Thread.sleep(1000);
			connection = true;
			System.out.println("sgipwait.....　" + sgipreceiver.getCnt().get());
		}
		Assert.assertEquals(true, sgipreceiver.getCnt().get() == count || connection);
		EndpointManager.INS.close(client);
		Assert.assertEquals(count , sgipreceiver.getCnt().get());

	}

	private void testSMGPEndpoint() throws Exception {

		SMGPClientEndpointEntity client = new SMGPClientEndpointEntity();
		client.setId("mixsmgpclient");
		client.setHost("127.0.0.1");
		client.setPort(port);
		client.setClientID("333");
		client.setPassword("0555");
		client.setChannelType(ChannelType.DUPLEX);

		client.setMaxChannels((short) 1);
		client.setRetryWaitTimeSec((short) 100);
		client.setUseSSL(useSSL);
		client.setReSendFailMsg(TestConstants.isReSendFailMsg);
		client.setClientVersion((byte) 0x13);
		client.setWriteLimit(writeLimit);
//		client.setReadLimit(200);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();

		SMGPSessionConnectedHandler sender = new SMGPSessionConnectedHandler(count);
		clienthandlers.add(sender);
		client.setBusinessHandlerSet(clienthandlers);

		EndpointManager.INS.openEndpoint(client);

		System.out.println("start.....");
		sender.getSendover().get();
		boolean connection = client.getSingletonConnector().getConnectionNum() > 0;
		while (client.getSingletonConnector().getConnectionNum() > 0 && sgmpreceiver.getCnt().get() < count) {
			Thread.sleep(1000);
			connection = true;
			System.out.println("smgpwait.....　" + sgmpreceiver.getCnt().get());
		}
		Assert.assertEquals(true, sgmpreceiver.getCnt().get() == count || connection);
		EndpointManager.INS.close(client);
		Assert.assertEquals(count, sgmpreceiver.getCnt().get());

	}

	private void testSMPPEndpoint() throws Exception {

		SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
		client.setId("mixsmppclient");
		client.setHost("127.0.0.1");
		client.setPort(port);
		client.setSystemId("901782");
		client.setPassword("ICP");
		client.setChannelType(ChannelType.DUPLEX);
		client.setSplitType(SmppSplitType.PAYLOADPARAM);
		client.setInterfaceVersion((byte) 0x34);
		client.setMaxChannels((short) 1);
		client.setRetryWaitTimeSec((short) 100);
		client.setUseSSL(useSSL);
		client.setReSendFailMsg(TestConstants.isReSendFailMsg);
		client.setWriteLimit(writeLimit);
//		client.setReadLimit(200);
		client.setSupportLongmsg(SupportLongMessage.SEND); // 接收长短信时不自动合并
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		SMPPSessionConnectedHandler sender = new SMPPSessionConnectedHandler(count);
		clienthandlers.add(sender);
		client.setBusinessHandlerSet(clienthandlers);

		EndpointManager.INS.openEndpoint(client);
		System.out.println("start.....");
		sender.getSendover().get();
		boolean connection = client.getSingletonConnector().getConnectionNum() > 0;
		while (client.getSingletonConnector().getConnectionNum() > 0 && smppreceiver.getCnt().get() < count) {
			Thread.sleep(1000);
			connection = true;
			System.out.println("smppwait.....　" + smppreceiver.getCnt().get());
		}
		Assert.assertEquals(true, smppreceiver.getCnt().get() == count || connection);
		EndpointManager.INS.close(client);

		Assert.assertEquals(count, smppreceiver.getCnt().get());

	}
}
