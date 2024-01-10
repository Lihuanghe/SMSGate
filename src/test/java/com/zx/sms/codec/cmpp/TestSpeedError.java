package com.zx.sms.codec.cmpp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.TestConstants;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.handler.cmpp.CMPPDeliverLongMessageHandler;
import com.zx.sms.handler.cmpp.CMPPSubmitLongMessageHandler;
import com.zx.sms.session.cmpp.SessionStateManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.WindowSizeChannelTrafficShapingHandler;

public class TestSpeedError {

	private short reSendTime = 3;
	private final int speedOverTime = 3;

	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {

		@Override
		protected void initChannel(Channel ch) throws Exception {
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
			client.setOverSpeedSendCountLimit(speedOverTime);
			// 增加流量整形 ，每个连接每秒发送，接收消息数不超过配置的值
			pipeline.addLast("ChannelTrafficAfter",
					new WindowSizeChannelTrafficShapingHandler(client, 100));
			
			pipeline.addLast("session", new SessionStateManager(client, new ConcurrentHashMap(), true));
			pipeline.addLast( "CMPPDeliverLongMessageHandler", new CMPPDeliverLongMessageHandler(client));
			pipeline.addLast("CMPPSubmitLongMessageHandler",  new CMPPSubmitLongMessageHandler(client));
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
		msg.setServiceId("10000");
		msg.setSrcId("10000");

		ch.writeOutbound(msg);
		// 等待重发
		Thread.sleep((reSendTime + 1) * 1000);
		// 一共发送了2条MT消息
		Assert.assertEquals(2, sessionhandler.getWriteCount());

		// 有一条等待发送的消息
		Assert.assertEquals(1, sessionhandler.getWaittingResp());

		ByteBuf recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);

		// 因为上面等待超时，会重发一次，所以这里会收到两条
		recvMsg = ch.readOutbound();
		Assert.assertNotNull(recvMsg);

		// 回复N条超速错误
		int cnt = speedOverTime+2;
		int i = 0;
		while(i++<cnt) {
			CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(msg.getHeader().getSequenceId());
			resp.setResult(8L);
			ch.writeOutbound(resp); // 把resp转化为ByteBuf
			recvMsg = ch.readOutbound();
			ch.writeInbound(recvMsg);
			Thread.sleep((reSendTime - 1) * 1000);
			if(i < speedOverTime) {
				Assert.assertEquals(2+i, sessionhandler.getWriteCount());
				// 收到超速错，会再次重发一次
				recvMsg = ch.readOutbound();
				Assert.assertNotNull(recvMsg);
			}else {
				//超过最大超速重发数后，不再重发
				Assert.assertEquals(2+speedOverTime, sessionhandler.getWriteCount());
				recvMsg = ch.readOutbound();
			}
		}

		// 回复一条正确接收
		CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(msg.getHeader().getSequenceId());
		resp.setResult(0L);
		ch.writeOutbound(resp); // 把resp转化为ByteBuf
		recvMsg = ch.readOutbound();
		ch.writeInbound(recvMsg);
		Thread.sleep((reSendTime - 1) * 1000);
		
		CmppSubmitResponseMessage respret ,lastRsp=null;
		while((respret = ch.readInbound())!=null) {
			lastRsp = respret;
		}; //读取到最后一个Response
			
		Assert.assertEquals(lastRsp.getMsgId(), resp.getMsgId());

		// 没有等待发送的消息
		Assert.assertEquals(0, sessionhandler.getWaittingResp());
		Assert.assertEquals(2+speedOverTime, sessionhandler.getWriteCount());
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
		msg.setServiceId("10000");
		msg.setSrcId("10000");
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
	
	@Test
	public void testDeliverCheckReceiveAndSendMsgid() throws Exception
	{
		
		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage();
		msg.setDestId("10658909");
		msg.setLinkid("0000");
		// 70个汉字
		msg.setMsgContent(TestConstants.testSmsContent);
		msg.setMsgId(new MsgId());
		msg.setServiceid("10006");
		msg.setSrcterminalId("13800138000");
		msg.setSrcterminalType((short) 1);
		
		List<CmppDeliverRequestMessage> msgs = LongMessageFrameHolder.splitLongSmsMessage(null, msg);
		SessionStateManager sessionhandler = (SessionStateManager) ch.pipeline().get("session");
		List<String> originMsgid = new ArrayList<String>();
		for(CmppDeliverRequestMessage part : msgs) {
			originMsgid.add(part.getMsgId().toString() +","+ part.getSequenceNo());
			sessionhandler.writeMessagesync(part);
		}
		System.out.println(StringUtils.join(originMsgid,"|"));
		ByteBuf reqBuf ;
		
		while((reqBuf = ch.readOutbound()) != null) {
			ch.writeInbound(reqBuf);
		}
		
		List<String> recvMsgid = new ArrayList<String>();
		CmppDeliverRequestMessage recvMsg = ch.readInbound();
		recvMsgid.add(recvMsg.getMsgId().toString()+","+recvMsg.getSequenceNo());
		
		for(CmppDeliverRequestMessage frag : recvMsg.getFragments()) {
			recvMsgid.add(frag.getMsgId().toString()+","+frag.getSequenceNo());
		}
		
		System.out.println(StringUtils.join(recvMsgid,"|"));
		Assert.assertEquals(StringUtils.join(originMsgid,"|"), StringUtils.join(recvMsgid,"|"));
	}
	
	
	@Test
	public void testSubmitCheckReceiveAndSendMsgid() throws Exception
	{
		
		CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
		msg.setDestterminalId(new String[] { "13800138000" });
		msg.setLinkID("0000");
		msg.setMsgContent(TestConstants.testSmsContent);
		msg.setMsgid(GlobalConstance.emptyMsgId);
		msg.setServiceId("10000");
		msg.setSrcId("100001");
		
		List<CmppSubmitRequestMessage> msgs = LongMessageFrameHolder.splitLongSmsMessage(null, msg);
		SessionStateManager sessionhandler = (SessionStateManager) ch.pipeline().get("session");
		List<String> originMsgid = new ArrayList<String>();
		for(CmppSubmitRequestMessage part : msgs) {
			originMsgid.add(part.getMsgid().toString() +","+ part.getSequenceNo());
			sessionhandler.writeMessagesync(part);
		}
		System.out.println(StringUtils.join(originMsgid,"|"));
		ByteBuf reqBuf ;
		
		while((reqBuf = ch.readOutbound()) != null) {
			ch.writeInbound(reqBuf);
		}
		
		List<String> recvMsgid = new ArrayList<String>();
		CmppSubmitRequestMessage recvMsg = ch.readInbound();
		recvMsgid.add(recvMsg.getMsgid().toString()+","+recvMsg.getSequenceNo());
		
		for(CmppSubmitRequestMessage frag : recvMsg.getFragments()) {
			recvMsgid.add(frag.getMsgid().toString()+","+frag.getSequenceNo());
		}
		
		System.out.println(StringUtils.join(recvMsgid,"|"));
		Assert.assertEquals(StringUtils.join(originMsgid,"|"), StringUtils.join(recvMsgid,"|"));
	}
}
