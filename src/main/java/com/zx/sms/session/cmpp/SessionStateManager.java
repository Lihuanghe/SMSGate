package com.zx.sms.session.cmpp;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppActiveTestRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

/**
 * @author Lihuanghe(18852780@qq.com) 消息发送窗口拜你控制和消息重发 ，消息持久化
 */
public class SessionStateManager extends AbstractSessionStateManager<Integer, Message> {
	private static final Logger logger = LoggerFactory.getLogger(SessionStateManager.class);

	public SessionStateManager(EndpointEntity entity, ConcurrentMap<String, VersionObject<Message>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Integer getSequenceId(Message msg) {
		return msg.getHeader().getSequenceId();
	}

	@Override
	protected boolean needSendAgainByResponse(Message req, Message res) {
		long result = 0;
		if (res instanceof CmppSubmitResponseMessage) {
			CmppSubmitResponseMessage submitResp = (CmppSubmitResponseMessage) res;
			result = submitResp.getResult();
		} else if (res instanceof CmppDeliverResponseMessage) {
			CmppDeliverResponseMessage deliverResp = (CmppDeliverResponseMessage) res;
			result = deliverResp.getResult();
		}
		
		if ((result != 0L) && (result != 8L)) {
			logger.error("Entity {} Receive Err Response result: {} . Req: {} ,Resp:{}",getEntity().getId(),result, req, res);
		}

		return result == 8L;
	}
	
	protected boolean closeWhenRetryFailed(Message req) {
		if(req instanceof CmppActiveTestRequestMessage) {
			return true;
		}
		return getEntity().isCloseWhenRetryFailed();
	};

}
