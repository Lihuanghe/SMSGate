package com.zx.sms.codec.cmpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.nio.charset.Charset;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.session.cmpp.SessionStateManager;

public class TestSpeedError {
	
	private short reSendTime = 3;
	
	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ResourceLeakDetector.setLevel(Level.ADVANCED);
			ChannelPipeline pipeline = ch.pipeline();
			CMPPCodecChannelInitializer codec = new CMPPCodecChannelInitializer();
			
			CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
			client.setId("client");
			client.setHost("127.0.0.1");
			client.setPort(7891);
			client.setChartset(Charset.forName("utf-8"));
			client.setGroupName("test");
			client.setUserName("901782");
			client.setPassword("ICP");


			client.setMaxChannels((short)12);
			client.setWindows((short)16);
			client.setVersion((short)0x20);
			client.setRetryWaitTimeSec(reSendTime);
			client.setUseSSL(false);
			client.setReSendFailMsg(true);
			pipeline.addLast("session",new SessionStateManager(client, new HashMap(), new HashMap()));
		}
	});
	
	
	@Test
	public void test() throws InterruptedException{
		SessionStateManager sessionhandler = (SessionStateManager)ch.pipeline().get("session");
		//测试超速错误的重发机制
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[]{"13800138000"});
		msg.setLinkID("0000");
		msg.setMsgContent("123");
		msg.setMsgid(new MsgId());
		msg.setServiceId("10086");
		msg.setSrcId("10086");
		
		ch.writeOutbound(msg);
		//等待重发
		Thread.sleep((reSendTime + 1)*1000);
		//一共发送了2条MT消息
		Assert.assertEquals(2, sessionhandler.getWriteCount());
		
		//有一条等待发送的消息
		Assert.assertEquals(1, sessionhandler.getWaittingResp());
		
		CmppSubmitRequestMessage recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);
		
		//新为上面等待超时，会重发一次，所以这里会收到两条
		recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);
		
		//回复一条超速错误
		CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(recvMsg.getHeader().getSequenceId());
		resp.setResult(8L);
		ch.writeInbound(resp);
		
		Thread.sleep(1000);
		//一共发送了3条MT消息
		Assert.assertEquals(3, sessionhandler.getWriteCount());
		//收到超速错，会再次重发一次
		recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);
		
		//回复一条正确接收
		resp = new CmppSubmitResponseMessage(recvMsg.getHeader().getSequenceId());
		resp.setResult(0L);
		ch.writeInbound(resp);
		
		//没有等待发送的消息
		Assert.assertEquals(0, sessionhandler.getWaittingResp());
		
		//等待重发超时
		Thread.sleep((reSendTime + 1)*1000);
		//这次接收的是Null
		recvMsg = ch.readOutbound();
		Assert.assertNull(recvMsg);
	}
}
