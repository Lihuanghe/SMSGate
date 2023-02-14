package com.zx.sms.codec.cmpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinamobile.cmos.sms.SmsConcatMessage;
import com.chinamobile.cmos.sms.SmsDcs;
import com.chinamobile.cmos.sms.SmsMessage;
import com.chinamobile.cmos.sms.SmsTextMessage;
import com.chinamobile.cmos.wap.push.SmsMmsNotificationMessage;
import com.chinamobile.cmos.wap.push.SmsWapPushMessage;
import com.chinamobile.cmos.wap.push.WapSIPush;
import com.chinamobile.cmos.wap.push.WapSLPush;
import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.AbstractTestMessageCodec;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.DefaultHeader;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.cmpp.wap.SmsMessageHolder;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.TestConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestCmppDeliverRequestMessageCodec extends AbstractTestMessageCodec<CmppDeliverRequestMessage> {
	private static final Logger logger = LoggerFactory.getLogger(TestCmppDeliverRequestMessageCodec.class);

	@Test
	public void testperformance() {
		int i = 0;
		int l = 100000;
		CmppDeliverRequestMessage msg = createTestReq(TestConstants.testSmsContent);
		// 先预热
		for (; i < 1000; i++) {
			decode(encode(msg));
		}
		// 开始计时

		long start = System.currentTimeMillis();

		for (i = 0; i < l; i++) {
			decode(encode(msg));
		}
		long end = System.currentTimeMillis();
		logger.info((end - start) * 1000000 / l + "ns");
	}

	@Test
	public void testCodec() {

		CmppDeliverRequestMessage msg = createTestReq("ad3 中");

		test0(msg);
	}

	@Test
	public void testReportCodec() {
		CmppDeliverRequestMessage msg = createTestReq("k k k ");
		msg.setMsgContent((SmsMessage) null);
		CmppReportRequestMessage reportRequestMessage = new CmppReportRequestMessage();
		reportRequestMessage.setSmscSequence(0x1234L);
		reportRequestMessage.setMsgId(new MsgId());
		reportRequestMessage.setDestterminalId("13800138000");
		reportRequestMessage.setStat("9876");
		msg.setReportRequestMessage(reportRequestMessage);
		test0(msg);
	}

	private void test0(CmppDeliverRequestMessage msg) {

		ByteBuf buf = encode(msg);
		ByteBuf newbuf = buf.copy();

		int length = buf.readableBytes();

		Assert.assertEquals(length, buf.readInt());
		Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
		Assert.assertEquals(msg.getHeader().getSequenceId(), buf.readInt());

		buf.release();
		CmppDeliverRequestMessage result = decode(newbuf);

		Assert.assertEquals(msg.getHeader().getSequenceId(), result.getHeader().getSequenceId());
		if (msg.isReport()) {
			Assert.assertEquals(msg.getReportRequestMessage().getSmscSequence(),
					result.getReportRequestMessage().getSmscSequence());
		} else {
			Assert.assertNotNull(result.getUniqueLongMsgId().getId());
			Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		}
		Assert.assertEquals(msg.getSrcterminalId(), result.getSrcterminalId());

	}

	private CmppDeliverRequestMessage createTestReq(String content) {

		Header header = new DefaultHeader();
		// 取时间，用来查看编码解码时间

		CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage(header);
		msg.setDestId("10"+RandomUtils.nextInt(138000, 188000));
		msg.setLinkid("0000");
		// 70个汉字
		msg.setMsgContent(content);
		msg.setMsgId(new MsgId());
		msg.setServiceid("10000");
		msg.setSrcterminalId("13800"+RandomUtils.nextInt(238000, 288000));
		msg.setSrcterminalType((short) 1);
		header.setSequenceId((int) System.nanoTime());
		return msg;
	}

	@Test
	public void testchinesecode() {
		
		CmppDeliverRequestMessage msg =  new CmppDeliverRequestMessage(new DefaultHeader());
		msg.setDestId("13800138000");
		msg.setLinkid("0000");
		msg.setMsgContent(new SmsTextMessage(TestConstants.testSmsContent,new SmsDcs((byte)15)));
		msg.setMsgId(new MsgId());
		msg.setServiceid("10000");
		msg.setSrcterminalId("13800138000");
		msg.setSrcterminalType((short) 1);
		msg.getHeader().setSequenceId((int) System.nanoTime());
		Assert.assertEquals(4,testlongCodec(msg));
		
		
		msg.setMsgContent(new SmsTextMessage(TestConstants.testSmsContent,new SmsDcs((byte)8)));
		Assert.assertEquals(4,testlongCodec(msg));
		//70字拆成1条
		msg = createTestReq(
				"1234567890123456789中01234567890123456789012345678901234567890123456789");
		Assert.assertEquals(1,testlongCodec(msg));
	}

	@Test
	public void testASCIIcode() {
		//159字母拆成1两条
		CmppDeliverRequestMessage msg = createTestReq(
				"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
		Assert.assertEquals(1,testlongCodec(msg));
		//160字母拆成2两条
		 msg = createTestReq(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		Assert.assertEquals(2,testlongCodec(msg));
	}

	@Test
	public void testSLPUSH() {
		CmppDeliverRequestMessage msg = createTestReq("");
		WapSLPush sl = new WapSLPush("http://www.baidu.com");
		SmsMessage wap = new SmsWapPushMessage(sl);
		msg.setMsgContent(wap);
		CmppDeliverRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage) result.getSmsMessage();
		WapSLPush actsl = (WapSLPush) smsmsg.getWbxml();
		Assert.assertEquals(sl.getUri(), actsl.getUri());
	}

	@Test
	public void testSIPUSH() {
		CmppDeliverRequestMessage msg = createTestReq("");
		WapSIPush si = new WapSIPush("http://www.baidu.com?", "baidu");
		SmsMessage wap = new SmsWapPushMessage(si);
		msg.setMsgContent(wap);
		CmppDeliverRequestMessage result = testWapCodec(msg);
		SmsWapPushMessage smsmsg = (SmsWapPushMessage) result.getSmsMessage();
		WapSIPush actsi = (WapSIPush) smsmsg.getWbxml();
		Assert.assertEquals(si.getUri(), actsi.getUri());
		Assert.assertEquals(si.getMessage(), actsi.getMessage());
	}

	@Test
	public void testMMSPUSH() {
		CmppDeliverRequestMessage msg = createTestReq("");
		SmsMmsNotificationMessage mms = new SmsMmsNotificationMessage(
				"https://www.baidu.com/s?wd=SMPPv3.4%20%E9%95%BF%E7%9F%AD%E4%BF%A1&rsv_spt=1&rsv_iqid=0xdd4666100001e74c&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=0&oq=SMPPv%2526lt%253B.4%2520ton%2520npi&rsv_t=50fdNrphqry%2FYfHh29wvp8KzJ9ogqigiPr33FT%2FpcGQu6X34vByQNu4O%2FLNZgIiXdd16&inputT=3203&rsv_pq=d576ead9000016eb&rsv_sug3=60&rsv_sug1=15&rsv_sug7=000&rsv_sug2=0&rsv_sug4=3937&rsv_sug=1",
				50 * 1024);
		msg.setMsgContent(mms);
		mms.setTransactionId("ABC");
		CmppDeliverRequestMessage result = testWapCodec(msg);
		SmsMmsNotificationMessage smsmsg = (SmsMmsNotificationMessage) result.getSmsMessage();
		Assert.assertEquals(smsmsg.getContentLocation_(), smsmsg.getContentLocation_());
	}

	public CmppDeliverRequestMessage testWapCodec(CmppDeliverRequestMessage msg) {
		channel().writeOutbound(msg);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		while (buf != null) {

			ByteBuf copy = buf.copy();
	    	copybuf.writeBytes(copy);
	    	copy.release();
			int length = buf.readableBytes();

			Assert.assertEquals(length, buf.readInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());

			buf = (ByteBuf) channel().readOutbound();
		}

		CmppDeliverRequestMessage result = decode(copybuf);
		System.out.println(result);
		Assert.assertTrue(result.getSmsMessage() instanceof SmsMessage);
		return result;
	}

	public int testlongCodec(CmppDeliverRequestMessage msg) {
		int frameCnt = 0;
		channel().writeOutbound(msg);
		ByteBuf buf = (ByteBuf) channel().readOutbound();
		ByteBuf copybuf = Unpooled.buffer();
		while (buf != null) {
			ByteBuf copy = buf.copy();
			copybuf.writeBytes(copy);
			copy.release();
			int length = buf.readableBytes();

			Assert.assertEquals(length, buf.readInt());
			Assert.assertEquals(msg.getPacketType().getCommandId(), buf.readInt());
			frameCnt++;
			buf = (ByteBuf) channel().readOutbound();
		}

		CmppDeliverRequestMessage result = decode(copybuf);

		System.out.println(result.getMsgContent());
		Assert.assertNotNull(result.getUniqueLongMsgId().getId());
		Assert.assertEquals(msg.getMsgContent(), result.getMsgContent());
		return frameCnt;
	}
	ExecutorService executor = Executors.newFixedThreadPool(200);
	// 多线程长短信合并
	@Test
	public void testConcurrentLongMessageMerge() throws Exception {
//		for(int i=0 ;i<100;i++) {
			int total = RandomUtils.nextInt(1000,3000);
			testConcurrentLongMessageMerge1(total,false);
			if(LongMessageFrameHolder.hasClusterLongMessageFrameProvider)
				testConcurrentLongMessageMerge1(total,true);
			
			testConcurrentLongMessageMerge2(total,false);
			if(LongMessageFrameHolder.hasClusterLongMessageFrameProvider)
				testConcurrentLongMessageMerge2(total,true);
//		}
	}
	

	private void testConcurrentLongMessageMerge1(int p_total,final boolean useredis) throws Exception {
		// 生成一个超长的短信
		String tmp = TestConstants.testSmsContent;
		final String longlongMsg = tmp + tmp +tmp;
		// 测试10次
		int randomSize = 0;
		
	
		long totalTime = 0;
		final int TOTLE = p_total;
		long sumsplit = 0;
		for (int i = 0; i < TOTLE; i++) {
			randomSize = RandomUtils.nextInt(130, 67 * 5);
			CmppDeliverRequestMessage lmsg = createTestReq(longlongMsg.substring(0, randomSize));
			
			lmsg.setDestId("1871" + i);
			// 长短信拆分
			SmsMessage msgcontent = lmsg.getSmsMessage();
			
			if(msgcontent instanceof SmsConcatMessage) {
				((SmsConcatMessage)msgcontent).setSeqNoKey(lmsg.getSrcIdAndDestId());
			}
			
			List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);

			// 保证同一条长短信，通过同一个tcp连接发送
			List<BaseMessage> msgs = new ArrayList<BaseMessage>();
			for (LongMessageFrame frame : frameList) {
				BaseMessage basemsg = (BaseMessage) lmsg.generateMessage(frame);
				msgs.add(basemsg);
			}
		

			int totalSplitMsg = msgs.size();
			sumsplit += totalSplitMsg;
			// 随机打乱
			Collections.shuffle(msgs);

			final CountDownLatch startSignal = new CountDownLatch(totalSplitMsg + 1);
			final CountDownLatch stop = new CountDownLatch(totalSplitMsg);
			final AtomicInteger count = new AtomicInteger();
			for (final BaseMessage split : msgs) {
				executor.submit(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						// 等待开始信号
						startSignal.await();
						// 启用多线程进行短信合并
						CmppDeliverRequestMessage splitMsg = (CmppDeliverRequestMessage) split;
						String key = splitMsg.getSrcterminalId() + splitMsg.getDestId();
						SmsMessageHolder hoder = LongMessageFrameHolder.INS.putAndget(null,key, ((LongSMSMessage) split),useredis);
						if (hoder != null) {
							// 只有一个线程能到达这里
							CmppDeliverRequestMessage result = (CmppDeliverRequestMessage) hoder.getMsg();
							result.setMsgContent(hoder.getSmsMessage());

							// 完成计数器加1
							count.incrementAndGet();
							stop.countDown();

							Assert.assertEquals(longlongMsg, result.getMsgContent());
							return true;
						}
						stop.countDown();
						return false;
					}
				});
				startSignal.countDown();
			}

			startSignal.countDown(); // 合并线程同时启动
			long start = System.nanoTime();
			stop.await(60,TimeUnit.SECONDS); // 等待结束

			Assert.assertEquals("合并成功的消息数：", 1, count.get());
			long end = System.nanoTime();
			totalTime += (end - start);
		}

		logger.info("testConcurrentLongMessageMerge1 "+ (useredis?"Redis":"     ")+"合并耗时 : " + totalTime / 1000000 + "ms，" + "长短信总条数: " + TOTLE + " 总分片数："+sumsplit + " 合并速度：" + (sumsplit*1000*1000000)/totalTime+"/s");
	}

	// 多线程长短信合并
	private void testConcurrentLongMessageMerge2(int p_total ,final boolean useredis) throws Exception {
		
		// 生成一个超长的短信
		String tmp = TestConstants.testSmsContent;
		final String longlongMsg = tmp + tmp + tmp;
		List<Character> chars = new ArrayList<Character>();
		for(char c: longlongMsg.toCharArray()) {
			chars.add(Character.valueOf(c));
		}
		long totalTime = 0;
		List<BaseMessage> msgs = new ArrayList<BaseMessage>();
		final int TOTLE = p_total;
		final Map<Integer,Integer> contentMap = new ConcurrentHashMap<Integer,Integer>(TOTLE);
		
		for (int i = 0; i < TOTLE; i++) {
			Collections.shuffle(chars);
			StringBuilder sb = new StringBuilder();
			int randomSize = RandomUtils.nextInt(70, 67 * 5);
			int k = 0;
			for(Character c : chars) {
				if(k++ > randomSize)
					break;
				sb.append(c);
			}
			String txt = sb.toString();
			
			//保存短信内容方便比较
			contentMap.put(txt.hashCode(), txt.hashCode());
			
			CmppDeliverRequestMessage lmsg = createTestReq(txt);
			// 设置每个短信的端口号不同
			lmsg.setDestId("1872" + i);
			// 长短信拆分
			SmsMessage msgcontent = lmsg.getSmsMessage();
			
			if(msgcontent instanceof SmsConcatMessage) {
				((SmsConcatMessage)msgcontent).setSeqNoKey(lmsg.getSrcIdAndDestId());
			}
			List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);

			// 保证同一条长短信，通过同一个tcp连接发送

			for (LongMessageFrame frame : frameList) {
				BaseMessage basemsg = (BaseMessage) lmsg.generateMessage(frame);
				msgs.add(basemsg);
			}
		}
		int totalSplitMsg = msgs.size();
		Collections.shuffle(msgs);	// 随机打乱


		final CountDownLatch startSignal = new CountDownLatch(totalSplitMsg + 1);
		final CountDownLatch stop = new CountDownLatch(totalSplitMsg);
		final AtomicInteger count = new AtomicInteger();

		for (final BaseMessage split : msgs) {
			executor.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					// 启用多线程进行短信合并
					CmppDeliverRequestMessage splitMsg = (CmppDeliverRequestMessage) split;
					final String key = splitMsg.getSrcterminalId() + splitMsg.getDestId();
					// 等待开始信号
					startSignal.await();
					
					SmsMessageHolder hoder = LongMessageFrameHolder.INS.putAndget(null,key, ((LongSMSMessage) split), useredis);
					if (hoder != null) {
						// 只有一个线程能到达这里
						CmppDeliverRequestMessage result = (CmppDeliverRequestMessage) hoder.getMsg();
						result.setMsgContent(hoder.getSmsMessage());


						
						if(contentMap.get(result.getMsgContent().hashCode()) != null) {
							
							// 完成计数器加1
							count.incrementAndGet();
							
							stop.countDown();
						}
						
						return true;
					}
					stop.countDown();
					return false;
				}
			});
			startSignal.countDown();
		}

		startSignal.countDown(); // 合并线程同时启动
		
		long start = System.nanoTime();
		stop.await(60,TimeUnit.SECONDS); // 等待结束

		Assert.assertEquals("合并成功的条数：", TOTLE, count.get());
		long end = System.nanoTime();
		totalTime += (end - start);

		logger.info("testConcurrentLongMessageMerge2 长短信总条数"+ TOTLE+" 总分片数："+totalSplitMsg + " " + (useredis?"Redis":"     ")+ " 合并耗时 : " + totalTime / 1000000 + "ms"+ " 合并速度：" + ((long)totalSplitMsg*1000*1000000)/totalTime+"/s");
	}
}
