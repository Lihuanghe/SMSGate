/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import java.util.ArrayList;
import java.util.List;

import org.marre.sms.SmsAlphabet;
import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;
import org.marre.sms.SmsMsgClass;
import org.marre.sms.SmsPortAddressedTextMessage;
import org.marre.sms.SmsTextMessage;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.marre.wap.push.SmsWapPushMessage;
import org.marre.wap.push.WapSIPush;
import org.marre.wap.push.WapSLPush;
import org.marre.wap.wbxml.WbxmlDocument;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.msg.DefaultMessage;
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
public class SgipDeliverRequestMessage extends DefaultMessage implements LongSMSMessage<SgipDeliverRequestMessage>{
	private static final long serialVersionUID = -605827022369453415L;

	private String usernumber = GlobalConstance.emptyString;
	private String spnumber = GlobalConstance.emptyString;
	private short tppid = 0;
	private short tpudhi = 0;
	private SmsDcs msgfmt = GlobalConstance.defaultmsgfmt;
	private int messagelength = 120;
	private String reserve = GlobalConstance.emptyString;
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;
	private SmsMessage msg;

	public SgipDeliverRequestMessage() {
		super(SgipPacketType.DELIVERREQUEST);
	}
	
	public SgipDeliverRequestMessage(Header header) {
		super(SgipPacketType.DELIVERREQUEST,header);
	}
	
	public SmsDcs getMsgfmt() {
		return msgfmt;
	}

	public void setMsgfmt(SmsDcs msgfmt) {
		this.msgfmt = msgfmt;
	}
	/**
	 * @return the usernumber
	 */
	public String getUsernumber() {
		return usernumber;
	}

	/**
	 * @param usernumber the usernumber to set
	 */
	public void setUsernumber(String usernumber) {
		this.usernumber = usernumber;
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
	
	public boolean isReport() {
		return false;
	}

	/**
	 * @param reserve the reserve to set
	 */
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
	
	public String getMsgContent() {
		if(msg instanceof SmsTextMessage){
			SmsTextMessage textMsg = (SmsTextMessage) msg;
			return textMsg.getText();
		}else if(msg instanceof SmsPortAddressedTextMessage){
			SmsPortAddressedTextMessage textMsg = (SmsPortAddressedTextMessage) msg;
			return textMsg.getText();
		}else if(msg instanceof SmsMmsNotificationMessage){
			SmsMmsNotificationMessage mms = (SmsMmsNotificationMessage) msg;
			return mms.getContentLocation_();
		}else if(msg instanceof SmsWapPushMessage){
			SmsWapPushMessage wap = (SmsWapPushMessage) msg;
			WbxmlDocument wbxml = wap.getWbxml();
			if(wbxml instanceof WapSIPush){
				return ((WapSIPush)wbxml).getUri();
			}else if(wbxml instanceof WapSLPush){
				return ((WapSLPush)wbxml).getUri();
			}
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SgipDeliverRequestMessage [").append("destId=").append(usernumber).append(", srcterminalId=").append(spnumber)
				.append(", msgContent=").append(getMsgContent()).append(", sequenceId=").append(getHeader().getSequenceId()).append("]");
		return sb.toString();
	}
	public SgipDeliverRequestMessage clone() throws CloneNotSupportedException {
		return (SgipDeliverRequestMessage) super.clone();
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
	public SgipDeliverRequestMessage generateMessage(LongMessageFrame frame) throws Exception {
		SgipDeliverRequestMessage requestMessage = this.clone();
		
		requestMessage.setTppid(frame.getTppid());
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
	private List<SgipDeliverRequestMessage> fragments = null;
	
	@Override
	public List<SgipDeliverRequestMessage> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(SgipDeliverRequestMessage fragment) {
		if(fragments==null)
			fragments = new ArrayList<SgipDeliverRequestMessage>();
		
		fragments.add(fragment);
	}
	
}
