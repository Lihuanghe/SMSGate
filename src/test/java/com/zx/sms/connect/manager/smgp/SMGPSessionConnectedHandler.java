package com.zx.sms.connect.manager.smgp;

import java.util.concurrent.atomic.AtomicInteger;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;

import io.netty.channel.ChannelHandlerContext;

public class SMGPSessionConnectedHandler extends SessionConnectedHandler {
	public SMGPSessionConnectedHandler(int t) {
		totleCnt = new AtomicInteger(t);
	}
	
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
		if(msg instanceof SMGPDeliverMessage) {
			SMGPDeliverMessage deliver = (SMGPDeliverMessage)msg;
			SMGPDeliverRespMessage deliResponse = new SMGPDeliverRespMessage();
			deliResponse.setSequenceNo(deliver.getSequenceNo());
			deliResponse.setMsgId(deliver.getMsgId());
			ctx.writeAndFlush(deliResponse);
		}
	}

	@Override
	protected BaseMessage createTestReq(String content) {
		final EndpointEntity finalentity = getEndpointEntity();

		if (finalentity instanceof ServerEndpoint) {
			SMGPDeliverMessage pdu = new SMGPDeliverMessage();
			pdu.setDestTermId("10000");
			pdu.setMsgContent(content);
			pdu.setSrcTermId("13800138000");
			return pdu;
		} else {
			SMGPSubmitMessage pdu = new SMGPSubmitMessage();
			pdu.setSrcTermId("10000");
	        pdu.setDestTermIdArray("13800138000");
	        pdu.setMsgContent(content);
	        pdu.setNeedReport(true);
			return pdu;
		}
	}

}
