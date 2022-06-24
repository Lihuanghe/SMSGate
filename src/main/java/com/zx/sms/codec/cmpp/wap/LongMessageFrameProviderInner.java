package com.zx.sms.codec.cmpp.wap;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.LongMessageFrameCache;
import com.zx.sms.codec.LongMessageFrameProvider;

public class LongMessageFrameProviderInner implements LongMessageFrameProvider {
	private static final Logger logger = LoggerFactory.getLogger(LongMessageFrameProviderInner.class);
	private static final RemovalListener<String, List<LongMessageFrame>> removealListener = new RemovalListener<String, List<LongMessageFrame>>() {

		@Override
		public void onRemoval(RemovalNotification<String, List<LongMessageFrame>> notification) {
			RemovalCause cause = notification.getCause();
			List<LongMessageFrame> h = notification.getValue();
			switch (cause) {
			case EXPIRED:
			case SIZE:
			case COLLECTED:
				logger.error("Long Message Lost cause by {}. {}", cause,longMessageFrameListToString(h));
			default:
				return;
			}
		}
	};
	
	/**
	 * 以服务号码+帧唯一码为key
	 * 注意：这里使用的jvm缓存保证长短信的分片。如果是集群部署，从网关过来的长短信会随机发送到不同的主机，需要使用集群缓存
	 * ，如redis,memcached来保存长短信分片。 由于可能有短信分片丢失，造成一直不能组装完成，为防止内存泄漏，这里要使用支持过期失效的缓存。
	 */
	private static Cache<String, List<LongMessageFrame>> cache = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.HOURS).removalListener(removealListener).build();
	private static ConcurrentMap<String, List<LongMessageFrame>> map = cache.asMap();
	@Override
	public LongMessageFrameCache create() {
		return new LongMessageFrameCacheInner();
	}

	@Override
	public int order() {
		//内部实现，序号为0
		return 0;
	}

	private static String longMessageFrameListToString(List<LongMessageFrame> list) {
		StringBuffer sb = new StringBuffer();
		for(LongMessageFrame m : list) {
			sb.append(m.toString()).append("\n");
		}
		return sb.toString();
	}
	
	private class LongMessageFrameCacheInner implements LongMessageFrameCache{

		@Override
		public List<LongMessageFrame> get(String key) {
			return map.get(key);
		}

		@Override
		public void remove(String key) {
			map.remove(key);
		}

		@Override
		public void set(LongSMSMessage msg,String key, List<LongMessageFrame> list,LongMessageFrame curr) {
			map.put(key, list);
		}
	}
}
