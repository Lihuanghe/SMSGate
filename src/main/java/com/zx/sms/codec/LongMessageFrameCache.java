package com.zx.sms.codec;

import java.util.List;

import com.zx.sms.codec.cmpp.wap.LongMessageFrame;

public interface LongMessageFrameCache {

	/**
	 * 获取全部的分片
	 */
	List<LongMessageFrame> get(String key);
	
	/**
	 * 删除全部的分片
	 */
	void remove(String key);
	
	/**
	 * 保存分片
	 * @param  全部的分片，包含当前分片
	 * @param  当前收到的分片
	 */
	void set(String key,List<LongMessageFrame> list, LongMessageFrame currFrame);
	
}
