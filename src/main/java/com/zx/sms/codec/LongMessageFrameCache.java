package com.zx.sms.codec;

import java.util.List;

import com.zx.sms.LongSMSMessage;
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
	 * @param msg
	 * 	当前收到的分片的消息对象
	 * @param  list  
	 * 全部的分片，包含当前分片
	 * @param  currFrame 
	 * 当前收到的分片
	 */
	void set(LongSMSMessage msg,String key,List<LongMessageFrame> list, LongMessageFrame currFrame);
	
}
