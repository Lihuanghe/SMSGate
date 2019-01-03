package com.zx.sms.codec.sgip12.codec;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.msg.DefaultMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipDeliverResponseMessage;
import com.zx.sms.codec.sgip12.msg.SgipReportRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitResponseMessage;
import com.zx.sms.common.util.SequenceNumber;
import com.zx.sms.handler.api.AbstractBusinessHandler;

@Sharable
public class Sgip2CMPPBusinessHandler extends AbstractBusinessHandler {

	private static Sgip2CMPPCodec codec = new Sgip2CMPPCodec();
	@Override
	public String name() {
		return "Sgip2CMPPBusinessHandler";
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	codec.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	codec.write(ctx, msg, promise);
    }
    
    private static class Sgip2CMPPCodec extends MessageToMessageCodec<DefaultMessage, DefaultMessage> {

    	@Override
    	protected void encode(ChannelHandlerContext ctx, DefaultMessage msg, List<Object> out) throws Exception {
    		if(msg instanceof CmppDeliverRequestMessage){
    			DefaultMessage pdu = null;
    			CmppDeliverRequestMessage deliver = (CmppDeliverRequestMessage)msg;
    			if(deliver.isReport()){
    				SgipReportRequestMessage pdur = new SgipReportRequestMessage(deliver.getHeader());
    				CmppReportRequestMessage report = deliver.getReportRequestMessage();
    				pdur.setSequenceId(new SequenceNumber(report.getMsgId()));
    				pdur.setUsernumber(report.getDestterminalId());
    				
    				pdur.setReserve(report.getStat());
    				pdu = pdur;
    			}else{
        			SgipDeliverRequestMessage sgipmsg = new SgipDeliverRequestMessage(deliver.getHeader());
        			sgipmsg.setTimestamp(deliver.getTimestamp());
        			sgipmsg.setUsernumber(deliver.getDestId());
        			sgipmsg.setSpnumber(deliver.getSrcterminalId());
        			sgipmsg.setTppid(deliver.getTppid());
        			sgipmsg.setTpudhi(deliver.getTpudhi());
        			sgipmsg.setMsgfmt(deliver.getMsgfmt());
        			sgipmsg.setMsgContent(deliver.getSmsMessage());
        			pdu = sgipmsg;
    			}
    		    out.add(pdu);
    			
    		}else if(msg instanceof CmppDeliverResponseMessage){
    			CmppDeliverResponseMessage message = (CmppDeliverResponseMessage)msg;
    			SgipDeliverResponseMessage resp = new SgipDeliverResponseMessage(message.getHeader());
    			resp.setResult((short)message.getResult());;
    		    out.add(resp);
    			
    		}else if(msg instanceof CmppSubmitRequestMessage){
    			CmppSubmitRequestMessage submit = (CmppSubmitRequestMessage)msg;
    			
    			SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage(submit.getHeader());
    			requestMessage.setTimestamp(msg.getTimestamp());
    			requestMessage.setSpnumber(submit.getSrcId());
    			short cnt = (short) submit.getDestUsrtl();
    			String[] nums = new String[cnt];
    			for (int i = 0; i < cnt; i++) {
    				nums[i]= submit.getDestterminalId()[i];
    			}
    			requestMessage.setUsernumber(nums);
    			requestMessage.setCorpid(submit.getMsgsrc());
    			requestMessage.setReportflag(submit.getRegisteredDelivery());
    			requestMessage.setTppid(submit.getTppid());
    			requestMessage.setTpudhi(submit.getTpudhi());
    			requestMessage.setMsgfmt(submit.getMsgfmt());
    			requestMessage.setMsgContent(submit.getSmsMessage());
    			requestMessage.setMessagelength(submit.getMsgLength());
    	        out.add(requestMessage);
    			
    			
    		}else if(msg instanceof CmppSubmitResponseMessage){
    			
    			SgipSubmitResponseMessage resp = new SgipSubmitResponseMessage(msg.getHeader());
    			CmppSubmitResponseMessage message = (CmppSubmitResponseMessage)msg;
    			resp.setResult((short)message.getResult());
    			resp.setReserve(message.getMsgId().toString());
    			out.add(resp);
    		}else{
    			out.add(msg);
    		}
    	}
    	

    	@Override
    	protected void decode(ChannelHandlerContext ctx, DefaultMessage msg, List<Object> out) throws Exception {
    		if(msg instanceof SgipDeliverRequestMessage){
    			SgipDeliverRequestMessage deli = (SgipDeliverRequestMessage)msg;
    			CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage(deli.getHeader());
    			deliver.setTimestamp(deli.getTimestamp());
    			deliver.setMsgContent(deli.getSmsMessage());
    			deliver.setSrcterminalId(deli.getSpnumber());
    			deliver.setDestId(deli.getUsernumber());
    			deliver.setMsgfmt(deli.getMsgfmt());
    			deliver.setTppid(deli.getTppid());
    			deliver.setTpudhi(deli.getTpudhi());
    			out.add(deliver);
    		}else if(msg instanceof SgipReportRequestMessage){
    			SgipReportRequestMessage rece = (SgipReportRequestMessage)msg;
				CmppReportRequestMessage report = new CmppReportRequestMessage();
				
				CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage(rece.getHeader());
				deliver.setTimestamp(rece.getTimestamp());
				deliver.setReportRequestMessage(report);
				deliver.setDestId(rece.getUsernumber());
				
				out.add(deliver);
    		}else if(msg instanceof SgipDeliverResponseMessage){
    			SgipDeliverResponseMessage deliresp = (SgipDeliverResponseMessage)msg;
    			CmppDeliverResponseMessage delir = new CmppDeliverResponseMessage(msg.getHeader());
    			delir.setResult(deliresp.getResult());
    			out.add(delir);
    		}else if(msg instanceof SgipSubmitRequestMessage){
    			SgipSubmitRequestMessage sm = (SgipSubmitRequestMessage)msg;
    			CmppSubmitRequestMessage submit = new CmppSubmitRequestMessage(msg.getHeader());
    			submit.setTimestamp(sm.getTimestamp());
    			submit.setDestterminalId(sm.getUsernumber());
    			submit.setRegisteredDelivery(sm.getReportflag());
    			submit.setSrcId(sm.getSpnumber());
    			submit.setMsgsrc(sm.getCorpid());
    			submit.setMsgContent(sm.getSmsMessage());
    			submit.setMsgfmt(sm.getMsgfmt());
    			submit.setTppid(sm.getTppid());
    			submit.setTpudhi(sm.getTpudhi());
    			out.add(submit);
    			
    		}else if(msg instanceof SgipSubmitResponseMessage){
    			SgipSubmitResponseMessage submitresp = (SgipSubmitResponseMessage)msg;
    			CmppSubmitResponseMessage mtresp = new CmppSubmitResponseMessage(msg.getHeader());
    			mtresp.setResult(submitresp.getResult());
    			out.add(mtresp);
    		}else{
    			out.add(msg);
    		}
    	}

    }
}
