package com.zx.sms.common;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisClientBuilder {
	
	public static JedisPool createJedisPool() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(10);
		jedisPoolConfig.setMaxIdle(10);
		String host = StringUtils.isBlank(System.getenv("REDIS_HOST")) ? System.getProperty("RedisHost"):System.getenv("REDIS_HOST");
		String port = StringUtils.isBlank(System.getenv("REDIS_PORT")) ? System.getProperty("RedisPort"):System.getenv("REDIS_PORT");
		host = StringUtils.isBlank(host)?"127.0.0.1":host;
		port = StringUtils.isBlank(port)?"6379":port;
		JedisPool pool = new JedisPool(jedisPoolConfig, host, Integer.parseInt(port));
		return pool;
	}

}
