package com.zx.sms.codec.cmpp.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.marre.sms.SmsDcs;
import org.marre.sms.SmsMessage;
import org.marre.sms.SmsPortAddressedTextMessage;
import org.marre.sms.SmsSimTookitSecurityMessage;
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
 *
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 */
public class CmppSubmitRequestMessage extends DefaultMessage implements LongSMSMessage<CmppSubmitRequestMessage> {
	private static final long serialVersionUID = 1369427662600486133L;
	private MsgId msgid = new MsgId();

	private short registeredDelivery = 0;
	private short msglevel = 9;
	private String serviceId = GlobalConstance.emptyString;
	private short feeUserType = 2;
	private String feeterminalId = GlobalConstance.emptyString;
	private short feeterminaltype = 0;

	private String msgsrc = GlobalConstance.emptyString;
	private String feeType = "01";
	private String feeCode = "000000";
	private String valIdTime = GlobalConstance.emptyString;
	private String atTime = GlobalConstance.emptyString;
	private String srcId = GlobalConstance.emptyString;
	private String[] destterminalId = GlobalConstance.emptyStringArray;
	private short destterminaltype = 0;

	private String linkID = GlobalConstance.emptyString;

	private String reserve = GlobalConstance.emptyString;

	private SmsMessage msg;

	private short pktotal = 1;
	private short pknumber = 1;
	private short tppid = 0;// 0是普通GSM 类型，点到点方式 ,127 :写sim卡
	private short tpudhi = 0; // 0:msgcontent不带协议头。1:带有协议头
	private SmsDcs msgfmt = GlobalConstance.defaultmsgfmt;
	private short msgLength = 140;
	private byte[] msgContentBytes = GlobalConstance.emptyBytes;

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

	public boolean isReport() {
		return false;
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
		return serviceId == null ? "" : serviceId;
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
		return feeterminalId == null ? "" : feeterminalId;
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
		return msgsrc == null ? "" : msgsrc;
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
		return feeType == null ? "" : feeType;
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
		return feeCode == null ? "" : feeCode;
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
		return valIdTime == null ? "" : valIdTime;
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
		return atTime == null ? "" : atTime;
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
		return srcId == null ? "" : srcId;
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
		return (short) this.destterminalId.length;
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
	}

	public void setDestterminalId(String destterminalId) {
		this.destterminalId = new String[] { destterminalId };
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
		return linkID == null ? "" : linkID;
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
		if (msg instanceof SmsMessage) {
			return msg.toString();
		}

		if (msgContentBytes != null && msgContentBytes.length > 0) {
			LongMessageFrame frame = generateFrame();
			return LongMessageFrameHolder.INS.getPartTextMsg(frame);
		}

		return "";
	}

	public SmsMessage getSmsMessage() {
		return msg;
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

	public byte[] getMsgContentBytes() {
		return msgContentBytes;
	}

	public void setMsgContentBytes(byte[] msgContentBytes) {
		this.msgContentBytes = msgContentBytes;
	}

	public short getPktotal() {
		return pktotal;
	}

	public void setPktotal(short pktotal) {
		this.pktotal = pktotal;
	}

	public short getPknumber() {
		return pknumber;
	}

	public void setPknumber(short pknumber) {
		this.pknumber = pknumber;
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

	public void setMsg(SmsMessage msg) {
		this.msg = msg;
	}

	public static CmppSubmitRequestMessage create(String phone, String spid, String text) {
		CmppSubmitRequestMessage ret = new CmppSubmitRequestMessage();
		ret.setDestterminalId(new String[] { phone });
		ret.setSrcId(spid);
		ret.setMsgContent(text);
		return ret;
	}

	public CmppSubmitRequestMessage clone() throws CloneNotSupportedException {
		return (CmppSubmitRequestMessage) super.clone();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CmppSubmitRequestMessage [msgid=").append(msgid).append(", serviceId=").append(serviceId).append(", srcId=").append(srcId)
				.append(", msgsrc=").append(msgsrc).append(", destterminalId=").append(Arrays.toString(destterminalId)).append(", msgContent=")
				.append(getMsgContent()).append(", sequenceId=").append(getHeader().getSequenceId()).append("]");
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
	public CmppSubmitRequestMessage generateMessage(LongMessageFrame frame) throws Exception {
		CmppSubmitRequestMessage requestMessage = this.clone();
		requestMessage.setPknumber(frame.getPknumber());
		requestMessage.setPktotal(frame.getPktotal());
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

	private List<CmppSubmitRequestMessage> fragments = null;

	@Override
	public List<CmppSubmitRequestMessage> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(CmppSubmitRequestMessage fragment) {
		if (fragments == null)
			fragments = new ArrayList<CmppSubmitRequestMessage>();

		fragments.add(fragment);
	}
}
