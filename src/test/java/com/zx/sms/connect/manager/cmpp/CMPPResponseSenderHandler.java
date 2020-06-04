package com.zx.sms.connect.manager.cmpp;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppQueryRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.handler.api.AbstractBusinessHandler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class CMPPResponseSenderHandler extends AbstractBusinessHandler {
	
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	
    	//此时未经过长短信合并
    	if (msg instanceof CmppDeliverRequestMessage) {
    		CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
    		CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(0);
			responseMessage.setMsgId(e.getMsgId());
			ctx.channel().writeAndFlush(responseMessage);
    	}else if (msg instanceof CmppSubmitRequestMessage) {
    		CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
    		CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setResult(0);
			ctx.channel().writeAndFlush(resp);
			
			if(e.getRegisteredDelivery()==1) {
				final CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
				deliver.setDestId(e.getSrcId());
				deliver.setSrcterminalId(e.getDestterminalId()[0]);
				CmppReportRequestMessage report = new CmppReportRequestMessage();
				report.setDestterminalId(deliver.getSrcterminalId());
				report.setMsgId(resp.getMsgId());
				String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
				report.setSubmitTime(t);
				report.setDoneTime(t);
				report.setStat("DELIVRD");
				report.setSmscSequence(0);
				deliver.setReportRequestMessage(report);
				ctx.executor().submit(new Runnable() {
					public void run() {
							ctx.channel().writeAndFlush(deliver);
					}
				});
			}
			
			
    	}else if (msg instanceof CmppQueryRequestMessage) {
			CmppQueryRequestMessage e = (CmppQueryRequestMessage) msg;
			CmppQueryResponseMessage res = new CmppQueryResponseMessage(e.getHeader().getSequenceId());
			ctx.channel().writeAndFlush(res);
		}
    	
    	ctx.fireChannelRead(msg);
    }
    
	@Override
	public String name() {
		return "CMPPResponseSenderHandler";
	}

}
