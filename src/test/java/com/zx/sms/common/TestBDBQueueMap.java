package com.zx.sms.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;


import java.nio.ByteOrder;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import com.zx.sms.common.util.CachedMillisecondClock;

public class TestBDBQueueMap {

	@Test
	public void testA() throws InterruptedException
	{
//		BlockingQueue<Message> queue = BDBStoredMapFactoryImpl.INS.getQueue("testA","testQ");
//		
//		queue.clear();
//		System.out.println(((byte)0x2 << 8 ) | (byte)0x2);
//		String prefix = (new StringBuilder()).append("\u3010\u7B2C").append(1).append("/").append(3).append("\u9875\u3011")
//				.toString();
//		System.out.println(prefix);
		ByteBuffer bf = ByteBuffer.allocate(10);
		ByteBuf nettybf = Unpooled.buffer();
		System.out.println(ByteOrder.nativeOrder());
		System.out.println(nettybf.order());
		System.out.println(DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm"));
	}
}
