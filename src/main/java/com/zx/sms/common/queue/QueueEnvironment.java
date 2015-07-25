package com.zx.sms.common.queue;

import java.io.File;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.zx.sms.config.ConfigFileUtil;
import com.zx.sms.connect.manager.EventLoopGroupFactory;

public class QueueEnvironment {
	private Environment environment;
	private DatabaseConfig dbConfig;
	private Database classCatalogDB;
	private StoredClassCatalog storedClassCatalog;
	private ConcurrentHashMap<String ,Database>  queueDBMap= new ConcurrentHashMap<String ,Database>();
	private static final Logger logger = LoggerFactory.getLogger(QueueEnvironment.class);
	
	
	
	public QueueEnvironment buildEnvironment(String pathHome) {
		
		File home = new File(pathHome);
		//获取BDB的配置文件
		File propertiesFile = ConfigFileUtil.getJeproperties();
		EnvironmentConfig environmentConfig = new EnvironmentConfig(loadFrompropertiesFile(propertiesFile));
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
	public synchronized Database buildQueueDB(String queueName) {
		Database queueDB = queueDBMap.get(queueName);
		if(queueDB==null){
			 queueDB = environment.openDatabase(null, queueName, dbConfig);
			queueDBMap.put(queueName, queueDB);
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
	private void closeAllQueue()
	{
		for(Entry<String,Database> entry :queueDBMap.entrySet() ){
			entry.getValue().close();
		}
	}
	public StoredClassCatalog getStoredClassCatalog() {
		return storedClassCatalog;
	}
	
	private Properties loadFrompropertiesFile(File file)
	{
		Properties tmpProperties = new Properties();
		InputStream in = null;
		try{
			in =  FileUtils.openInputStream(file);
			tmpProperties.load(in);
		}
		catch(Exception ex){
			logger.error("load je.properties error.",ex);
		}
		finally{
			IOUtils.closeQuietly(in);
		}
		return tmpProperties;
	}
	
	/**
	 *定时清除BDB的Log 
	 * 
	 */
	private void cleanLogSchedule()
	{
		EventLoopGroupFactory.INS.getMsgResend().scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				clearLog();
			}}, 60, 60, TimeUnit.SECONDS);
	}
	
}