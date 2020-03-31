package com.zx.sms.codec.smgp.msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.smgp.tlv.TLVByte;
import com.zx.sms.codec.smgp.tlv.TLVString;
import com.zx.sms.codec.smgp.util.ByteUtil;
import com.zx.sms.codec.smgp.util.SMGPMsgIdUtil;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

public class SMGPDeliverMessage extends SMGPBaseMessage implements LongSMSMessage<SMGPDeliverMessage>{
	private static final Logger logger = LoggerFactory.getLogger(SMGPDeliverMessage.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6960208317220566142L;

	private MsgId msgId = new MsgId(); // 10

	private boolean isReport; // 1

	private SmsDcs msgFmt = GlobalConstance.defaultmsgfmt;

	private String recvTime = DateFormatUtils.format((new Date()), "yyyyMMddHHmmss"); // 14

	private String srcTermId; // 21

	private String destTermId; // 21

	private byte[] bMsgContent; // msgLength

	private String reserve=""; // 8
	
	private SmsMessage msg;
	
	private SMGPReportData report;
	
	private TLVByte     tpPid   =new TLVByte(SMGPConstants.OPT_TP_PID);
	private TLVByte     tpUdhi  =new TLVByte(SMGPConstants.OPT_TP_UDHI);
	private TLVString   linkId  =new TLVString(SMGPConstants.OPT_LINK_ID);
	private TLVByte     srcTermType=new TLVByte(SMGPConstants.OPT_SRC_TERM_TYPE);
	private TLVString   srcTermPseudo=new TLVString(SMGPConstants.OPT_SRC_TERM_PSEUDO);
	private TLVByte     submitMsgType=new TLVByte(SMGPConstants.OPT_SUBMIT_MSG_TYPE);
	private TLVByte     spDealResult=new TLVByte(SMGPConstants.OPT_SP_DEAL_RESULT);

	public SMGPDeliverMessage() {
		this.commandId = SMGPConstants.SMGP_DELIVER;
		registerOptional(tpPid);
		registerOptional(tpUdhi);
		registerOptional(linkId);
		registerOptional(srcTermType);
		registerOptional(srcTermPseudo);
		registerOptional(submitMsgType);
		registerOptional(spDealResult);		
	}

	
	
	public void setTpPid(byte value){
		tpPid.setValue(value);
	}
	public byte getTpPid(){
		return tpPid.getValue();
	}
	public void setTpUdhi(byte value){
		tpUdhi.setValue(value);
	}
	public byte getTpUdhi(){
		return tpUdhi.getValue();
	}
	public void setLinkId(String value){
		linkId.setValue(value);
	}
	public String getLinkId(){
		return linkId.getValue();
	}

	public void setSrcTermType(byte value){
		srcTermType.setValue(value);
	}
	public byte getSrcTermType(){
		return srcTermType.getValue();
	}
	
	public void setSrcTermPseudo(String value){
		srcTermPseudo.setValue(value);
	}
	public String getSrcTermPseudo(){
		return srcTermPseudo.getValue();
	}	

	public void setSubmitMsgType(byte value){
		submitMsgType.setValue(value);
	}
	public byte getSubmitMsgType(){
		return submitMsgType.getValue();
	}
	
	public void setSpDealResult(byte value){
		spDealResult.setValue(value);
	}
	public byte getSpDealResult(){
		return spDealResult.getValue();
	}


	
	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		int offset = 0;
		byte[] tmp = null;

		byte[] msgId = new byte[10];
		System.arraycopy(bodyBytes, offset, msgId, 0, 10);
		this.msgId = SMGPMsgIdUtil.bytes2MsgId(msgId);
		offset += 10;

		isReport = bodyBytes[offset]==1;
		offset += 1;

		msgFmt = new SmsDcs(bodyBytes[offset]);
		offset += 1;

		tmp = new byte[14];
		System.arraycopy(bodyBytes, offset, tmp, 0, 14);
		recvTime = new String(ByteUtil.rtrimBytes(tmp));
		offset += 14;

		tmp = new byte[21];
		System.arraycopy(bodyBytes, offset, tmp, 0, 21);
		srcTermId = new String(ByteUtil.rtrimBytes(tmp));
		offset += 21;

		tmp = new byte[21];
		System.arraycopy(bodyBytes, offset, tmp, 0, 21);
		destTermId = new String(ByteUtil.rtrimBytes(tmp));
		offset += 21;

		byte b = bodyBytes[offset];
		offset += 1;

		int msgLength = b >= 0 ? b : (256 + b); // byte 最大只有128，这种处理可以取得129-140的数据
		
		if(msgLength>0){
			tmp = new byte[msgLength];
			System.arraycopy(bodyBytes, offset, tmp, 0, msgLength);
			offset += msgLength;
			
			if(isReport()){
				SMGPReportData tmpreport = new SMGPReportData();
				tmpreport.fromBytes(tmp);
				report = tmpreport;
			}else{
				bMsgContent = tmp;
			}
		}

		tmp = new byte[8];
		System.arraycopy(bodyBytes, offset, tmp, 0, 8);
		reserve = new String(ByteUtil.rtrimBytes(tmp));
		offset += 8;
		
		return offset;

	}

	@Override
	protected byte[] getBody(int version) throws Exception {
		
		int msgLength = 0 ;
		byte[] msgContent = null;
		if(isReport()){
			msgContent = report.toBytes();
			msgLength = msgContent.length;
		}else{
			msgContent = bMsgContent;
			msgLength = bMsgContent.length;
		}
		
		int len = 10 + 1 + 1 + 14 + 21 + 21 + 1 + msgLength + 8;
		int offset = 0;
		byte[] bodyBytes = new byte[len];
		byte[] b_msgId = SMGPMsgIdUtil.msgId2Bytes(msgId);
		System.arraycopy(b_msgId, 0, bodyBytes, offset, 10);
		offset += 10;

		bodyBytes[offset] = isReport ? (byte)1 : (byte)0;
		offset += 1;

		bodyBytes[offset] = msgFmt.getValue();
		offset += 1;

		ByteUtil.rfillBytes(recvTime.getBytes(), 14, bodyBytes, offset);
		offset += 14;

		ByteUtil.rfillBytes(srcTermId.getBytes(), 21, bodyBytes, offset);
		offset += 21;

		ByteUtil.rfillBytes(destTermId.getBytes(), 21, bodyBytes, offset);
		offset += 21;

		bodyBytes[offset] = (byte)msgLength;
		offset += 1;

		ByteUtil.rfillBytes(msgContent, msgLength, bodyBytes, offset);
		offset+=msgLength;

		ByteUtil.rfillBytes(reserve.getBytes(), 8, bodyBytes, offset);
		offset += 8;

		return bodyBytes;
	}

	public MsgId getMsgId() {
		return this.msgId;
	}

	public void setMsgId(MsgId msgId) {
		this.msgId = msgId;
	}

	public boolean isReport() {
		return isReport;
	}

	public SmsDcs getMsgFmt() {
		return this.msgFmt;
	}

	public void setMsgFmt(SmsDcs msgFmt) {
		this.msgFmt = msgFmt;
	}

	public String getRecvTime() {
		return this.recvTime;
	}

	public void setRecvTime(String recvTime) {
		this.recvTime = recvTime;
	}

	public void setRecvTime(Date recvTime) {
		this.recvTime = DateFormatUtils.format(recvTime, "yyyyMMddHHmmss");
	}
	
	public String getSrcTermId() {
		return this.srcTermId;
	}

	public void setSrcTermId(String srcTermId) {
		this.srcTermId = srcTermId;
	}

	public String getDestTermId() {
		return this.destTermId;
	}

	public void setDestTermId(String destTermId) {
		this.destTermId = destTermId;
	}

	public byte[] getBMsgContent() {
		return this.bMsgContent;
	}

	public void setBMsgContent(byte[] msgContent) {
		bMsgContent = msgContent;
	}

	public void setMsgContent(String msgContent) {
		setMsgContent(CMPPCommonUtil.buildTextMessage(msgContent));
	}
	
	public void setMsgContent(SmsMessage msg){
		this.msg = msg;
	}

	public SmsMessage getSmsMessage() {
		return msg;
	}
	
	public SMGPReportData getReport(){
		return report;
	}
	
	public void setReport(SMGPReportData report){
		this.report = report;
		this.isReport=true;
	}

	public String getReserve() {
		return this.reserve;
	}

	public void setReserve(String reserve) {
		this.reserve = reserve;
	}

	private String msgIdString(){
		return msgId.toString();
	}
	
	public String getMsgContent() {
		if(msg instanceof SmsMessage){
			return msg.toString();
		}
		
		if(bMsgContent!=null && bMsgContent.length>0){
			LongMessageFrame frame = generateFrame();
			return LongMessageFrameHolder.INS.getPartTextMsg(frame);
		}
	
	return "";
}
	
	@Override
	public String toString() {
		if(isReport){
			StringBuffer buffer = new StringBuffer();
			buffer.append("SMGPDeliverMessage:[sequenceNumber=").append(
					sequenceString()).append(",");
			buffer.append("msgId=").append(msgIdString()).append(",");
			buffer.append("recvTime=").append(recvTime).append(",");
			buffer.append("srcTermId=").append(srcTermId).append(",");
			buffer.append("destTermId=").append(destTermId).append(",");
			buffer.append("ReportDate=").append(getReport()).append("]");
			return buffer.toString();
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPDeliverMessage:[sequenceNumber=").append(
				sequenceString()).append(",");
		buffer.append("msgId=").append(msgIdString()).append(",");
		buffer.append("recvTime=").append(recvTime).append(",");
		buffer.append("srcTermId=").append(srcTermId).append(",");
		buffer.append("destTermId=").append(destTermId).append(",");
		buffer.append("msgContent=").append(getMsgContent()).append("]");
		
		return buffer.toString();
	}

	public SMGPDeliverMessage clone() throws CloneNotSupportedException {
		return (SMGPDeliverMessage) super.clone();
	}

	@Override
	public LongMessageFrame generateFrame() {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(getTpPid());
		frame.setTpudhi(getTpUdhi());
		frame.setMsgfmt(getMsgFmt());
		frame.setMsgContentBytes(getBMsgContent());
		frame.setMsgLength((short)this.bMsgContent.length);
		frame.setSequence(getSequenceNo());
		return frame;
	}

	@Override
	public SMGPDeliverMessage generateMessage(LongMessageFrame frame) throws Exception {
		SMGPDeliverMessage requestMessage = this.clone();
		
		requestMessage.setTpUdhi((byte)frame.getTpudhi());
		requestMessage.setMsgFmt((SmsDcs)frame.getMsgfmt());
		requestMessage.setBMsgContent(frame.getMsgContentBytes());
		
		if(frame.getPknumber()!=1){
			requestMessage.setSequenceNo(DefaultSequenceNumberUtil.getSequenceNo());
		}
		requestMessage.setMsgContent((SmsMessage)null);
		return requestMessage;
	}
	
	private List<SMGPDeliverMessage> fragments = null;
	
	@Override
	public List<SMGPDeliverMessage> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(SMGPDeliverMessage fragment) {
		if(fragments==null)
			fragments = new ArrayList<SMGPDeliverMessage>();
		
		fragments.add(fragment);
	}
	
}