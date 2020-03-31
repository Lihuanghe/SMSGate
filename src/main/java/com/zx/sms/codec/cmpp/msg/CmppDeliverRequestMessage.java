/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

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
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CMPPCommonUtil;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppDeliverRequestMessage extends DefaultMessage implements LongSMSMessage<CmppDeliverRequestMessage> {
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

	private SmsMessage msg;

	private short tppid = 0;// 0是普通GSM 类型，点到点方式 ,127 :写sim卡
	private short tpudhi = 0; // 0:msgcontent不带协议头。1:带有协议头
	private SmsDcs msgfmt = GlobalConstance.defaultmsgfmt;
	private short msgLength = 140;
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;

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
		if (reportRequestMessage != null) {
			this.registeredDelivery = (short) 1;
		}
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
		return this.registeredDelivery != 0;
	}

	public String getMsgContent() {
		if (msg instanceof SmsMessage) {
			return msg.toString();
		}

		if (msgContentBytes != null && msgContentBytes.length > 0) {
			LongMessageFrame frame = generateFrame();
			return LongMessageFrameHolder.INS.getPartTextMsg(frame);
		}

		return "";
	}

	public short getTppid() {
		return tppid;
	}

	public void setTppid(short tppid) {
		this.tppid = tppid;
	}

	public short getTpudhi() {
		return tpudhi;
	}

	public void setTpudhi(short tpudhi) {
		this.tpudhi = tpudhi;
	}

	public SmsDcs getMsgfmt() {
		return msgfmt;
	}

	public void setMsgfmt(SmsDcs msgfmt) {
		this.msgfmt = msgfmt;
	}

	public short getMsgLength() {
		return msgLength;
	}

	public void setMsgLength(short msgLength) {
		this.msgLength = msgLength;
	}

	public byte[] getMsgContentBytes() {
		return msgContentBytes;
	}

	public void setMsgContentBytes(byte[] msgContentBytes) {
		this.msgContentBytes = msgContentBytes;
	}

	public void setMsg(SmsMessage msg) {
		this.msg = msg;
	}

	/**
	 * @return the msgContent
	 */
	public void setMsgContent(String msgContent) {
		setMsgContent(CMPPCommonUtil.buildTextMessage(msgContent));
	}

	public void setMsgContent(SmsMessage msg) {
		this.msg = msg;
	}

	public SmsMessage getSmsMessage() {
		return msg;
	}

	public CmppDeliverRequestMessage clone() throws CloneNotSupportedException {
		return (CmppDeliverRequestMessage) super.clone();
	}

	@Override
	public String toString() {
		if (isReport()) {
			StringBuilder sb = new StringBuilder();
			sb.append("CmppDeliverRequestMessage [msgId=").append(msgId).append(", destId=").append(destId).append(", srcterminalId=").append(srcterminalId)
					.append(", getHeader()=").append(getHeader()).append(", ReportRequest=").append(getReportRequestMessage()).append("]");
			return sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("CmppDeliverRequestMessage [msgId=").append(msgId).append(", destId=").append(destId).append(", srcterminalId=").append(srcterminalId)
				.append(", msgContent=").append(getMsgContent()).append(", sequenceId=").append(getHeader().getSequenceId()).append("]");
		return sb.toString();
	}

	@Override
	public LongMessageFrame generateFrame() {
		LongMessageFrame frame = new LongMessageFrame();
		frame.setTppid(getTppid());
		frame.setTpudhi(getTpudhi());
		frame.setMsgfmt(getMsgfmt());
		frame.setMsgContentBytes(getMsgContentBytes());
		frame.setMsgLength((short) getMsgLength());
		frame.setSequence(getSequenceNo());
		return frame;
	}

	@Override
	public CmppDeliverRequestMessage generateMessage(LongMessageFrame frame) throws Exception {
		CmppDeliverRequestMessage requestMessage = this.clone();
		requestMessage.setTpudhi(frame.getTpudhi());
		requestMessage.setMsgfmt((SmsDcs)frame.getMsgfmt());
		requestMessage.setMsgContentBytes(frame.getMsgContentBytes());
		requestMessage.setMsgLength((short) frame.getMsgLength());

		if (frame.getPknumber() != 1) {
			requestMessage.getHeader().setSequenceId(DefaultSequenceNumberUtil.getSequenceNo());
		}
		requestMessage.setMsg(null);
		return requestMessage;
	}

	private List<CmppDeliverRequestMessage> fragments = null;

	@Override
	public List<CmppDeliverRequestMessage> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(CmppDeliverRequestMessage fragment) {
		if (fragments == null)
			fragments = new ArrayList<CmppDeliverRequestMessage>();

		fragments.add(fragment);
	}
}
