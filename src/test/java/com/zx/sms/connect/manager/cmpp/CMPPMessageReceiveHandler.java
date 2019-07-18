package com.zx.sms.connect.manager.cmpp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppQueryRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppQueryResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class CMPPMessageReceiveHandler extends MessageReceiveHandler {

	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {
		int result = RandomUtils.nextInt(0, 100) > 97 ? 0 : 0 ;
		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			
			if(e.getFragments()!=null) {
				//长短信会带有片断
				for(CmppDeliverRequestMessage frag:e.getFragments()) {
					CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(frag.getHeader().getSequenceId());
					responseMessage.setResult(result);
					responseMessage.setMsgId(frag.getMsgId());
					ctx.channel().write(responseMessage);
				}
			}
			
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(result);
			responseMessage.setMsgId(e.getMsgId());
			return ctx.channel().writeAndFlush(responseMessage);

		}else if (msg instanceof CmppSubmitRequestMessage) {
			//接收到 CmppSubmitRequestMessage 消息
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			
			final List<CmppDeliverRequestMessage> reportlist = new ArrayList<CmppDeliverRequestMessage>();
			
			if(e.getFragments()!=null) {
				//长短信会可能带有片断，每个片断都要回复一个response
				for(CmppSubmitRequestMessage frag:e.getFragments()) {
					CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(frag.getHeader().getSequenceId());
					responseMessage.setResult(result);
					ctx.channel().write(responseMessage);
					
					CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
					deliver.setDestId(e.getSrcId());
					deliver.setSrcterminalId(e.getDestterminalId()[0]);
					CmppReportRequestMessage report = new CmppReportRequestMessage();
					report.setDestterminalId(deliver.getSrcterminalId());
					report.setMsgId(responseMessage.getMsgId());
					String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
					report.setSubmitTime(t);
					report.setDoneTime(t);
					report.setStat("DELIVRD");
					report.setSmscSequence(0);
					deliver.setReportRequestMessage(report);
					reportlist.add(deliver);
				}
			}
			
			final CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setResult(result);
			
			ChannelFuture future = ctx.channel().writeAndFlush(resp);
			
			//回复状态报告
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
				reportlist.add(deliver);
				
				ctx.executor().submit(new Runnable() {
					public void run() {
						for(CmppDeliverRequestMessage t : reportlist)
							ctx.channel().writeAndFlush(t);
					}
				});
			}
			return result == 0 ? future : null;
		} else if (msg instanceof CmppQueryRequestMessage) {
			CmppQueryRequestMessage e = (CmppQueryRequestMessage) msg;
			CmppQueryResponseMessage res = new CmppQueryResponseMessage(e.getHeader().getSequenceId());
			return ctx.channel().writeAndFlush(res);
		}
		return null;
	}

}
