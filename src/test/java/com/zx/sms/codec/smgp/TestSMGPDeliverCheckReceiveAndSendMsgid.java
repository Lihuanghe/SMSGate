package com.zx.sms.codec.smgp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.TestConstants;
import com.zx.sms.connect.manager.smgp.SMGPClientEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPCodecChannelInitializer;
import com.zx.sms.handler.smgp.SMGPDeliverLongMessageHandler;
import com.zx.sms.handler.smgp.SMGPSubmitLongMessageHandler;
import com.zx.sms.session.smgp.SMGPSessionStateManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.WindowSizeChannelTrafficShapingHandler;

public class TestSMGPDeliverCheckReceiveAndSendMsgid {

	private short reSendTime = 3;

	protected EmbeddedChannel ch = new EmbeddedChannel(new ChannelInitializer<Channel>() {

		@Override
		protected void initChannel(Channel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
					
			SMGPClientEndpointEntity client = new SMGPClientEndpointEntity();
			client.setId("client");
			client.setHost("127.0.0.1");
			client.setPort(7890);
			client.setClientID("333");
			client.setPassword("0555");
			client.setChannelType(ChannelType.DUPLEX);

			client.setMaxChannels((short) 1);
			client.setRetryWaitTimeSec((short) 100);
			client.setUseSSL(false);
			client.setReSendFailMsg(TestConstants.isReSendFailMsg);
			
			SMGPCodecChannelInitializer codec = new SMGPCodecChannelInitializer(client.getClientVersion());
			pipeline.addLast("serverLog", new LoggingHandler(LogLevel.DEBUG));
			pipeline.addLast(codec.pipeName(), codec);

		
			// 增加流量整形 ，每个连接每秒发送，接收消息数不超过配置的值
			pipeline.addLast("ChannelTrafficAfter",
					new WindowSizeChannelTrafficShapingHandler(client, 100));
			
			pipeline.addLast("session", new SMGPSessionStateManager(client, new ConcurrentHashMap(), true));
			pipeline.addLast("SMGPDeliverLongMessageHandler", new SMGPDeliverLongMessageHandler(client));
			pipeline.addLast("SMGPSubmitLongMessageHandler",  new SMGPSubmitLongMessageHandler(client));
		}
	});

	
	@Test
	public void testDeliverCheckReceiveAndSendMsgid() throws Exception
	{
		SMGPDeliverMessage msg = new SMGPDeliverMessage();
		msg.setDestTermId("13800138000");
		msg.setLinkId("1023rsd");
		msg.setMsgContent(TestConstants.testSmsContent);
		msg.setSrcTermId("10000988");
	
		
		List<SMGPDeliverMessage> msgs = LongMessageFrameHolder.splitLongSmsMessage(null, msg);
		SMGPSessionStateManager sessionhandler = (SMGPSessionStateManager) ch.pipeline().get("session");
		List<String> originMsgid = new ArrayList<String>();
		for(SMGPDeliverMessage part : msgs) {
			originMsgid.add(part.getMsgId().toString() +","+ part.getSequenceNo());
			sessionhandler.writeMessagesync(part);
		}
		System.out.println(StringUtils.join(originMsgid,"|"));
		ByteBuf reqBuf ;
		
		while((reqBuf = ch.readOutbound()) != null) {
			ch.writeInbound(reqBuf);
		}
		
		List<String> recvMsgid = new ArrayList<String>();
		SMGPDeliverMessage recvMsg = ch.readInbound();
		recvMsgid.add(recvMsg.getMsgId().toString()+","+recvMsg.getSequenceNo());
		
		for(SMGPDeliverMessage frag : recvMsg.getFragments()) {
			recvMsgid.add(frag.getMsgId().toString()+","+frag.getSequenceNo());
		}
		
		System.out.println(StringUtils.join(recvMsgid,"|"));
		Assert.assertEquals(StringUtils.join(originMsgid,"|"), StringUtils.join(recvMsgid,"|"));
	}
	
}
