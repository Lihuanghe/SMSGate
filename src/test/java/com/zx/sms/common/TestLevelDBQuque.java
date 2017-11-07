package com.zx.sms.common;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.Range;
import org.junit.Test;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
public class TestLevelDBQuque {

	@Test 
	public void testLevelDB() throws Exception{
		Options options = new Options();
		options.createIfMissing(true);
		DB db = factory.open(new File("d:\\example"), options);
		try {
		  // Use the db in here....
			long start = System.currentTimeMillis();
			int i = 10000;
			while(i-->0){
			db.put(bytes(UUID.randomUUID().toString()),SerializationUtils.serialize(new CmppActiveTestRequestMessage()));
			}
			System.out.println("testLevelDB");
			System.out.println(System.currentTimeMillis() - start);
		} finally {
		  // Make sure you close the db to shutdown the 
		  // database and avoid resource leaks.
		  db.close();
		}
	}
	
	private byte[] bytes(String s ){
		return s.getBytes();
	}
	
	@Test
	public void teststoreMap(){
		
		 Map<Serializable, Serializable>  map = BDBStoredMapFactoryImpl.INS.buildMap("abc", "abc");
		 // Use the db in here....
			long start = System.currentTimeMillis();
			int i = 10000;
			long l = 0;
			while(i-->0){
			 map.put(l++, new CmppActiveTestRequestMessage());
			}
			System.out.println("teststoreMap");
			System.out.println(System.currentTimeMillis() - start);
	}
}
