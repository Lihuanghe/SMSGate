package com.zx.sms.common;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;

import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.common.storedMap.VersionObject;

import org.junit.Assert;

public class TestBDBQueueMap {

	@Test
	public void testA() throws InterruptedException
	{
		ConcurrentMap<Serializable, VersionObject> map = BDBStoredMapFactoryImpl.INS.buildMap("testA","testQ");
		
		map.put("121", new VersionObject("1"));
		
		BDBStoredMapFactoryImpl.INS.close("testA","testQ");
		
		map = BDBStoredMapFactoryImpl.INS.buildMap("testA","testQ");
		
		map.put("1212", new VersionObject("2"));
		BDBStoredMapFactoryImpl.INS.close("testA","testQ");
		BDBStoredMapFactoryImpl.INS.close("testA","testQ");
		map = BDBStoredMapFactoryImpl.INS.buildMap("testA","testQ");
		for(Entry<Serializable, VersionObject> entry : map.entrySet()) {
			System.out.println(entry.getKey()+"|"+entry.getValue().getObj());
		}

		Assert.assertEquals("2", map.get("1212").getObj());
	}
}
