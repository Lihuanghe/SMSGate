package com.zx.sms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.LongMessageFrameCache;
import com.zx.sms.codec.LongMessageFrameProvider;

public class RedisLongMessageFrameProvider implements LongMessageFrameProvider {
	private static final Logger logger = LoggerFactory.getLogger(RedisLongMessageFrameProvider.class);

	@Override
	public LongMessageFrameCache create() {
			return new RedisLongMessageFrameCache(RedisClientBuilder.createJedisPool(), "Test_");
	}

	@Override
	public int order() {
		return 1;
	}

}
