/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import java.util.ArrayList;
import java.util.List;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;
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
	
	private String msgContent;
	
	private boolean supportLongMsg = GlobalConstance.isSupportLongMsg;

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
		return msgContent;
	}

	public boolean isSupportLongMsg() {
		return supportLongMsg;
	}

	public void setSupportLongMsg(boolean supportLongMsg) {
		this.supportLongMsg = supportLongMsg;
	}

	/**
	 * @return the msgContent
	 */
	public void setMsgContent(String msgContent) {
		if (msgContent == null){
			this.msgContent = GlobalConstance.emptyString;
		}else{
			this.msgContent = msgContent;
		}
	}
	
	public CmppDeliverRequestMessage clone() throws CloneNotSupportedException {
		return (CmppDeliverRequestMessage) super.clone();
	}

	@Override
	public String toString() {
		return "CmppDeliverRequestMessage [msgId=" + msgId + ", destId=" + destId + ", serviceid=" + serviceid + ", srcterminalId=" + srcterminalId
				+ ", registeredDelivery=" + registeredDelivery + ", msgContent=" + msgContent + ", getHeader()=" + getHeader() + "]";
	}
	

	
	
}
