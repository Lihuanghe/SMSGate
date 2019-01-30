package com.zx.sms.common;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.junit.Test;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.common.storedMap.VersionObject;
public class TestLevelDBQuque {

	@Test 
	public void testLevelDB() throws Exception{
		Options options = new Options();
		options.createIfMissing(true);
		final DB db = factory.open(new File("d:\\example"), options);
		Thread d = new Thread(){
			@Override
			 public void run() {
				while(true){
					ReadOptions opt = new ReadOptions();
					opt.snapshot(db.getSnapshot());
					int cnt =0;
					for(DBIterator iter = db.iterator(opt);iter.hasNext();){
						Map.Entry<byte[], byte[]> entry = iter.next();
						cnt++;
					}
					System.out.println("size:"+cnt);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		};
		d.start();
		
		try {
		  // Use the db in here....
			long start = System.currentTimeMillis();
			int i = 10000;
			while(i-->0){
				db.put(bytes(UUID.randomUUID().toString()),SerializationUtils.serialize(new CmppActiveTestRequestMessage()));
				if(i%100 == 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
			System.out.println("testLevelDB");
			System.out.println(System.currentTimeMillis() - start);
			
			int cnt = 0;
			for(DBIterator iter = db.iterator();iter.hasNext();){
				Map.Entry<byte[], byte[]> entry = iter.next();
				cnt++;
			}
			System.out.println("size:"+cnt);
			
		} finally {
		  // Make sure you close the db to shutdown the 
		  // database and avoid resource leaks.
		  db.close();
		}
	}
	
	private byte[] bytes(String s ){
		return s.getBytes();
	}
	

	public void teststoreMap(){
		
		 Map<Serializable, VersionObject>  map = BDBStoredMapFactoryImpl.INS.buildMap("abc", "abc");
		 // Use the db in here....
			long start = System.currentTimeMillis();
			int i = 10000;
			long l = 0;
			while(i-->0){
			 map.put(l++,new VersionObject(new CmppActiveTestRequestMessage()));
			}
			System.out.println("teststoreMap");
			System.out.println(System.currentTimeMillis() - start);
	}
}
