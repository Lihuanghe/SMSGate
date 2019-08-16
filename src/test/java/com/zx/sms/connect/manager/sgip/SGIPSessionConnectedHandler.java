package com.zx.sms.connect.manager.sgip;

import java.util.concurrent.atomic.AtomicInteger;

import com.zx.sms.BaseMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;

public class SGIPSessionConnectedHandler extends SessionConnectedHandler {
	public SGIPSessionConnectedHandler(int t){
		totleCnt = new AtomicInteger(t);
	}
	
	@Override
	protected BaseMessage createTestReq(String content) {
		final SgipEndpointEntity finalentity = (SgipEndpointEntity)getEndpointEntity();
		String sms = "【费用提醒】尊敬的客户，截止2018年2月1日17时";
		if (finalentity instanceof ServerEndpoint) {
			SgipDeliverRequestMessage sgipmsg = new SgipDeliverRequestMessage();
			sgipmsg.setUsernumber("13800138000");
			sgipmsg.setSpnumber("10086");
			sgipmsg.setMsgContent(sms);
			return sgipmsg;
		} else {
			SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage();
			requestMessage.setSpnumber("10086");
			requestMessage.setUsernumber("13800138000");
			requestMessage.setMsgContent(sms);
			requestMessage.setReportflag((short)0);
			return requestMessage;
		}
	}

}
