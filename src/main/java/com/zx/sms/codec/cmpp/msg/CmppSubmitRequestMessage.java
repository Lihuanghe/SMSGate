package com.zx.sms.codec.cmpp.msg;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.MsgId;

/**
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppSubmitRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = 1369427662600486133L;
	private MsgId msgid = new MsgId();

	private short registeredDelivery = 0;
	private short msglevel = 9;
	private String serviceId = "cmcczx_sms";
	private short feeUserType = 2;
	private String feeterminalId = GlobalConstance.emptyString;
	private short feeterminaltype = 0;

	private String msgsrc = GlobalConstance.emptyString;
	private String feeType = "01";
	private String feeCode = "000000";
	private String valIdTime = GlobalConstance.emptyString;
	private String atTime =GlobalConstance.emptyString;
	private String srcId = GlobalConstance.emptyString;
	private short destUsrtl = 0;
	private String[] destterminalId = GlobalConstance.emptyStringArray;
	private short destterminaltype = 0;

	private String linkID = GlobalConstance.emptyString;

	private String reserve = GlobalConstance.emptyString;

	private SmsMessage msg;
	
	private boolean supportLongMsg =  true;
	
	public CmppSubmitRequestMessage(Header header) {
		super(CmppPacketType.CMPPSUBMITREQUEST, header);
	}

	public CmppSubmitRequestMessage() {
		super(CmppPacketType.CMPPSUBMITREQUEST);
	}

	/**
	 * @return the msgid
	 */
	public MsgId getMsgid() {
		return msgid;
	}

	/**
	 * @param msgid
	 *            the msgid to set
	 */
	public void setMsgid(MsgId msgid) {
		this.msgid = msgid;
	}



	/**
	 * @return the registeredDelivery
	 */
	public short getRegisteredDelivery() {
		return registeredDelivery;
	}

	/**
	 * @param registeredDelivery
	 *            the registeredDelivery to set
	 */
	public void setRegisteredDelivery(short registeredDelivery) {
		this.registeredDelivery = registeredDelivery;
	}

	/**
	 * @return the msglevel
	 */
	public short getMsglevel() {
		return msglevel;
	}

	/**
	 * @param msglevel
	 *            the msglevel to set
	 */
	public void setMsglevel(short msglevel) {
		this.msglevel = msglevel;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * @param serviceId
	 *            the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * @return the feeUserType
	 */
	public short getFeeUserType() {
		return feeUserType;
	}

	/**
	 * @param feeUserType
	 *            the feeUserType to set
	 */
	public void setFeeUserType(short feeUserType) {
		this.feeUserType = feeUserType;
	}

	/**
	 * @return the feeterminalId
	 */
	public String getFeeterminalId() {
		return feeterminalId;
	}

	/**
	 * @param feeterminalId
	 *            the feeterminalId to set
	 */
	public void setFeeterminalId(String feeterminalId) {
		this.feeterminalId = feeterminalId;
	}

	/**
	 * @return the feeterminaltype
	 */
	public short getFeeterminaltype() {
		return feeterminaltype;
	}

	/**
	 * @param feeterminaltype
	 *            the feeterminaltype to set
	 */
	public void setFeeterminaltype(short feeterminaltype) {
		this.feeterminaltype = feeterminaltype;
	}

	/**
	 * @return the msgsrc
	 */
	public String getMsgsrc() {
		return msgsrc;
	}

	/**
	 * @param msgsrc
	 *            the msgsrc to set
	 */
	public void setMsgsrc(String msgsrc) {
		this.msgsrc = msgsrc;
	}

	/**
	 * @return the feeType
	 */
	public String getFeeType() {
		return feeType;
	}

	/**
	 * @param feeType
	 *            the feeType to set
	 */
	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	/**
	 * @return the feeCode
	 */
	public String getFeeCode() {
		return feeCode;
	}

	/**
	 * @param feeCode
	 *            the feeCode to set
	 */
	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}

	/**
	 * @return the valIdTime
	 */
	public String getValIdTime() {
		return valIdTime;
	}

	/**
	 * @param valIdTime
	 *            the valIdTime to set
	 */
	public void setValIdTime(String valIdTime) {
		this.valIdTime = valIdTime;
	}

	/**
	 * @return the atTime
	 */
	public String getAtTime() {
		return atTime;
	}

	/**
	 * @param atTime
	 *            the atTime to set
	 */
	public void setAtTime(String atTime) {
		this.atTime = atTime;
	}

	/**
	 * @return the srcId
	 */
	public String getSrcId() {
		return srcId;
	}

	/**
	 * @param srcId
	 *            the srcId to set
	 */
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	/**
	 * @return the destUsrtl
	 */
	public short getDestUsrtl() {
		return destUsrtl;
	}

	/**
	 * @param destUsrtl
	 *            the destUsrtl to set
	 */
	public void setDestUsrtl(short destUsrtl) {
		this.destUsrtl = destUsrtl;
	}

	/**
	 * @return the destterminalId
	 */
	public String[] getDestterminalId() {
		return destterminalId;
	}

	/**
	 * @param destterminalId
	 *            the destterminalId to set
	 */
	public void setDestterminalId(String[] destterminalId) {
		this.destterminalId = destterminalId;
		this.destUsrtl = (short)destterminalId.length;
	}
	
	public void setDestterminalId(String destterminalId) {
		this.destterminalId = new String[]{destterminalId};
		this.destUsrtl = (short)1;
	}

	/**
	 * @return the destterminaltype
	 */
	public short getDestterminaltype() {
		return destterminaltype;
	}

	/**
	 * @param destterminaltype
	 *            the destterminaltype to set
	 */
	public void setDestterminaltype(short destterminaltype) {
		this.destterminaltype = destterminaltype;
	}

	/**
	 * @return the linkID
	 */
	public String getLinkID() {
		return linkID;
	}

	/**
	 * @param linkID
	 *            the linkID to set
	 */
	public void setLinkID(String linkID) {
		this.linkID = linkID;
	}

	/**
	 * @return the reserve
	 */
	public String getReserve() {
		return reserve;
	}

	/**
	 * @param reserve
	 *            the reserve to set
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
		return "";
	}
	public SmsMessage getMsg() {
		return msg;
	}
	/**
	 * @return the msgContent
	 */
	public void setMsgContent(String msgContent) {
		this.msg = CMPPCommonUtil.buildTextMessage(msgContent);
	}
	
	public void setMsgContent(SmsMessage msg){
		this.msg = msg;
	}

	public boolean isSupportLongMsg() {
		return supportLongMsg;
	}

	public void setSupportLongMsg(boolean supportLongMsg) {
		this.supportLongMsg = true;
	}

	public static CmppSubmitRequestMessage create(String phone ,String spid,String text){
		CmppSubmitRequestMessage ret = new CmppSubmitRequestMessage();
		ret.setDestterminalId(new String[]{phone});
		ret.setSrcId(spid);
		ret.setMsgContent(text);
		return ret;
	}
	
	public CmppSubmitRequestMessage clone() throws CloneNotSupportedException {
		return   (CmppSubmitRequestMessage) super.clone();
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CmppSubmitRequestMessage [msgid=").append(msgid).append(", destterminalId=").append(Arrays.toString(destterminalId)).append(", msgContent=")
		.append(getMsgContent()).append(", SmsMessageType=").append(msg==null?"":msg.getClass().getSimpleName()).append(", sequenceId=").append(getHeader().getSequenceId()).append("]");
		return sb.toString();
	}
}
