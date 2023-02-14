package com.zx.sms;

import java.util.List;

import com.chinamobile.cmos.sms.SmsMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.UniqueLongMsgId;

public interface LongSMSMessage<T> {
	 LongMessageFrame generateFrame();

	 T generateMessage(LongMessageFrame frame) throws Exception;

	 SmsMessage getSmsMessage();

	 boolean isReport();

	/**
	 * 对于长短信，UniqueLongMsgId 相同的表示这些短信分片对应于同一个长短信。<br/>
	 * 这个值用于网关实现转发短信时，关联接收的短短信分片与合并后的长短信对应关系，方便状态报告处理。<br/>
	 * 由：手机号 + 端口号 + 短短信分片的frameKey组成
	 */
	 UniqueLongMsgId getUniqueLongMsgId();
	
	 void setUniqueLongMsgId(UniqueLongMsgId id);

	// 下面两个方法用来保存合并短信前各个片断对应的消息
	 List<T> getFragments();

	 void addFragment(T fragment);
	
	 boolean needHandleLongMessage();
	
	 String getSrcIdAndDestId();
}
