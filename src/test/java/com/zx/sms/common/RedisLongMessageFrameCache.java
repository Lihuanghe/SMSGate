package com.zx.sms.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.LongMessageFrameCache;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.common.util.FstObjectSerializeUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisLongMessageFrameCache implements LongMessageFrameCache {
	private static final Logger logger = LoggerFactory.getLogger(RedisLongMessageFrameCache.class);
	final static String BitSetPre = "BitSetdfj_";
	final static Long ttl = 2 * 3600L;
	private JedisPool jedispool;
	private String scriptHash ;
	private String userPrefix;
	public RedisLongMessageFrameCache(JedisPool jedispool, String userPrefix) {
		this.jedispool = jedispool;
		this.userPrefix = userPrefix == null?"":userPrefix;
		init();
	}

	private void init() {
		//初始化lua hash
		Jedis jedis = jedispool.getResource();
		try {
			scriptHash = jedis.scriptLoad(Lua_ge_64);
		}finally {
			jedis.close();
		}
	}
	@Override
	public boolean addAndGet(LongSMSMessage msg, String t_key, LongMessageFrame currFrame) {
		String key = userPrefix + t_key;
		// pkTotal ,pkNumber 是byte，可能为负数
		int pkTotal = (int) (currFrame.getPktotal() & 0x0ff);
		int pkNumber = (int) (currFrame.getPknumber() & 0x0ff);
		String bitsetKey = userPrefix + BitSetPre + t_key;
		Jedis jedis = jedispool.getResource();
		try {
			Pipeline pipe = jedis.pipelined();
			// 使用原子方法设置bitset并判断是否接收全部分片
			/*
			 * 相当于下面代码加全局分布锁，使用Lua实现 jedis.setbit(bitsetKey, pkNumber, true); long bitCount  = jedis.bitcount(bitsetKey); return pkTotal == bitCount;
			 */
			//1:先将frame加入Set
			pipe.sadd(key.getBytes(StandardCharsets.UTF_8), FstObjectSerializeUtil.write(currFrame));
			
			//2: 再统计bitset 
			Response<Object> response = pipe.evalsha(scriptHash, Collections.singletonList(bitsetKey), Collections.singletonList(String.valueOf(pkNumber)));
			
			//上面两步顺序不能错
			pipe.expire(key.getBytes(StandardCharsets.UTF_8), ttl);
			pipe.expire(bitsetKey.getBytes(StandardCharsets.UTF_8), ttl);
			pipe.sync();
			Long b_count = (Long) response.get();
			return pkTotal == b_count;

		} catch (Exception e) {
			logger.warn("", e);
		} finally {
			jedis.close();
		}

		return false;
	}

	@Override
	public List<LongMessageFrame> getAndDel(String t_key) {
		String key = userPrefix + t_key;
		String bitsetKey = userPrefix + BitSetPre + t_key;
		Jedis jedis = jedispool.getResource();
		try {
			Pipeline pipe = jedis.pipelined();
			Response<Set<byte[]>> allFrame = pipe.smembers(key.getBytes(StandardCharsets.UTF_8));
			pipe.del(bitsetKey.getBytes(StandardCharsets.UTF_8));
			pipe.del(key.getBytes(StandardCharsets.UTF_8));
			pipe.sync();
			List<LongMessageFrame> frames = new ArrayList<LongMessageFrame>();
			for (byte[] arr : allFrame.get()) {
				try {
					LongMessageFrame f = (LongMessageFrame) FstObjectSerializeUtil.read(arr);
					frames.add(f);
				} catch (Exception e) {
					logger.warn("", e);
				}
			}
			return frames;
		} finally {
			jedis.close();
		}
	}

	private static String Lua_ge_64 = "redis.call('setbit',KEYS[1],ARGV[1],1);return redis.call('bitcount',KEYS[1])";
	private final static AtomicInteger sequenceId = new AtomicInteger(RandomUtils.nextInt());
	@Override
	public Long getUniqueLongMsgId(String cacheKey) {
		Jedis jedis = jedispool.getResource();
		String key = userPrefix + cacheKey;
		
		try {
			Long value = Long.valueOf(sequenceId.incrementAndGet());
			long ret = jedis.setnx(key, value.toString());
			if(1 ==  ret ) {
				jedis.expire(key, ttl);
				return value;
			}else {
				return Long.valueOf(jedis.get(key));
			}
		} finally {
			jedis.close();
		}
	}

	@Override
	public void clearUniqueLongMsgIdCacheKey(String cacheKey) {
		if(cacheKey == null)
			return;
		Jedis jedis = jedispool.getResource();
		try {
			String key = userPrefix + cacheKey;
			jedis.del(key);
		} finally {
			jedis.close();
		}
	}

}
