package com.zx.sms.codec.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

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
			pipeline.addLast("serverLog", new LoggingHandler(LogLevel.DEBUG));
			pipeline.addLast(codec.pipeName(), codec);

			CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
			client.setId("client");
			client.setHost("127.0.0.1");
			client.setPort(7891);
			client.setChartset(Charset.forName("utf-8"));
			client.setGroupName("test");
			client.setUserName("901782");
			client.setPassword("ICP");

			client.setMaxChannels((short) 12);
			client.setVersion((short) 0x20);
			client.setRetryWaitTimeSec(reSendTime);
			client.setUseSSL(false);
			client.setReSendFailMsg(true);
			pipeline.addLast("session", new SessionStateManager(client, new ConcurrentHashMap(), true));
		}
	});

	@Test
	public void test() throws InterruptedException {
		SessionStateManager sessionhandler = (SessionStateManager) ch.pipeline().get("session");
		// 测试超速错误的重发机制
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[] { "13800138000" });
		msg.setLinkID("0000");
		msg.setMsgContent("123");
		msg.setServiceId("10086");
		msg.setSrcId("10086");

		ch.writeOutbound(msg);
		// 等待重发
		Thread.sleep((reSendTime + 1) * 1000);
		// 一共发送了2条MT消息
		Assert.assertEquals(2, sessionhandler.getWriteCount());

		// 有一条等待发送的消息
		Assert.assertEquals(1, sessionhandler.getWaittingResp());

		ByteBuf recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);

		// 新为上面等待超时，会重发一次，所以这里会收到两条
		recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);

		// 回复一条超速错误
		CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(msg.getHeader().getSequenceId());
		resp.setResult(8L);
		ch.writeOutbound(resp); // 把resp转化为ByteBuf
		ch.writeInbound(ch.readOutbound());

		Thread.sleep(1000);
		// 一共发送了3条MT消息
		Assert.assertEquals(3, sessionhandler.getWriteCount());
		// 收到超速错，会再次重发一次
		recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);

		// 回复一条正确接收
		resp = new CmppSubmitResponseMessage(msg.getHeader().getSequenceId());
		resp.setResult(0L);
		ch.writeOutbound(resp); // 把resp转化为ByteBuf

		ch.writeInbound(ch.readOutbound());

		CmppSubmitResponseMessage respret = ch.readInbound();
		// System.out.println(respret.getRequest());
		Assert.assertSame(msg, respret.getRequest());
		Assert.assertNotEquals(respret.getMsgId(), msg.getMsgid());

		// 没有等待发送的消息
		Assert.assertEquals(0, sessionhandler.getWaittingResp());

		// 等待重发超时
		Thread.sleep((reSendTime + 1) * 1000);
		// 因为已收到response,发送成功了，不会再重发了，因此这次接收的是Null
		recvMsg = ch.readOutbound();
		Assert.assertNull(recvMsg);

	}

	@Test
	public void testterminated() throws IOException, ClassNotFoundException, InterruptedException {
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[] { "13800138000" });
		msg.setLinkID("0000");
		msg.setMsgContent("123asdf23asdgq5");
		msg.setMsgid(new MsgId());
		msg.setServiceId("10086");
		msg.setSrcId("10086");
		// 设置短信的生存时间为2s
		msg.setLifeTime(2);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(msg);
		byte[] b = bos.toByteArray();
		
		Thread.sleep(3000);
		
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
		CmppSubmitRequestMessage result = (CmppSubmitRequestMessage) in.readObject();

		ChannelFuture futurn = ch.writeAndFlush(result);
		// System.out.println(futurn.isSuccess());
		// Thread.sleep(100);
		Assert.assertTrue(!futurn.isSuccess());
		Assert.assertTrue("Msg Life over".equals(futurn.cause().getMessage()));
	}
}
