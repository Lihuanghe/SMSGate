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
		BlockingQueue<Message> queue = BDBStoredMapFactoryImpl.INS.getQueue("testA","testQ");
		
		queue.clear();
	}
}
