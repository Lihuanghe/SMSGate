package com.zx.sms.handler.smgp;

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
import com.zx.sms.codec.smgp.msg.MsgId;
import com.zx.sms.codec.smgp.msg.SMGPBaseMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverMessage;
import com.zx.sms.codec.smgp.msg.SMGPDeliverRespMessage;
import com.zx.sms.codec.smgp.msg.SMGPReportData;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitRespMessage;
import com.zx.sms.handler.api.AbstractBusinessHandler;

@Sharable
public class SMGP2CMPPBusinessHandler extends AbstractBusinessHandler {

	private static SMGP2CMPPCodec codec = new SMGP2CMPPCodec();
	@Override
	public String name() {
		return "SMGP2CMPPBusinessHandler";
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	codec.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	codec.write(ctx, msg, promise);
    }
    
    private static class SMGP2CMPPCodec extends MessageToMessageCodec<SMGPBaseMessage, DefaultMessage> {

    	@Override
    	protected void encode(ChannelHandlerContext ctx, DefaultMessage msg, List<Object> out) throws Exception {
    		if(msg instanceof CmppDeliverRequestMessage){
    			SMGPDeliverMessage pdu = null;
    			CmppDeliverRequestMessage deliver = (CmppDeliverRequestMessage)msg;
    			if(deliver.isReport()){
    				SMGPDeliverMessage pdur = new SMGPDeliverMessage();
    				SMGPReportData smgpreport = new SMGPReportData();
    				CmppReportRequestMessage report = deliver.getReportRequestMessage();
    				MsgId msgid =  new MsgId();
    				msgid.setDay(report.getMsgId().getDay());
    				msgid.setGateId(report.getMsgId().getGateId());
    				msgid.setHour(report.getMsgId().getHour());
    				msgid.setMinutes(report.getMsgId().getMinutes());
    				msgid.setMonth(report.getMsgId().getMonth());
    				msgid.setSequenceId(report.getMsgId().getSequenceId());
    				
    				smgpreport.setMsgId(msgid);
    				smgpreport.setDlvrd("000");
    				smgpreport.setSub("000");
    				smgpreport.setSubTime(report.getSubmitTime());
    				smgpreport.setDoneTime(report.getDoneTime());
    				smgpreport.setStat(report.getStat());
    				smgpreport.setErr("000");
    				smgpreport.setTxt("ZYZX");
    				pdur.setReport(smgpreport);
    				pdu = pdur;
    			}else{
    				pdu = new SMGPDeliverMessage();
    			}
    			
    			MsgId msgid =  new MsgId();
				msgid.setDay(deliver.getMsgId().getDay());
				msgid.setGateId(deliver.getMsgId().getGateId());
				msgid.setHour(deliver.getMsgId().getHour());
				msgid.setMinutes(deliver.getMsgId().getMinutes());
				msgid.setMonth(deliver.getMsgId().getMonth());
				msgid.setSequenceId(deliver.getMsgId().getSequenceId());

				pdu.setMsgFmt(deliver.getMsgfmt());
		        pdu.setSequenceNo(deliver.getHeader().getSequenceId());
		        pdu.setLinkId(deliver.getLinkid());
		        pdu.setDestTermId(deliver.getDestId());
		        pdu.setMsgContent(deliver.getSmsMessage());
		        pdu.setMsgId(msgid);
		        pdu.setSrcTermId(deliver.getSrcterminalId());
		        out.add(pdu);
    			
    		}else if(msg instanceof CmppDeliverResponseMessage){
    			
    			SMGPDeliverRespMessage resp = new SMGPDeliverRespMessage();
    		    resp.setSequenceNo((int)msg.getHeader().getSequenceId());
    		    resp.setStatus((int)((CmppDeliverResponseMessage)msg).getResult());
    		    out.add(resp);
    			
    		}else if(msg instanceof CmppSubmitRequestMessage){
    			CmppSubmitRequestMessage submit = (CmppSubmitRequestMessage)msg;
    			SMGPSubmitMessage pdu = new SMGPSubmitMessage();
    	        pdu.setSequenceNo(submit.getHeader().getSequenceId());
    	        
    	        pdu.setDestTermIdArray(submit.getDestterminalId());
    	        pdu.setLinkId(submit.getLinkID());
    	        pdu.setMsgContent(submit.getSmsMessage());
    	        pdu.setSrcTermId(submit.getSrcId());
    	        pdu.setMsgSrc(submit.getMsgsrc());
    	        out.add(pdu);
    			
    		}else if(msg instanceof CmppSubmitResponseMessage){
    			SMGPSubmitRespMessage resp = new SMGPSubmitRespMessage();
    		    resp.setSequenceNo((int)msg.getHeader().getSequenceId());
    		    resp.setStatus((int)((CmppSubmitResponseMessage)msg).getResult());
    		    out.add(resp);
    		}
    	}
    	

    	@Override
    	protected void decode(ChannelHandlerContext ctx, SMGPBaseMessage msg, List<Object> out) throws Exception {
    		if(msg instanceof SMGPDeliverMessage){
    			SMGPDeliverMessage deli = (SMGPDeliverMessage)msg;
    			if(deli.isReport()){
    				CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
    				SMGPReportData rece = deli.getReport();
    				CmppReportRequestMessage report = new CmppReportRequestMessage();

    				com.zx.sms.common.util.MsgId msgid =  new com.zx.sms.common.util.MsgId();
    				msgid.setDay(deliver.getMsgId().getDay());
    				msgid.setGateId(deliver.getMsgId().getGateId());
    				msgid.setHour(deliver.getMsgId().getHour());
    				msgid.setMinutes(deliver.getMsgId().getMinutes());
    				msgid.setMonth(deliver.getMsgId().getMonth());
    				msgid.setSequenceId(deliver.getMsgId().getSequenceId());
    				
    				
    				report.setMsgId(msgid);
    				report.setDoneTime(rece.getDoneTime());
    				report.setSubmitTime(rece.getSubTime());
    				report.setStat(rece.getStat());
    				deliver.setReportRequestMessage(report);
    				deliver.getHeader().setSequenceId(deli.getSequenceNo());
        			deliver.setSrcterminalId(deli.getSrcTermId());
        			deliver.setDestId(deli.getDestTermId());
        			deliver.setMsgfmt(deli.getMsgFmt());
        			out.add(deliver);
    			}else{
        			CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
        			deliver.getHeader().setSequenceId(deli.getSequenceNo());
        			deliver.setMsgContent(deli.getSmsMessage());
        			deliver.setSrcterminalId(deli.getSrcTermId());
        			deliver.setDestId(deli.getDestTermId());
        			deliver.setMsgfmt(deli.getMsgFmt());
        			out.add(deliver);
    			}
    			
    		}else if(msg instanceof SMGPDeliverRespMessage){
    			SMGPDeliverRespMessage deliresp = (SMGPDeliverRespMessage)msg;
    			CmppDeliverResponseMessage delir = new CmppDeliverResponseMessage(msg.getSequenceNo());
    			delir.setResult(deliresp.getStatus());
    			out.add(delir);
    		}else if(msg instanceof SMGPSubmitMessage){
    			SMGPSubmitMessage sm = (SMGPSubmitMessage)msg;
    			CmppSubmitRequestMessage submit = new CmppSubmitRequestMessage();
    			submit.getHeader().setSequenceId(sm.getSequenceNo());
    			submit.setDestterminalId(sm.getDestTermIdArray());
    			submit.setSrcId(sm.getSrcTermId());
    			submit.setMsgContent(sm.getSmsMessage());
    			submit.setMsgfmt(sm.getMsgFmt());
    			out.add(submit);
    			
    		}else if(msg instanceof SMGPSubmitRespMessage){
    			SMGPSubmitRespMessage submitresp = (SMGPSubmitRespMessage)msg;
    			CmppSubmitResponseMessage mtresp = new CmppSubmitResponseMessage(msg.getSequenceNo());
    			mtresp.setResult(submitresp.getStatus());
    			out.add(mtresp);
    		}else{
    			out.add(msg);
    		}
    	}

    }
}
