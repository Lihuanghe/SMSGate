/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;
import com.zx.sms.common.util.MsgId;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppReportRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = -4631945859346437882L;
	
	private MsgId msgId = new MsgId();
	private String stat = GlobalConstance.emptyString;
	private String submitTime = String.format("%ty%<tm%<td%<tH%<tM", CachedMillisecondClock.INS.now());
	private String doneTime = String.format("%ty%<tm%<td%<tH%<tM", CachedMillisecondClock.INS.now());
	private String destterminalId = GlobalConstance.emptyString;
	private long smscSequence = 0;

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
	 * @return the stat
	 */
	public String getStat() {
		return stat;
	}

	/**
	 * @param stat
	 *            the stat to set
	 */
	public void setStat(String stat) {
		this.stat = stat;
	}

	/**
	 * @return the submitTime
	 */
	public String getSubmitTime() {
		return submitTime;
	}

	/**
	 * @param submitTime
	 *            the submitTime to set
	 */
	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	/**
	 * @return the doneTime
	 */
	public String getDoneTime() {
		return doneTime;
	}

	/**
	 * @param doneTime
	 *            the doneTime to set
	 */
	public void setDoneTime(String doneTime) {
		this.doneTime = doneTime;
	}

	/**
	 * @return the destterminalId
	 */
	public String getDestterminalId() {
		return destterminalId;
	}

	/**
	 * @param destterminalId
	 *            the destterminalId to set
	 */
	public void setDestterminalId(String destterminalId) {
		this.destterminalId = destterminalId;
	}

	/**
	 * @return the smscSequence
	 */
	public long getSmscSequence() {
		return smscSequence;
	}

	/**
	 * @param smscSequence
	 *            the smscSequence to set
	 */
	public void setSmscSequence(long smscSequence) {
		this.smscSequence = smscSequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("CmppReportRequestMessage [msgId=%s, stat=%s, submitTime=%s, doneTime=%s, destterminalId=%s, smscSequence=%s]", msgId, stat,
				submitTime, doneTime, destterminalId, smscSequence);
	}

}
