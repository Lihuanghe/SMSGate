/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import org.marre.sms.SmsMessage;
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
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppDeliverRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = 4851585208067281751L;
	private MsgId msgId = new MsgId();
	private String destId = GlobalConstance.emptyString;
	private String serviceid = GlobalConstance.emptyString;

	private String srcterminalId = GlobalConstance.emptyString;
	private short srcterminalType = 0;
	private short registeredDelivery = 0;

	private CmppReportRequestMessage reportRequestMessage = null;
	private String linkid = GlobalConstance.emptyString;

	private String reserved = GlobalConstance.emptyString;

	private boolean isReport = false;
	
	private SmsMessage msg;

	private boolean supportLongMsg = true;

	public CmppDeliverRequestMessage(Header header) {
		super(CmppPacketType.CMPPDELIVERREQUEST, header);
	}

	public CmppDeliverRequestMessage() {
		super(CmppPacketType.CMPPDELIVERREQUEST);
	}

	/**
	 * @return the msgId
	 */
	public MsgId getMsgId() {
		return msgId;
	}

	/**
	 * @param msgId
	 *            the msgId to set
	 */
	public void setMsgId(MsgId msgId) {
		this.msgId = msgId;
	}

	/**
	 * @return the destId
	 */
	public String getDestId() {
		return destId;
	}

	/**
	 * @param destId
	 *            the destId to set
	 */
	public void setDestId(String destId) {
		this.destId = destId;
	}

	/**
	 * @return the serviceid
	 */
	public String getServiceid() {
		return serviceid;
	}

	/**
	 * @param serviceid
	 *            the serviceid to set
	 */
	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	/**
	 * @return the srcterminalId
	 */
	public String getSrcterminalId() {
		return srcterminalId;
	}

	/**
	 * @param srcterminalId
	 *            the srcterminalId to set
	 */
	public void setSrcterminalId(String srcterminalId) {
		this.srcterminalId = srcterminalId;
	}

	/**
	 * @return the srcterminalType
	 */
	public short getSrcterminalType() {
		return srcterminalType;
	}

	/**
	 * @param srcterminalType
	 *            the srcterminalType to set
	 */
	public void setSrcterminalType(short srcterminalType) {
		this.srcterminalType = srcterminalType;
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
		if (this.registeredDelivery != 0) {
			setReport(true);
			setReportRequestMessage(this.reportRequestMessage);
		}
	}

	/**
	 * @return the reserved
	 */
	public String getReserved() {
		return reserved;
	}

	/**
	 * @param reserved
	 *            the reserved to set
	 */
	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	/**
	 * @return the reportRequestMessage
	 */
	public CmppReportRequestMessage getReportRequestMessage() {
		return reportRequestMessage;
	}

	/**
	 * @param reportRequestMessage
	 *            the reportRequestMessage to set
	 */
	public void setReportRequestMessage(CmppReportRequestMessage reportRequestMessage) {
		this.reportRequestMessage = reportRequestMessage;
	}

	/**
	 * @return the linkid
	 */
	public String getLinkid() {
		return linkid;
	}

	/**
	 * @param linkid
	 *            the linkid to set
	 */
	public void setLinkid(String linkid) {
		this.linkid = linkid;
	}

	/**
	 * @return the isReport
	 */
	public boolean isReport() {
		return isReport;
	}

	/**
	 * @param isReport
	 *            the isReport to set
	 */
	public void setReport(boolean isReport) {
		this.isReport = isReport;
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

	public boolean isSupportLongMsg() {
		return supportLongMsg;
	}

	public void setSupportLongMsg(boolean supportLongMsg) {
		this.supportLongMsg = true;
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

	public SmsMessage getMsg() {
		return msg;
	}

	public CmppDeliverRequestMessage clone() throws CloneNotSupportedException {
		return (CmppDeliverRequestMessage) super.clone();
	}

	@Override
	public String toString() {
		if (isReport()) {
			StringBuilder sb = new StringBuilder();
			sb.append("CmppDeliverRequestMessage [msgId=").append(msgId ).append( ", destId=" ).append( destId ).append( ", srcterminalId=" ).append( srcterminalId ).append( ", getHeader()=" ).append( getHeader() ).append( ", ReportRequest=" ).append( getReportRequestMessage() ).append( "]");
			return sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("CmppDeliverRequestMessage [msgId=").append(msgId).append(", destId=").append(destId).append(", srcterminalId=").append(srcterminalId)
				.append(", msgContent=").append(getMsgContent()).append(", SmsMessageType=").append(msg==null?"":msg.getClass().getSimpleName()).append(", sequenceId=").append(getHeader().getSequenceId()).append("]");
		return sb.toString();
	}
}
