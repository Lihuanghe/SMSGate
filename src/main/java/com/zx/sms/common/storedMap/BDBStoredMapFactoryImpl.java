package com.zx.sms.common.storedMap;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.connect.manager.EventLoopGroupFactory;

public enum BDBStoredMapFactoryImpl implements StoredMapFactory<Serializable, VersionObject> {
	INS;

	private static final Logger logger = LoggerFactory.getLogger(BDBStoredMapFactoryImpl.class);
	private final ConcurrentHashMap<String, QueueEnvironment> envMap = new ConcurrentHashMap<String, QueueEnvironment>();
	private final ConcurrentHashMap<String, StoredMap<Serializable, VersionObject>> storedMaps = new ConcurrentHashMap<String, StoredMap<Serializable, VersionObject>>();

	/**
	 * VersionObject 在保存消息对象信息时，同时保存时间戳。这样就可以区分哪些是历史信息。
	 */
	@Override
	public synchronized ConcurrentMap<Serializable, VersionObject> buildMap(String storedpath, String name) {
		QueueEnvironment env = buildBDB(storedpath);
		FstSerialBinding<Serializable> messageKeyBinding = new FstSerialBinding<Serializable>();
		FstSerialBinding<VersionObject> messageValueBinding = new FstSerialBinding<VersionObject>();
		Database db = env.buildDatabase(name);
		String keyName = buildStoredMapKey(storedpath,name);
		StoredMap<Serializable, VersionObject> map = storedMaps.get(keyName);
		if (map == null) {
			StoredMap<Serializable, VersionObject> tmpMap = new StoredMap<Serializable, VersionObject>(db, messageKeyBinding, messageValueBinding, true);
			StoredMap<Serializable, VersionObject> old = storedMaps.putIfAbsent(keyName, tmpMap);
			return old == null ? tmpMap : old;
		}
		return map;
	}

	private String buildStoredMapKey(String storedpath, String name) {
		String keyName = new StringBuilder().append(storedpath).append(name).toString();
		return keyName;
	}
	
	public synchronized void close(String storedpath, String name) {
		String keyName = buildStoredMapKey(storedpath,name);
		storedMaps.remove(keyName);
		
		String  pathName = buildBDBPath(storedpath);
		QueueEnvironment env = envMap.remove(pathName);
		if(env!=null)
			env.close(name);

	}
	
	private String buildBDBPath(String basename) {
		basename = basename == null ? "" : basename;
		if (PropertiesUtils.GLOBAL_BDB_BASE_HOME.endsWith("/")) {
			return PropertiesUtils.GLOBAL_BDB_BASE_HOME + basename;
		} else {
			return PropertiesUtils.GLOBAL_BDB_BASE_HOME + "/" + basename;
		}
	}
	
	private QueueEnvironment buildBDB(String basename) {
		String pathName = buildBDBPath(basename);

		File file = new File(pathName);
		if (!file.exists()) {
			boolean succ = file.mkdirs();

			if (!succ) {
				logger.error("create Directory {} failed. ", pathName);
				return null;
			}
		}

		if (!file.isDirectory()) {
			logger.error("file  {} is not a Directory ", pathName);
			return null;
		}

		QueueEnvironment env = envMap.get(pathName);

		if (env == null) {
			logger.info("init BDBPath : {}", pathName);
			env = new QueueEnvironment(pathName);
			QueueEnvironment oldenv = envMap.putIfAbsent(pathName, env);
			if(oldenv == null) {
				env.startCleanLogSchedule();
				return env;
			}else {
				return oldenv;
			}
			
		}
		return env;
	}

	private class QueueEnvironment {
		private Environment environment;
		private DatabaseConfig dbConfig;
		private ConcurrentHashMap<String, Database> dbMap = new ConcurrentHashMap<String, Database>();
		private ListenableScheduledFuture  logScheduleFuture ;
		private String pathHome;
		public QueueEnvironment(String pathHome) {
			this.pathHome = pathHome;
			File home = new File(pathHome);
			// 获取BDB的配置文件

			//Adler32  makeChecksum
			//在linux上java 原生的算法有bug.
			System.setProperty("je.disable.java.adler32", "false");
			
			EnvironmentConfig environmentConfig = new EnvironmentConfig(PropertiesUtils.getJeProperties());
			environmentConfig.setAllowCreate(true);
			environmentConfig.setTransactional(true);
			environment = new Environment(home, environmentConfig);
			dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setTransactional(true);
		}

		@SuppressWarnings("unchecked")
		public Database buildDatabase(String queueName) {
			Database queueDB = dbMap.get(queueName);
			if (queueDB == null) {
				queueDB = environment.openDatabase(null, queueName, dbConfig);

			
				Database olddb = dbMap.putIfAbsent(queueName, queueDB);
				logger.info("openDatabase BDBPath {} . db:{}  ",pathHome,queueName);
				Database retdb = olddb == null ? queueDB : olddb;
				
				return retdb;
			}
			return queueDB;
		}
		
		public void close(String queueName) {
			logger.info("close BDBPath {} .dbName:{} ",pathHome, queueName);
			logScheduleFuture.cancel(true);
//			clearLog();
			Database queueDB = dbMap.remove(queueName);
			if(queueDB != null)
				queueDB.close();
			
			environment.close();
			
		}

		public void clearLog() {
			logger.debug("clearLog {} ",pathHome );
			environment.cleanLog();
		}

		/**
		 * 定时清除BDB的Log
		 * 
		 */
		public void startCleanLogSchedule() {
			logScheduleFuture = EventLoopGroupFactory.INS.getBusiWork().scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					clearLog();
				}
			}, 60, 60, TimeUnit.SECONDS);
		}
	}

}
