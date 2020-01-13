package com.zx.sms.connect.manager.smpp;

import java.util.concurrent.atomic.AtomicInteger;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.DeliverSm;
import com.zx.sms.codec.smpp.msg.DeliverSmReceipt;
import com.zx.sms.codec.smpp.msg.DeliverSmResp;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;

import io.netty.channel.ChannelHandlerContext;

public class SMPPSessionConnectedHandler extends SessionConnectedHandler {
	public SMPPSessionConnectedHandler(int t) {
		totleCnt = new AtomicInteger(t);
	}

	@Override
	protected BaseMessage createTestReq(String str) {
		final EndpointEntity finalentity = getEndpointEntity();
		String content = "PS：第三种方法会在集群中传送很多无用的数据，无形中增加了网络的带宽。但是这也是没有办法的事情。以上代码都没经过测试，大体是这个意思PSS：如果谁有更好的方法，希望能和他说一声";
		if (finalentity instanceof ServerEndpoint) {
			DeliverSm pdu = new DeliverSm();
	        pdu.setSourceAddress(new Address((byte)0,(byte)0,"13800138000"));
	        pdu.setDestAddress(new Address((byte)0,(byte)0,"10086"));
	        pdu.setSmsMsg(content);
			return pdu;
		} else {
			SubmitSm pdu = new SubmitSm();
	        pdu.setSourceAddress(new Address((byte)0,(byte)0,"10086"));
	        pdu.setDestAddress(new Address((byte)0,(byte)0,"13800138000"));
	        pdu.setSmsMsg(content);
			return pdu;
		}
	}
	
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof DeliverSmReceipt) {
			DeliverSmReceipt e = (DeliverSmReceipt) msg;
			
			DeliverSmResp res =e.createResponse();
			res.setMessageId(String.valueOf(System.currentTimeMillis()));
			 ctx.writeAndFlush(res);

		} else {
			ctx.fireChannelRead(msg);
		}
	}


}
