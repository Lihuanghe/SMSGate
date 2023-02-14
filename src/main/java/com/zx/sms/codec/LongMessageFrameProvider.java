package com.zx.sms.codec;

public interface LongMessageFrameProvider {
	LongMessageFrameCache create();
	
	/**
	 * 选取序号最大的为生效的Provider
	 * 框架自带的JVM 缓存，序号为0，业务侧的实现类要大于0 
	 */
	int order();
}
