package com.zx.sms.transgate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinamobile.cmos.sms.SmsDcs;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.wap.UniqueLongMsgId;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointEntity.SupportLongMessage;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.TestConstants;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPResponseSenderHandler;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TestReportForward {
	private static final Logger logger = LoggerFactory.getLogger(TestReportForward.class);

	/**
	 * 测试短信网接收接收，回复，转发状态报告
	 * S1 模拟运营商网关 
	 * Ts 模拟转发网关分配置的Sp服务账号 
	 * Tc 模拟转发上游通道端账号 
	 * C1 模拟一个Sp用户
	 * 消息路径 
	 * Submit C1 --> Ts ===> Tc --> S1 
	 * Report S1 --> Tc ===> Ts --> C1
	 * 
	 * @throws InterruptedException
	 */

	private Map<String, ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>>> uidMap = new ConcurrentHashMap<String, ImmutablePair<AtomicInteger, ImmutablePair<UniqueLongMsgId, Map<Integer, MsgId>>>>();
	private Map<String, Map<String, UniqueLongMsgId>> msgIdMap = new ConcurrentHashMap<String, Map<String, UniqueLongMsgId>>();

	@Test
	public void testReportForward() throws InterruptedException {
		int count = TestConstants.Count; //发送消息总数
		
		int port = 26890;
		ForwardHander forward = new ForwardHander(null);
		String s1Id = createS1(port,forward); // 创建运营商，模拟最终接收短信，并回复状态报告
		Thread.sleep(1000);
		String tcId = createTc(port); // 创建转发器客户端并连到运营商
		Thread.sleep(1000);
		int tsport = 26891;
		String tsId = createTS(tcId, tsport); // 创建转发器的服务端，给Sp提供账号，收到的消息都转到tcId
		Thread.sleep(1000);

		String cid = "C1"; //下边是创建模拟Sp ,提交短信，并等待状态报告回来
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId(cid + "client");
//		client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");
		client.setPort(tsport);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("test01");
		client.setPassword("1qaz2wsx");

		client.setMaxChannels((short) 10);
		client.setVersion((short) 0x20);
		client.setRetryWaitTimeSec((short) 30);
		client.setMaxRetryCnt((short) 1);
		client.setCloseWhenRetryFailed(false);
		client.setUseSSL(false);
//		 client.setWriteLimit(600);
		client.setWindow(64);
		client.setReSendFailMsg(TestConstants.isReSendFailMsg);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		
		client.setDefaultDcsBuilder(new TestSmsDcsBuilder());
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		
		//用于检查收到的状态和response的msgId是否一样
		final Map<String,AtomicInteger> checkMsgIdCnt = new  ConcurrentHashMap<String, AtomicInteger>();
		DefaultPromise sendover = new DefaultPromise(GlobalEventExecutor.INSTANCE);
		final AtomicInteger seq = new AtomicInteger(0);
		SessionConnectedHandler sender = new SessionConnectedHandler(new AtomicInteger(count),sendover) {
			
			
			@Override
			protected BaseMessage createTestReq(String content) {
				CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
				msg.setDestterminalId("13800138005");
				//有机率端口号手机号相同
				int t = seq.incrementAndGet();
				//t太小造成相同手机号，端口号重复太多，会锁冲突，速度下降
				if(t >= 500)seq.set(0);
				
				msg.setSrcId("100009"+t);
				msg.setLinkID("0000");
				
				if(RandomUtils.nextBoolean() ) {
					msg.setMsgContent(content);
				}else {
					msg.setMsgContent(content
							+ " 16:28:40.453 [busiWo中国rk-6] IN0.453 [busiWork-6] INFO  ReceiveHandler - channels : 1,ToFO 总计查找1次");
				}
				
				
				msg.setRegisteredDelivery((short) 1);
				msg.setServiceId("10000");
				return msg;

			}
			
			public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
				
				super.channelRead(ctx, msg);
				
				if (msg instanceof CmppDeliverRequestMessage) { 
					
					//收状态
					CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
					
					CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
					responseMessage.setResult(0);
					responseMessage.setMsgId(e.getMsgId());
					ctx.channel().writeAndFlush(responseMessage);
					
					if(e.isReport()) {
						String msgId = e.getReportRequestMessage().getMsgId().toString();
						AtomicInteger atom = checkMsgIdCnt.putIfAbsent(msgId, new AtomicInteger(-1));
						if(atom!=null) {
							if(atom.decrementAndGet() == 0)
								checkMsgIdCnt.remove(msgId);
						}
					}
				} else if (msg instanceof CmppSubmitResponseMessage) {
					
					//收Response
					CmppSubmitResponseMessage e = (CmppSubmitResponseMessage) msg;
					String msgId = e.getMsgId().toString();
					AtomicInteger atom = checkMsgIdCnt.putIfAbsent(msgId, new AtomicInteger(1));
					if(atom!=null) {
						if(atom.incrementAndGet() == 0)
							checkMsgIdCnt.remove(msgId);
					}
				}  else {
					ctx.fireChannelRead(msg);
				}
			}

		};
		clienthandlers.add(sender);
		client.setBusinessHandlerSet(clienthandlers);
		
		//模拟SP开启5个连接
		EndpointManager.INS.openEndpoint(client);
		Thread.sleep(100);
		EndpointManager.INS.openEndpoint(client);
		Thread.sleep(100);
		EndpointManager.INS.openEndpoint(client);
		Thread.sleep(100);
		EndpointManager.INS.openEndpoint(client);
		Thread.sleep(100);
		EndpointManager.INS.openEndpoint(client);
		Thread.sleep(1000);
		//等待Sp所有短信发送完
		try {
			logger.info("等待Sp所有短信发送完...." );
			sendover.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		int cnt = uidMap.size();
		int checksize = 0;
		while ( cnt > 0 ) {
			logger.info("等待所有状态报告回来...." +"size...{}.." ,cnt );
			Thread.sleep(1000);
			
			if(cnt == uidMap.size()) {
				//连接5次size大小都不变即退出
				if(checksize++ > 5)
					break;
			}
			cnt = uidMap.size();
		}
		Thread.sleep(10000);
		EndpointManager.INS.remove(client.getId());
		EndpointManager.INS.remove(tsId);
		EndpointManager.INS.remove(tcId);
		EndpointManager.INS.remove(s1Id);
		EndpointManager.INS.removeAll();
		Thread.sleep(1000);
		logger.info("检查状态报告是否完全匹配上...." );
		logger.info("checkMsgIdMap:{}; count : {}", checkMsgIdCnt.size(),forward.getTotalReceiveCnt());
		Iterator<Entry<String,AtomicInteger>> itor = checkMsgIdCnt.entrySet().iterator();
		int check = 0;
		int checkErr = 0;
		while(itor.hasNext()) {
			Entry<String,AtomicInteger> entry = itor.next();
			check = entry.getValue().get();
			if(check != 0) {
				logger.info("checking report undelived count....{}" ,entry.getKey());
				checkErr++ ;
			}
		}
		logger.info("检查状态报告是否完全匹配上....{}..." ,checkErr);
		Assert.assertTrue(checkErr == 0); 
		Assert.assertTrue(count<=forward.getTotalReceiveCnt());  //多连接下，实际收到的要比发送的多
	}

	private String createS1(int port,ForwardHander forward) {

		String Sid = "S1";
		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId(Sid);
		server.setHost("0.0.0.0");
		server.setPort(port);
		server.setValid(true);
		// 使用ssl加密数据流
		server.setUseSSL(false);

		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId(Sid + "child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("test01");
		child.setPassword("1qaz2wsx");

		child.setValid(true);
		child.setVersion((short) 0x20);

		child.setMaxChannels((short) 10);
		child.setRetryWaitTimeSec((short) 30);
		child.setMaxRetryCnt((short) 3);

		child.setReSendFailMsg(TestConstants.isReSendFailMsg);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		forward.setEndpointEntity(child);
		serverhandlers.add(forward);
		serverhandlers.add(new AbstractBusinessHandler() {

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				CMPPResponseSenderHandler handler = new CMPPResponseSenderHandler(true);
				handler.setEndpointEntity(getEndpointEntity());
				ctx.pipeline().addAfter(GlobalConstance.sessionStateManager, handler.name(), handler);
				ctx.pipeline().remove(this);
			}

			@Override
			public String name() {
				return "AddS1ResponseSenderHandler";
			}

		});
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		EndpointManager.INS.openEndpoint(server);
		return server.getId();
	}

	private String createTc(int serverPort) throws InterruptedException {
		String cid = "TC";
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId(cid + "client");
//		client.setLocalhost("127.0.0.1");
		// client.setLocalport(65521);
		client.setHost("127.0.0.1");
		client.setPort(serverPort);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("test01");
		client.setPassword("1qaz2wsx");

		client.setMaxChannels((short) 10);
		client.setVersion((short) 0x20);
		client.setRetryWaitTimeSec((short) 30);
		client.setMaxRetryCnt((short) 1);
		client.setCloseWhenRetryFailed(false);
		client.setUseSSL(false);
//		 client.setWriteLimit(150);
		client.setWindow(100);
		client.setReSendFailMsg(TestConstants.isReSendFailMsg);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new AbstractBusinessHandler() {

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				ForwardResponseHander handler = new ForwardResponseHander(uidMap, msgIdMap);
				handler.setEndpointEntity(getEndpointEntity());
				ctx.pipeline().addAfter(GlobalConstance.sessionStateManager, handler.name(), handler);
				ctx.pipeline().remove(this);
			}
			@Override
			public String name() {
				return "AddForwardResponseSenderHandler";
			}
		});
		client.setBusinessHandlerSet(clienthandlers);
		EndpointManager.INS.openEndpoint(client);
		EndpointManager.INS.openEndpoint(client);
		EndpointManager.INS.openEndpoint(client);
		EndpointManager.INS.openEndpoint(client);
		EndpointManager.INS.openEndpoint(client);
		//等待连接建立 完成
		Thread.sleep(2000);
		return client.getId();
	}

	private String createTS(String forwardEid, int port) {

		String Sid = "TS";
		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId(Sid);
		server.setHost("0.0.0.0");
		server.setPort(port);
		server.setValid(true);
		// 使用ssl加密数据流
		server.setUseSSL(false);

		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId(Sid + "child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("test01");
		child.setPassword("1qaz2wsx");

		child.setValid(true);
		child.setVersion((short) 0x20);

		child.setMaxChannels((short) 10);
		child.setRetryWaitTimeSec((short) 30);
		child.setMaxRetryCnt((short) 3);

		child.setReSendFailMsg(TestConstants.isReSendFailMsg);

		child.setWindow(64); //加大窗口，加大状态报告发送速度
		
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();

		ForwardHander sender = new ForwardHander(forwardEid);

		serverhandlers.add(sender);

		serverhandlers.add(new AbstractBusinessHandler() {

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				ForwardResponseHander handler = new ForwardResponseHander(uidMap, msgIdMap);
				handler.setEndpointEntity(getEndpointEntity());
				ctx.pipeline().addAfter(GlobalConstance.sessionStateManager, handler.name(), handler);
				ctx.pipeline().remove(this);
			}

			@Override
			public String name() {
				return "AddForwardResponseSenderHandler";
			}

		});

		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		
		//再加一个账号
		child = new CMPPServerChildEndpointEntity();
		child.setId(Sid + "child1");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("test02");
		child.setPassword("1qaz2wsx");

		child.setValid(true);
		child.setVersion((short) 0x20);

		child.setMaxChannels((short) 10);
		child.setRetryWaitTimeSec((short) 30);
		child.setMaxRetryCnt((short) 3);

		child.setReSendFailMsg(TestConstants.isReSendFailMsg);

		child.setWindow(64); //加大窗口，加大状态报告发送速度
		
		serverhandlers = new ArrayList<BusinessHandlerInterface>();

		sender = new ForwardHander(forwardEid);

		serverhandlers.add(sender);

		serverhandlers.add(new AbstractBusinessHandler() {

			@Override
			public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
				ForwardResponseHander handler = new ForwardResponseHander(uidMap, msgIdMap);
				handler.setEndpointEntity(getEndpointEntity());
				ctx.pipeline().addAfter(GlobalConstance.sessionStateManager, handler.name(), handler);
				ctx.pipeline().remove(this);
			}

			@Override
			public String name() {
				return "AddForwardResponseSenderHandler";
			}

		});

		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		EndpointManager.INS.openEndpoint(server);

		return server.getId();
	}
}
