package com.zx.sms.common.storedMap;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		String keyName = new StringBuilder().append(storedpath).append(name).toString();
		StoredMap<Serializable, VersionObject> map = storedMaps.get(keyName);
		if (map == null) {
			StoredMap<Serializable, VersionObject> tmpMap = new StoredMap<Serializable, VersionObject>(db, messageKeyBinding, messageValueBinding, true);
			StoredMap<Serializable, VersionObject> old = storedMaps.putIfAbsent(keyName, tmpMap);
			return old == null ? tmpMap : old;
		}
		return map;
	}

	private QueueEnvironment buildBDB(String basename) {
		String pathName;
		basename = basename == null ? "" : basename;
		if (PropertiesUtils.GLOBAL_BDB_BASE_HOME.endsWith("/")) {
			pathName = PropertiesUtils.GLOBAL_BDB_BASE_HOME + basename;
		} else {
			pathName = PropertiesUtils.GLOBAL_BDB_BASE_HOME + "/" + basename;
		}

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
			env = new QueueEnvironment().buildEnvironment(pathName);
			QueueEnvironment oldenv = envMap.putIfAbsent(pathName, env);
			return oldenv == null ? env : oldenv;
		}
		return env;
	}

	private class QueueEnvironment {
		private Environment environment;
		private DatabaseConfig dbConfig;
		private ConcurrentHashMap<String, Database> dbMap = new ConcurrentHashMap<String, Database>();

		public QueueEnvironment buildEnvironment(String pathHome) {

			File home = new File(pathHome);
			// 获取BDB的配置文件

			EnvironmentConfig environmentConfig = new EnvironmentConfig(PropertiesUtils.getJeProperties());
			environmentConfig.setAllowCreate(true);
			environmentConfig.setTransactional(true);
			environment = new Environment(home, environmentConfig);
			dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setTransactional(true);
			cleanLogSchedule();
			return this;
		}

		@SuppressWarnings("unchecked")
		public Database buildDatabase(String queueName) {
			Database queueDB = dbMap.get(queueName);
			if (queueDB == null) {
				queueDB = environment.openDatabase(null, queueName, dbConfig);
				Database olddb = dbMap.putIfAbsent(queueName, queueDB);
				return olddb == null ? queueDB : olddb;
			}
			return queueDB;
		}

		public void clearLog() {
			environment.cleanLog();
		}

		/**
		 * 定时清除BDB的Log
		 * 
		 */
		private void cleanLogSchedule() {
			EventLoopGroupFactory.INS.getBusiWork().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					clearLog();
				}
			}, 60, 60, TimeUnit.SECONDS);
		}

	}

}
