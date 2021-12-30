package com.zx.sms.session.sgip;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.sgip12.msg.SgipDeliverResponseMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitResponseMessage;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipEndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.util.concurrent.Promise;

public class SgipSessionStateManager extends AbstractSessionStateManager<Long, Message> {
	private static final Logger logger = LoggerFactory.getLogger(SgipSessionStateManager.class);
	public SgipSessionStateManager(EndpointEntity entity, ConcurrentMap<Long, VersionObject<Message>> storeMap, boolean preSend) {
		super(entity, storeMap, preSend);
	}

	@Override
	protected Long getSequenceId(Message msg) {
		long seq = msg.getHeader().getSequenceId();
		long time = msg.getTimestamp();
		long node = msg.getHeader().getNodeId();
		return Long.valueOf(node << 32 | (seq & 0x0ffffffff));
	}

	@Override
	protected boolean needSendAgainByResponse(Message req, Message res) {
		short result = SmppConstants.STATUS_OK;
		if(res instanceof SgipSubmitResponseMessage) {
			result = ((SgipSubmitResponseMessage) res).getResult();
		}else if(res instanceof SgipDeliverResponseMessage) {
			result = ((SgipDeliverResponseMessage) res).getResult();
		}
		if (result != SmppConstants.STATUS_OK && result != SmppConstants.STATUS_THROTTLED) {
			logger.error("Receive Err Response result: {} . Req: {} ,Resp:{}",result, req, res);
		}
		
		//Sgip协议 与 Smpp协议的超速错误码相同
		return result ==  SmppConstants.STATUS_THROTTLED;
	}

	@Override
	protected boolean closeWhenRetryFailed(Message req) {
		return getEntity().isCloseWhenRetryFailed();
	}
	
	//同步调用时，要设置sgip的NodeId
	public Promise<Message> writeMessagesync(Message msg){
		SgipEndpointEntity entity = (SgipEndpointEntity)getEntity();
		if(msg instanceof DefaultMessage && entity instanceof SgipEndpointEntity) {
			DefaultMessage message = (DefaultMessage)msg;
			if(message.isRequest() && entity.getNodeId()!=0 && message.getHeader().getNodeId()==0) {
				message.getHeader().setNodeId(entity.getNodeId());
			}
		}
		return super.writeMessagesync(msg);
	}

}
