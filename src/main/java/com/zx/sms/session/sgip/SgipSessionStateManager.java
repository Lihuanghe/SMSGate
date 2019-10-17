package com.zx.sms.session.sgip;

import java.util.concurrent.ConcurrentMap;

import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.VersionObject;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipEndpointEntity;
import com.zx.sms.session.AbstractSessionStateManager;

import io.netty.util.concurrent.Promise;

public class SgipSessionStateManager extends AbstractSessionStateManager<Long, Message> {

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
		return false;
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
