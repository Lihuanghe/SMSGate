package com.zx.sms.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class TestRedis {
	private static final Logger logger = LoggerFactory.getLogger(TestRedis.class);

	@Test
	public void test() {
		if (!LongMessageFrameHolder.hasClusterLongMessageFrameProvider)
			return;

		JedisPool pool = RedisClientBuilder.createJedisPool();

		Jedis jedis = pool.getResource();
		try {
			String key = "ABC" + RandomUtils.nextInt();
			jedis.set(key, "9");
			jedis.incr(key);
			logger.info("get- key: {}:{}", key, jedis.get(key));
			Assert.assertEquals(10, Long.parseLong(jedis.get(key)));
			jedis.del(key);
			
			long ll = bitsetAndCount(jedis, key, 97);
			ll = bitsetAndCount(jedis, key, 98);
			ll = bitsetAndCount(jedis, key, 98);
			ll = bitsetAndCount(jedis, key, 96);
			System.out.println(ll);
			Assert.assertEquals(3, ll);
		} finally {
			jedis.close();
		}
	}

	private static String Lua_ge_64 = "redis.call('setbit',KEYS[1],ARGV[1],1) \n redis.call('expire',KEYS[1],ARGV[2]) \n return redis.call('bitcount',KEYS[1])";

	private long bitsetAndCount(Jedis jedis, String bitsetKey, int pkNumber) {
		List<String> params = new ArrayList<String>();
		params.add(String.valueOf(pkNumber));
		params.add(String.valueOf(7200));
		Long b_count = (Long) jedis.eval(Lua_ge_64, Collections.singletonList(bitsetKey), params);
		return b_count.longValue();
	}
}
