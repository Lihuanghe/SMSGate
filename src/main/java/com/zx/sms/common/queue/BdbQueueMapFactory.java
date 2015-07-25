package com.zx.sms.common.queue;

import java.io.File;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;
import com.zx.sms.config.ConfigFileUtil;

public enum BdbQueueMapFactory {
	INS;
	private static final Logger logger = LoggerFactory.getLogger(BdbQueueMapFactory.class);
	private static final ConcurrentHashMap<String, QueueEnvironment> envMap = new ConcurrentHashMap<String, QueueEnvironment>();

	/**
  * 
  */
	private static final ConcurrentHashMap<String, BlockingQueue> queueMap = new ConcurrentHashMap<String, BlockingQueue>();

	public synchronized QueueEnvironment buildBDB(String basename) {

		String pathName = ConfigFileUtil.getGlobalBDBBaseHome() + basename;
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

	/**
	 * 清理log
	 * 
	 * @see QueueEnvironment#cleanLog()
	 * @see com.sleepycat.je.Environment#cleanLog()
	 */
	public synchronized void clearLog(String envName) {
		QueueEnvironment env = envMap.get(envName);
		if (env != null) {
			env.clearLog();
		}
		;
	}

	/**
	 * 清理log <br>
	 * 关闭{@link Environment} <br>
	 * 关闭{@link Database}
	 * 
	 * @see QueueEnvironment#close()
	 * @see com.sleepycat.je.Environment#cleanLog()
	 * @see com.sleepycat.je.Environment#close()
	 */
	public synchronized void close(String envName) {
		QueueEnvironment env = envMap.get(envName);
		if (env != null) {
			env.close();
		}
	}

	public synchronized void closeAll() {
		for (Entry<String, QueueEnvironment> entry : envMap.entrySet()) {
			entry.getValue().close();
		}
	}

	public Database buildQueuemap(QueueEnvironment queueEnvironment, String queueName) {
		return queueEnvironment.buildQueueDB(queueName);
	}

	public static BlockingQueue getQueue(String gateId) {
		BlockingQueue queue = queueMap.get(gateId);
		if (queue == null) {
			synchronized (queueMap) {
				queue = queueMap.get(gateId);
				if (queue == null) {
					queue = BdbQueueMap.build(gateId, "Trans_" + gateId);
					queueMap.put(gateId, queue);
				}
			}

		}
		return queue;
	}

}
