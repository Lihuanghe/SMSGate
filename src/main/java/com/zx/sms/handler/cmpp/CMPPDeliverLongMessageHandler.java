package com.zx.sms.handler.cmpp;

import io.netty.channel.ChannelHandlerContext;

import org.marre.sms.SmsMessage;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.AbstractLongMessageHandler;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

public class CMPPDeliverLongMessageHandler extends AbstractLongMessageHandler<CmppDeliverRequestMessage> {

	@Override
	protected void response(ChannelHandlerContext ctx, CmppDeliverRequestMessage msg) {
		
		//短信片断未接收完全，直接给网关回复resp，等待其它片断
		CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(msg.getHeader());
		responseMessage.setMsgId(msg.getMsgId());
		responseMessage.setResult(0);
		ctx.channel().writeAndFlush(responseMessage);
	}

	@Override
	protected boolean needHandleLongMessage(CmppDeliverRequestMessage msg) {
	
		return !msg.isReport();
	}

	@Override
	protected LongMessageFrame generateFrame(CmppDeliverRequestMessage msg) {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(msg.getTppid());
		frame.setTpudhi(msg.getTpudhi());
		frame.setMsgfmt(msg.getMsgfmt());
		frame.setMsgContentBytes(msg.getMsgContentBytes());
		frame.setMsgLength((short)msg.getMsgLength());
		return frame;
	}

	@Override
	protected String generateFrameKey(CmppDeliverRequestMessage msg) {
		return msg.getSrcterminalId();
	}

	@Override
	protected CmppDeliverRequestMessage generateMessage(CmppDeliverRequestMessage t, LongMessageFrame frame) throws Exception {
		CmppDeliverRequestMessage requestMessage = t.clone();
		
		requestMessage.setTppid(frame.getTppid());
		requestMessage.setTpudhi(frame.getTpudhi());
		requestMessage.setMsgfmt(frame.getMsgfmt());
		requestMessage.setMsgContentBytes(frame.getMsgContentBytes());
		requestMessage.setMsgLength((short)frame.getMsgLength());
		
		if(frame.getPknumber()!=1){
			requestMessage.getHeader().setSequenceId(DefaultSequenceNumberUtil.getSequenceNo());
		}
		
		return requestMessage;
	}

	@Override
	protected SmsMessage getSmsMessage(CmppDeliverRequestMessage t) {
		return t.getMsg();
	}

	@Override
	protected void resetMessageContent(CmppDeliverRequestMessage t, SmsMessage content) {
		t.setMsgContent(content);
	}

}
