package com.zx.sms.common.storedMap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.zx.sms.codec.cmpp.msg.Message;

public interface StoredMapFactory<K,T> {
	
	/**
	 * @param storedpath
	 * 数据文件保存的路径
	 * @param name 
	 * Map的名字
	 */
	Map<K,T> buildMap(String storedpath,String name);
	
	BlockingQueue<Message> getQueue(String storedpath,String name);
	
	

}
