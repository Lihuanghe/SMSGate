package com.zx.sms.common;

import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.queue.BdbQueueMap;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;

public class TestBDBQueueMap {

	@Test
	public void testA() throws InterruptedException
	{
//		BlockingQueue<Message> queue = BDBStoredMapFactoryImpl.INS.getQueue("testA","testQ");
//		
//		queue.clear();
		System.out.println(((byte)0x2 << 8 ) | (byte)0x2);
		String prefix = (new StringBuilder()).append("\u3010\u7B2C").append(1).append("/").append(3).append("\u9875\u3011")
				.toString();
		System.out.println(prefix);
	}
}
