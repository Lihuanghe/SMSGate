package com.zx.sms.session.cmpp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

/**
 * @author Lihuanghe(18852780@qq.com) 消息发送窗口拜你控制和消息重发 ，消息持久化
 */
public class SessionStateManager extends AbstractSessionStateManager<Long,Message> {
	private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);
	
	public SessionStateManager(EndpointEntity entity, Map<Long, Message> storeMap, Map<Long, Message> preSend) {
		super(entity, storeMap, preSend);
	}
	@Override
	protected Long getSequenceId(Message msg) {
			return msg.getHeader().getSequenceId();
	}
	@Override
	protected boolean checkTerminateLife(Object msg) {
		
		return true;
	}


}
