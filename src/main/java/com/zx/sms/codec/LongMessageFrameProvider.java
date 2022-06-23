package com.zx.sms.codec;

public interface LongMessageFrameProvider {
	LongMessageFrameCache create();
	
	/**
	 * 选取序号最大的为生效的Provider
	 */
	int order();
}
