package com.zx.sms.connect.manager;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPMessageReceiveHandler;
import com.zx.sms.connect.manager.cmpp.CMPPResponseSenderHandler;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPSessionConnectedHandler;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TestReSendMsgWhenClose {
	@Test
	public void test() throws Exception {
		EndpointManager.INS.removeAll();
		int port = 16890;
		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("0.0.0.0");
		server.setPort(port);
		server.setValid(true);
		// 使用ssl加密数据流
		server.setUseSSL(false);

		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("ReSendcmppchild");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("test01");
		child.setPassword("1qaz2wsx");

		child.setValid(true);
		child.setVersion((short) 0x20);

		child.setMaxChannels((short) 10);
		child.setRetryWaitTimeSec((short) 30);
		child.setMaxRetryCnt((short) 3);
		
		//不开启IP白名单
//		List<String> iplist = new ArrayList<String>();
//		iplist.add("192.168.98.48/18");
//		child.setAllowedAddr(iplist);
		
		child.setReSendFailMsg(false);
		// child.setWriteLimit(200);
		// child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();

		CMPPMessageReceiveHandler receiver = new CMPPMessageReceiveHandler();

		serverhandlers.add(receiver);
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		EndpointManager.INS.openEndpoint(server);

		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("ReSendclient");
//		client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");
		client.setPort(port);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("test01");
		client.setPassword("1qaz2wsx");

		client.setMaxChannels((short) 2);
		client.setVersion((short) 0x20);
		client.setRetryWaitTimeSec((short) 30);
		client.setMaxRetryCnt((short)1);
		client.setCloseWhenRetryFailed(false);
		client.setUseSSL(false);
//		 client.setWriteLimit(150);
		client.setWindow(0);
		client.setReSendFailMsg(true);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		
		CMPPSessionConnectedHandler sender = new CMPPSessionConnectedHandler(0);
		clienthandlers.add(sender);
		client.setBusinessHandlerSet(clienthandlers);
		for(int i = 0 ;i<  client.getMaxChannels();i++) {
			EndpointManager.INS.openEndpoint(client);
		}
		
		
		System.out.println("start.....");
		sender.getSendover().get();
		//发送消息，不回response
		Channel ch = client.getSingletonConnector().fetch();
		int perCount =  RandomUtils.nextInt(10,50); 
		int count = client.getMaxChannels() * perCount;
		for(int i = 0 ;i < count ;i++) {
			CmppSubmitRequestMessage msg = CmppSubmitRequestMessage.create("13800138000", "106872"+i, "Test"+i);
			ch.write(msg);
		}
		Thread.sleep(1000);
		ch.close();
		Thread.sleep(1000);
		EndpointManager.INS.close(client);
		//加上回复response的handler
		
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
		Thread.sleep(1000);
		//重新打开连接，上次未收到response的消息
		EndpointManager.INS.openEndpoint(client);
		sender.getSendover().get();

		Thread.sleep(2000);
		
		EndpointManager.INS.close(server);
		EndpointManager.INS.close(client);
		EndpointManager.INS.removeAll();
		Assert.assertEquals(3 * count,  receiver.getCnt().get());
		System.out.println("end.....count: " + 3 * count);
	}
}
