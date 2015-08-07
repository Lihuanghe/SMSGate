package com.zx.sms.common.storedMap;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.queue.BdbQueueMap;
import com.zx.sms.config.PropertiesUtils;
import com.zx.sms.connect.manager.EventLoopGroupFactory;

public enum BDBStoredMapFactoryImpl implements StoredMapFactory<Long, Message> {
	INS;
	private static final Logger logger = LoggerFactory.getLogger(BDBStoredMapFactoryImpl.class);
	private final ConcurrentHashMap<String, QueueEnvironment> envMap = new ConcurrentHashMap<String, QueueEnvironment>();

	private final ConcurrentHashMap<String, StoredMap<Long, Message>> storedMaps = new ConcurrentHashMap<String, StoredMap<Long, Message>>();
	private final ConcurrentHashMap<String, StoredSortedMap<Long, Message>> sortedstoredMap = new ConcurrentHashMap<String, StoredSortedMap<Long, Message>>();
	private final ConcurrentHashMap<String, BlockingQueue<Message>> queueMap = new ConcurrentHashMap<String, BlockingQueue<Message>>();

	@Override
	public synchronized Map<Long, Message> buildMap(String storedpath, String name) {
		QueueEnvironment env = buildBDB(storedpath);
		SerialBinding<Long> messageKeyBinding = new SerialBinding<Long>(env.getStoredClassCatalog(), Long.class);
		SerialBinding<Message> messageValueBinding = new SerialBinding<Message>(env.getStoredClassCatalog(), Message.class);
		Database db = env.buildDatabase(name);

		String keyName = new StringBuilder().append(storedpath).append(name).toString();
		StoredMap<Long, Message> map = storedMaps.get(keyName);
		if (map == null) {
			StoredMap<Long, Message> tmpMap = new StoredMap<Long, Message>(db, messageKeyBinding, messageValueBinding, true);
			storedMaps.put(keyName, tmpMap);
			return tmpMap;
		}
		return map;
	}
	

	@Override
	public BlockingQueue<Message> getQueue(String storedpath, String name) {
		String keyName = new StringBuilder().append(storedpath).append(name).toString();
		BlockingQueue<Message> queue = queueMap.get(keyName);
		if (queue == null) {
			synchronized (queueMap) {
				queue = queueMap.get(keyName);
				if (queue == null) {
					StoredSortedMap<Long, Message> sortedStoredmap = buildStoredSortedMap(storedpath, "Trans_" + name);
					BlockingQueue<Message> newqueue = new BdbQueueMap<Message>(sortedStoredmap);
					queueMap.put(keyName, newqueue);
					return newqueue;
				}
			}
		}
		return queue;
	}

	private StoredSortedMap<Long, Message> buildStoredSortedMap(String storedpath, String name) {
		QueueEnvironment env = buildBDB(storedpath);
		SerialBinding<Long> messageKeyBinding = new SerialBinding<Long>(env.getStoredClassCatalog(), Long.class);
		SerialBinding<Message> messageValueBinding = new SerialBinding<Message>(env.getStoredClassCatalog(), Message.class);
		Database db = env.buildDatabase(name);
		String keyName = new StringBuilder().append(storedpath).append(name).toString();

		StoredSortedMap<Long, Message> soredMap = sortedstoredMap.get(keyName);

		if (soredMap == null) {
			soredMap = new StoredSortedMap<Long, Message>(db, (EntryBinding<Long>) messageKeyBinding, (EntryBinding<Message>) messageValueBinding, true);

			sortedstoredMap.put(keyName, soredMap);
			return soredMap;
		}

		return soredMap;
	}

	private QueueEnvironment buildBDB(String basename) {
		String pathName;
		basename = basename==null?"":basename;
		
		if(GlobalConstance.globalBDBBaseHome.endsWith("/")){
			 pathName = GlobalConstance.globalBDBBaseHome + basename;
		}else{
			 pathName = GlobalConstance.globalBDBBaseHome +"/"+ basename;
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
			env = new QueueEnvironment().buildEnvironment(pathName).buildStoredClassCatalog();
			envMap.put(pathName, env);
			return env;
		}
		return env;
	}

	private class QueueEnvironment {
		private Environment environment;
		private DatabaseConfig dbConfig;
		private Database classCatalogDB;
		private StoredClassCatalog storedClassCatalog;
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

		public QueueEnvironment buildStoredClassCatalog() {
			return buildStoredClassCatalog("classCatalog");
		}

		public QueueEnvironment buildStoredClassCatalog(String Name) {
			classCatalogDB = environment.openDatabase(null, Name, dbConfig);
			storedClassCatalog = new StoredClassCatalog(classCatalogDB);
			return this;
		}

		@SuppressWarnings("unchecked")
		public Database buildDatabase(String queueName) {
			Database queueDB = dbMap.get(queueName);
			if (queueDB == null) {
				queueDB = environment.openDatabase(null, queueName, dbConfig);
				dbMap.put(queueName, queueDB);
			}
			return queueDB;
		}

		public void clearLog() {
			environment.cleanLog();
		}

		public void close() {
			environment.cleanLog();
			classCatalogDB.close();
			closeAllQueue();
			environment.close();
		}

		private synchronized void closeAllQueue() {
			for (Entry<String, Database> entry : dbMap.entrySet()) {
				entry.getValue().close();
			}
		}

		public StoredClassCatalog getStoredClassCatalog() {
			return storedClassCatalog;
		}

		private Properties loadFrompropertiesFile(File file) {
			Properties tmpProperties = new Properties();
			InputStream in = null;
			try {
				in = FileUtils.openInputStream(file);
				tmpProperties.load(in);
			} catch (Exception ex) {
				logger.error("load je.properties error.", ex);
			} finally {
				IOUtils.closeQuietly(in);
			}
			return tmpProperties;
		}

		/**
		 * 定时清除BDB的Log
		 * 
		 */
		private void cleanLogSchedule() {
			EventLoopGroupFactory.INS.getMsgResend().scheduleWithFixedDelay(new Runnable() {

				@Override
				public void run() {
					clearLog();
				}
			}, 60, 60, TimeUnit.SECONDS);
		}

	}

}
