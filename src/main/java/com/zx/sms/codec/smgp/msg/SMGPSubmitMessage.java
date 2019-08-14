package com.zx.sms.codec.smgp.msg;

import java.util.ArrayList;
import java.util.List;

import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.smgp.tlv.TLVByte;
import com.zx.sms.codec.smgp.tlv.TLVString;
import com.zx.sms.codec.smgp.util.ByteUtil;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

public class SMGPSubmitMessage extends SMGPBaseMessage implements LongSMSMessage<SMGPSubmitMessage>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1347896099727385979L;

	private byte msgType=(byte)6; // 1

	private boolean needReport=true; // 1

	private byte priority=2; // 1

	private String serviceId=""; // 10

	private String feeType="00"; // 2

	private String feeCode="000000"; // 6

	private String fixedFee="000000"; // 6  v3.0新增字段

	private SmsDcs msgFmt = GlobalConstance.defaultmsgfmt;

	private String validTime=""; // 17

	private String atTime=""; // 17

	private String srcTermId=""; // 21

	private String chargeTermId=""; // 21

	private byte destTermIdCount; // 1

	private String[] destTermIdArray; // 21*destTermIdCount

	private byte[] bMsgContent; // msgLength

	private String reserve=""; // 8
	
	private SmsMessage msg;
	
	public SMGPSubmitMessage() {
		this.commandId = SMGPConstants.SMGP_SUBMIT;
		registerOptional(tpPid);
		registerOptional(tpUdhi);
		registerOptional(linkId);
		registerOptional(msgSrc);
		registerOptional(chargeUserType);
		registerOptional(chargeTermType);
		registerOptional(chargeTermPseudo);
		registerOptional(destTermType);
		registerOptional(destTermPseudo);
		registerOptional(pkTotal);
		registerOptional(pkNumber);
		registerOptional(submitMsgType);
		registerOptional(spDealResult);
		registerOptional(mServiceId);
	}
	
	
	private TLVByte     tpPid   =new TLVByte(SMGPConstants.OPT_TP_PID);
	private TLVByte     tpUdhi  =new TLVByte(SMGPConstants.OPT_TP_UDHI);
	private TLVString   linkId  =new TLVString(SMGPConstants.OPT_LINK_ID);
	private TLVString   msgSrc  =new TLVString(SMGPConstants.OPT_MSG_SRC);
	private TLVByte     chargeUserType=new TLVByte(SMGPConstants.OPT_CHARGE_USER_TYPE);
	private TLVByte     chargeTermType=new TLVByte(SMGPConstants.OPT_CHARGE_TERM_TYPE);
	private TLVString   chargeTermPseudo=new TLVString(SMGPConstants.OPT_CHARGE_TERM_PSEUDO);
	private TLVByte     destTermType=new TLVByte(SMGPConstants.OPT_DEST_TERM_TYPE);
	private TLVString   destTermPseudo=new TLVString(SMGPConstants.OPT_DEST_TERM_PSEUDO);
	private TLVByte     pkTotal=new TLVByte(SMGPConstants.OPT_PK_TOTAL);
	private TLVByte     pkNumber=new TLVByte(SMGPConstants.OPT_PK_NUMBER);
	private TLVByte     submitMsgType=new TLVByte(SMGPConstants.OPT_SUBMIT_MSG_TYPE);
	private TLVByte     spDealResult=new TLVByte(SMGPConstants.OPT_SP_DEAL_RESULT);
	private TLVString   mServiceId=new TLVString(SMGPConstants.OPT_M_SERVICE_ID);
	
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
	public void setMsgSrc(String value){
		msgSrc.setValue(value);
	}
	public String getMsgSrc(){
		return msgSrc.getValue();
	}
	public void setChargeUserType(byte value){
		chargeUserType.setValue(value);
	}
	public byte getChargeUserType(){
		return chargeUserType.getValue();
	}
	
	public void setChargeTermType(byte value){
		chargeTermType.setValue(value);
	}
	public byte getChargeTermType(){
		return chargeTermType.getValue();
	}
	
	public void setChargeTermPseudo(String value){
		chargeTermPseudo.setValue(value);
	}
	public String getChargeTermPseudo(){
		return chargeTermPseudo.getValue();
	}
	public boolean isReport() {
		return false;
	}
	
	public void setDestTermType(byte value){
		destTermType.setValue(value);
	}
	public byte getDestTermType(){
		return destTermType.getValue();
	}
	
	public void setDestTermPseudo(String value){
		destTermPseudo.setValue(value);
	}
	public String getDestTermPseudo(){
		return destTermPseudo.getValue();
	}	
	

	public void setPkTotal(byte value){
		pkTotal.setValue(value);
	}
	public byte getPkTotal(){
		return pkTotal.getValue();
	}
	
	public void setPkNumber(byte value){
		pkNumber.setValue(value);
	}
	public byte getPkNumber(){
		return pkNumber.getValue();
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

	public void setMServiceId(String value){
		mServiceId.setValue(value);
	}
	public String getMServiceId(){
		return mServiceId.getValue();
	}	
	
	
	
	@Override
	protected int setBody(byte[] bodyBytes,int version) throws Exception {
		int offset = 0;
		byte[] tmp = null;

		msgType = bodyBytes[offset];
		offset += 1;

		needReport = bodyBytes[offset] == 1;
		offset += 1;

		priority = bodyBytes[offset];
		offset += 1;

		tmp = new byte[10];
		System.arraycopy(bodyBytes, offset, tmp, 0, 10);
		serviceId = new String(ByteUtil.rtrimBytes(tmp));
		offset += 10;

		tmp = new byte[2];
		System.arraycopy(bodyBytes, offset, tmp, 0, 2);
		feeType = new String(ByteUtil.rtrimBytes(tmp));
		offset += 2;

		tmp = new byte[6];
		System.arraycopy(bodyBytes, offset, tmp, 0, 6);
		feeCode = new String(ByteUtil.rtrimBytes(tmp));
		offset += 6;

		if(0x13 != version) {
			tmp = new byte[6];
			System.arraycopy(bodyBytes, offset, tmp, 0, 6);
			fixedFee = new String(ByteUtil.rtrimBytes(tmp));
			offset += 6;
		}

		msgFmt = new SmsDcs(bodyBytes[offset]);
		offset += 1;

		tmp = new byte[17];
		System.arraycopy(bodyBytes, offset, tmp, 0, 17);
		validTime = new String(ByteUtil.rtrimBytes(tmp));
		offset += 17;

		tmp = new byte[17];
		System.arraycopy(bodyBytes, offset, tmp, 0, 17);
		atTime = new String(ByteUtil.rtrimBytes(tmp));
		offset += 17;

		tmp = new byte[21];
		System.arraycopy(bodyBytes, offset, tmp, 0, 21);
		srcTermId = new String(ByteUtil.rtrimBytes(tmp));
		offset += 21;

		tmp = new byte[21];
		System.arraycopy(bodyBytes, offset, tmp, 0, 21);
		chargeTermId = new String(ByteUtil.rtrimBytes(tmp));
		offset += 21;

		destTermIdCount = bodyBytes[offset];
		offset += 1;


		if (destTermIdCount >= 100 || destTermIdCount <= 0) {
			throw new Exception("destTermIdCount must be in [1,99],but " + destTermIdCount);
		}
		destTermIdArray = new String[destTermIdCount];
		for (int i = 0; i < destTermIdCount; i++) {
			tmp = new byte[21];
			System.arraycopy(bodyBytes, offset, tmp, 0, 21);
			offset += 21;
			destTermIdArray[i] = new String(ByteUtil.rtrimBytes(tmp));
		}
		byte b = bodyBytes[offset];
		offset += 1;

	    int msgLength = b >= 0 ? b : (256 + b); // byte 最大只有128，这种处理可以取得129-140的数据
		if(msgLength>0){
			tmp = new byte[msgLength];
			System.arraycopy(bodyBytes, offset, tmp, 0, msgLength);
			offset += msgLength;
			bMsgContent = tmp;
		}

		tmp = new byte[8];
		System.arraycopy(bodyBytes, offset, tmp, 0, 8);
		reserve = new String(ByteUtil.rtrimBytes(tmp));
		offset += 8;

		return offset;
	}

	@Override
	protected byte[] getBody(int version) throws Exception {
		int msgLength = bMsgContent.length;
		int len =0+1 + 1 + 1 + 10 + 2 + 6 + (0x13 != version ? 6 : 0 ) + 1 + 17 + 17 + 21 + 21 + 1 + 21* destTermIdCount + 1 + msgLength + 8;
		int offset = 0;
		byte[] bodyBytes = new byte[len];
		bodyBytes[offset] = msgType;
		offset += 1;

		bodyBytes[offset] = needReport?(byte)1:0;
		offset += 1;

		bodyBytes[offset] = priority;
		offset += 1;

		ByteUtil.rfillBytes(serviceId.getBytes(), 10, bodyBytes, offset);
		offset += 10;

		ByteUtil.rfillBytes(feeType.getBytes(), 2, bodyBytes, offset);
		offset += 2;

		ByteUtil.rfillBytes(feeCode.getBytes(), 6, bodyBytes, offset);
		offset += 6;

		if(0x13 != version) {
			ByteUtil.rfillBytes(fixedFee.getBytes(), 6, bodyBytes, offset);
			offset += 6;
		}
		bodyBytes[offset] = msgFmt.getValue();
		offset += 1;

		ByteUtil.rfillBytes(validTime.getBytes(), 17, bodyBytes, offset);
		offset += 17;

		ByteUtil.rfillBytes(atTime.getBytes(), 17, bodyBytes, offset);
		offset += 17;

		ByteUtil.rfillBytes(srcTermId.getBytes(), 21, bodyBytes, offset);
		offset += 21;

		ByteUtil.rfillBytes(chargeTermId.getBytes(), 21, bodyBytes, offset);
		offset += 21;

		bodyBytes[offset] = destTermIdCount;
		offset += 1;

		for (int i = 0; i < destTermIdCount; i++) {
			ByteUtil.rfillBytes(destTermIdArray[i].getBytes(), 21,
					bodyBytes, offset);
			offset += 21;
		}

		bodyBytes[offset] = (byte)msgLength;
		offset += 1;

		if (bMsgContent != null) {
			ByteUtil.rfillBytes(bMsgContent, msgLength, bodyBytes, offset);			
		}
		offset+=msgLength;
		
		ByteUtil.rfillBytes(reserve.getBytes(), 8, bodyBytes, offset);
		offset += 8;

		return bodyBytes;
	}

	public byte getMsgType() {
		return this.msgType;
	}

	public void setMsgType(byte msgType) {
		this.msgType = msgType;
	}

	public boolean isNeedReport() {
		return needReport;
	}
	public void setNeedReport(boolean needReport) {
		this.needReport = needReport;
	}
	public byte getPriority() {
		return this.priority;
	}

	public void setPriority(byte priority) {
		this.priority = priority;
	}

	public String getServiceId() {
		return this.serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getFeeType() {
		return this.feeType;
	}

	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	public String getFeeCode() {
		return this.feeCode;
	}

	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}

	public String getFixedFee() {
		return this.fixedFee;
	}

	public void setFixedFee(String fixedFee) {
		this.fixedFee = fixedFee;
	}

	public SmsDcs getMsgFmt() {
		return this.msgFmt;
	}

	public void setMsgFmt(SmsDcs msgFmt) {
		this.msgFmt = msgFmt;
	}

	public String getValidTime() {
		return this.validTime;
	}

	public void setValidTime(String validTime) {
		this.validTime = validTime;
	}

	public String getAtTime() {
		return this.atTime;
	}

	public void setAtTime(String atTime) {
		this.atTime = atTime;
	}

	public String getSrcTermId() {
		return this.srcTermId;
	}

	public void setSrcTermId(String srcTermId) {
		this.srcTermId = srcTermId;
	}

	public String getChargeTermId() {
		return this.chargeTermId;
	}

	public void setChargeTermId(String chargeTermId) {
		this.chargeTermId = chargeTermId;
	}

	public byte getDestTermIdCount() {
		return this.destTermIdCount;
	}

	public String[] getDestTermIdArray() {
		return destTermIdArray;
	}

	public void setDestTermIdArray(String destTermIdArray) {
		this.destTermIdArray = new String[] { destTermIdArray};
		this.destTermIdCount=(byte)1;
	}
	
	public void setDestTermIdArray(String[] destTermIdArray) {
		this.destTermIdArray = destTermIdArray;
		this.destTermIdCount=(byte)(destTermIdArray==null?0:destTermIdArray.length);
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

	public byte[] getBMsgContent() {
		return this.bMsgContent;
	}

	public void setBMsgContent(byte[] msgContent) {
		bMsgContent = msgContent;
	}

	public String getReserve() {
		return this.reserve;
	}

	public void setReserve(String reserve) {
		this.reserve = reserve;
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
		StringBuffer buffer = new StringBuffer();
		buffer.append("SMGPSubmitMessage:[sequenceNumber=").append(
				sequenceString()).append(",");
		buffer.append("msgType=").append(msgType).append(",");
		buffer.append("needReport=").append(needReport).append(",");
		buffer.append("validTime=").append(validTime).append(",");
		buffer.append("atTime=").append(atTime).append(",");
		buffer.append("srcTermId=").append(srcTermId).append(",");
		buffer.append("chargeTermId=").append(chargeTermId).append(",");
		buffer.append("destTermIdArray={");
		for (int i = 0; i < destTermIdCount; i++) {
			if (i == 0) {
				buffer.append(destTermIdArray[i]);
			} else {
				buffer.append(";" + destTermIdArray[i]);
			}
		}
		buffer.append("},");
		buffer.append("msgContent=").append(getMsgContent()).append("]");
		return buffer.toString();
	}
	
	public SMGPSubmitMessage clone() throws CloneNotSupportedException {
		SMGPSubmitMessage cloned = new SMGPSubmitMessage();
		cloned.setSequenceNo(this.getSequenceNo());
		cloned.setTimestamp(this.getTimestamp());
		cloned.setLifeTime(this.getLifeTime());
		cloned.setServiceId(this.getServiceId());
		cloned.setAtTime(this.getAtTime());
		cloned.setChargeTermId(this.getChargeTermId());
		cloned.setChargeTermPseudo(this.getChargeTermPseudo());
		cloned.setChargeTermType(this.getChargeTermType());
		cloned.setChargeUserType(this.getChargeUserType());
		cloned.setDestTermIdArray(this.getDestTermIdArray());
		cloned.setDestTermPseudo(this.getDestTermPseudo());
		cloned.setDestTermType(this.getDestTermType());
		cloned.setSpDealResult(this.getSpDealResult());
		cloned.setFeeCode(this.getFeeCode());
		cloned.setFeeType(this.getFeeType());
		cloned.setFixedFee(this.getFixedFee());
		cloned.setLifeTime(this.getLifeTime());
		cloned.setLinkId(this.getLinkId());
		cloned.setMServiceId(this.getMServiceId());
		cloned.setMsgType(this.getMsgType());
		cloned.setMsgSrc(this.getMsgSrc());
		cloned.setNeedReport(this.isNeedReport());
		cloned.setPkNumber(this.getPkNumber());
		cloned.setPriority(this.getPriority());
		cloned.setReserve(this.getReserve());
		cloned.setSpDealResult(this.getSpDealResult());
		cloned.setSrcTermId(this.getSrcTermId());
		cloned.setSubmitMsgType(this.getSubmitMsgType());
		cloned.setTpPid(this.getTpPid());
		cloned.setTpUdhi(this.getTpUdhi());
		cloned.setPkNumber(this.getPkNumber());
		cloned.setPkTotal(this.getPkTotal());
		cloned.setValidTime(this.getValidTime());
		cloned.setMsgFmt(this.getMsgFmt());
		cloned.setBMsgContent(this.getBMsgContent());
		cloned.setMsgContent(this.getSmsMessage());
		return cloned;
	}
	
	@Override
	public LongMessageFrame generateFrame() {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(getTpPid());
		frame.setTpudhi(getTpUdhi());
		frame.setMsgfmt(getMsgFmt());
		frame.setMsgContentBytes(getBMsgContent());
		frame.setMsgLength((short)bMsgContent.length);
		frame.setSequence(getSequenceNo());
		return frame;
	}
	@Override
	public SMGPSubmitMessage generateMessage(LongMessageFrame frame) throws Exception {
		SMGPSubmitMessage requestMessage = this.clone();

		requestMessage.setTpUdhi((byte)frame.getTpudhi());
		requestMessage.setMsgFmt(frame.getMsgfmt());
		requestMessage.setBMsgContent(frame.getMsgContentBytes());
		requestMessage.setPkTotal((byte)frame.getPktotal());
		requestMessage.setPkNumber((byte)frame.getPknumber());
		if(frame.getPknumber()!=1){
			requestMessage.setSequenceNumber(DefaultSequenceNumberUtil.getSequenceNo());
		}
		requestMessage.setMsgContent((SmsMessage)null);
		return requestMessage;
	}
	
	private List<SMGPSubmitMessage> fragments = null;
	
	@Override
	public List<SMGPSubmitMessage> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(SMGPSubmitMessage fragment) {
		if(fragments==null)
			fragments = new ArrayList<SMGPSubmitMessage>();
		
		fragments.add(fragment);
	}
}
