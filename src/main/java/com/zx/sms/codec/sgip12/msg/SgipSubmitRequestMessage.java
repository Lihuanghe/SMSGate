/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

/**
 * @author huzorro(huzorro@gmail.com)
 * 
 */
public class SgipSubmitRequestMessage extends SgipDefaultMessage implements LongSMSMessage<SgipSubmitRequestMessage>{
	private static final long serialVersionUID = 5265747696709571791L;

	private String spnumber = GlobalConstance.emptyString;
	private String chargenumber = GlobalConstance.emptyString;
	private String[] usernumber = null;
	private String corpid = GlobalConstance.emptyString;
	private String servicetype = GlobalConstance.emptyString;
	private short feetype = 2;
	private String feevalue = GlobalConstance.emptyString;
	private String givenvalue = GlobalConstance.emptyString;
	private short agentflag = 0;
	private short morelatetomtflag = 0;
	private short priority = 9;
	private String expiretime = GlobalConstance.emptyString;
	private String scheduletime = GlobalConstance.emptyString;
	private short reportflag = 1;
	private short tppid = 0;
	private short tpudhi = 0;
	private SmsDcs msgfmt = GlobalConstance.defaultmsgfmt;
	private short messagetype = 0;
	private int messagelength = 120;
	private String reserve = GlobalConstance.emptyString;
	
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;
	private SmsMessage msg;
	
	public SgipSubmitRequestMessage() {
		super(SgipPacketType.SUBMITREQUEST);
	}

	public SgipSubmitRequestMessage(Header header) {
		super(SgipPacketType.SUBMITREQUEST,header);
	}
	
	public boolean isReport() {
		return false;
	}

	/**
	 * @return the spnumber
	 */
	public String getSpnumber() {
		return spnumber;
	}

	/**
	 * @param spnumber the spnumber to set
	 */
	public void setSpnumber(String spnumber) {
		this.spnumber = spnumber;
	}

	/**
	 * @return the chargenumber
	 */
	public String getChargenumber() {
		return chargenumber;
	}

	/**
	 * @param chargenumber the chargenumber to set
	 */
	public void setChargenumber(String chargenumber) {
		this.chargenumber = chargenumber;
	}

	/**
	 * @return the usercount
	 */
	public short getUsercount() {
		return (short) usernumber.length;
	}
	
	public String[] getUsernumber() {
		return usernumber;
	}

	public void setUsernumber(String[] usernumber) {
		this.usernumber = usernumber;
	}
	public void setUsernumber(String usernumber) {
		this.usernumber = new String [] {usernumber};
	}
	/**
	 * @return the corpid
	 */
	public String getCorpid() {
		return corpid;
	}

	/**
	 * @param corpid the corpid to set
	 */
	public void setCorpid(String corpid) {
		this.corpid = corpid;
	}

	/**
	 * @return the servicetype
	 */
	public String getServicetype() {
		return servicetype;
	}

	/**
	 * @param servicetype the servicetype to set
	 */
	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}

	/**
	 * @return the feetype
	 */
	public short getFeetype() {
		return feetype;
	}

	/**
	 * @param feetype the feetype to set
	 */
	public void setFeetype(short feetype) {
		this.feetype = feetype;
	}

	/**
	 * @return the feevalue
	 */
	public String getFeevalue() {
		return feevalue;
	}

	/**
	 * @param feevalue the feevalue to set
	 */
	public void setFeevalue(String feevalue) {
		this.feevalue = feevalue;
	}

	/**
	 * @return the givenvalue
	 */
	public String getGivenvalue() {
		return givenvalue;
	}

	/**
	 * @param givenvalue the givenvalue to set
	 */
	public void setGivenvalue(String givenvalue) {
		this.givenvalue = givenvalue;
	}

	/**
	 * @return the agentflag
	 */
	public short getAgentflag() {
		return agentflag;
	}

	/**
	 * @param agentflag the agentflag to set
	 */
	public void setAgentflag(short agentflag) {
		this.agentflag = agentflag;
	}

	/**
	 * @return the morelatetomtflag
	 */
	public short getMorelatetomtflag() {
		return morelatetomtflag;
	}

	/**
	 * @param morelatetomtflag the morelatetomtflag to set
	 */
	public void setMorelatetomtflag(short morelatetomtflag) {
		this.morelatetomtflag = morelatetomtflag;
	}

	/**
	 * @return the priority
	 */
	public short getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(short priority) {
		this.priority = priority;
	}

	/**
	 * @return the expiretime
	 */
	public String getExpiretime() {
		return expiretime;
	}

	/**
	 * @param expiretime the expiretime to set
	 */
	public void setExpiretime(String expiretime) {
		this.expiretime = expiretime;
	}

	/**
	 * @return the scheduletime
	 */
	public String getScheduletime() {
		return scheduletime;
	}

	/**
	 * @param scheduletime the scheduletime to set
	 */
	public void setScheduletime(String scheduletime) {
		this.scheduletime = scheduletime;
	}

	/**
	 * @return the reportflag
	 */
	public short getReportflag() {
		return reportflag;
	}

	/**
	 * @param reportflag the reportflag to set
	 */
	public void setReportflag(short reportflag) {
		this.reportflag = reportflag;
	}

	/**
	 * @return the tppid
	 */
	public short getTppid() {
		return tppid;
	}

	/**
	 * @param tppid the tppid to set
	 */
	public void setTppid(short tppid) {
		this.tppid = tppid;
	}

	/**
	 * @return the tpudhi
	 */
	public short getTpudhi() {
		return tpudhi;
	}

	/**
	 * @param tpudhi the tpudhi to set
	 */
	public void setTpudhi(short tpudhi) {
		this.tpudhi = tpudhi;
	}

	/**
	 * @return the messagetype
	 */
	public short getMessagetype() {
		return messagetype;
	}

	/**
	 * @param messagetype the messagetype to set
	 */
	public void setMessagetype(short messagetype) {
		this.messagetype = messagetype;
	}

	/**
	 * @return the messagelength
	 */
	public int getMessagelength() {
		return messagelength;
	}

	/**
	 * @param messagelength the messagelength to set
	 */
	public void setMessagelength(int messagelength) {
		this.messagelength = messagelength;
	}

	/**
	 * @return the reserve
	 */
	public String getReserve() {
		return reserve;
	}

	/**
	 * @param reserve the reserve to set
	 */
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
    /**
	 * @return the msgContentBytes
	 */
	public byte[] getMsgContentBytes() {
		return msgContentBytes;
	}
	/**
	 * @param msgContentBytes the msgContentBytes to set
	 */
	public void setMsgContentBytes(byte[] msgContentBytes) {
		this.msgContentBytes = msgContentBytes;
	}
	
	
	public String getMsgContent() {
		if(msg instanceof SmsMessage){
			return msg.toString();
		}
		
		if(msgContentBytes!=null && msgContentBytes.length>0){
			LongMessageFrame frame = generateFrame();
			return LongMessageFrameHolder.INS.getPartTextMsg(frame);
		}
	
	return "";
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
	
	public SgipSubmitRequestMessage clone() throws CloneNotSupportedException {
		return (SgipSubmitRequestMessage) super.clone();
	}
	
	public SmsDcs getMsgfmt() {
		return msgfmt;
	}

	public void setMsgfmt(SmsDcs msgfmt) {
		this.msgfmt = msgfmt;
	}
	
	@Override
	public LongMessageFrame generateFrame() {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(getTppid());
		frame.setTpudhi(getTpudhi());
		frame.setMsgfmt(getMsgfmt());
		frame.setMsgContentBytes(getMsgContentBytes());
		frame.setMsgLength((short)getMessagelength());
		frame.setSequence(getSequenceNo());
		return frame;
	}

	@Override
	public SgipSubmitRequestMessage generateMessage(LongMessageFrame frame) throws Exception {
		SgipSubmitRequestMessage requestMessage = this.clone();
		
		requestMessage.setTpudhi(frame.getTpudhi());
		requestMessage.setMsgfmt(frame.getMsgfmt());
		requestMessage.setMsgContentBytes(frame.getMsgContentBytes());
		requestMessage.setMessagelength((short)frame.getMsgLength());
		
		if(frame.getPknumber()!=1){
			requestMessage.getHeader().setSequenceId(DefaultSequenceNumberUtil.getSequenceNo());
		}
		requestMessage.setMsgContent((SmsMessage)null);
		return requestMessage;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SgipSubmitRequestMessage [corpid=").append(corpid)
		.append(", spnumber=").append(spnumber)
		.append(", destterminalId=").append(Arrays.toString(usernumber))
		.append(", msgContent=").append(getMsgContent())
		.append(", seq=").append(getSequenceNumber())
		.append(", Header=").append(getHeader()).append("]");
		return sb.toString();
	}
	private List<SgipSubmitRequestMessage> fragments = null;
	
	@Override
	public List<SgipSubmitRequestMessage> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(SgipSubmitRequestMessage fragment) {
		if(fragments==null)
			fragments = new ArrayList<SgipSubmitRequestMessage>();
		
		fragments.add(fragment);
	}

}
