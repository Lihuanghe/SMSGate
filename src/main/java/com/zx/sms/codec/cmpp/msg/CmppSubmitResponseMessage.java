/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.util.MsgId;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppSubmitResponseMessage extends DefaultMessage {
	private static final long serialVersionUID = -6806940736604019528L;
	private MsgId msgId = new MsgId();
	private long result = 0;

	public CmppSubmitResponseMessage(int sequenceId) {
		super(CmppPacketType.CMPPSUBMITRESPONSE, sequenceId);
	}

	public CmppSubmitResponseMessage(Header header) {
		super(CmppPacketType.CMPPSUBMITRESPONSE, header);
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
	 * @return the result
	 */
	public long getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(long result) {
		this.result = result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CmppSubmitResponseMessage [msgId=").append(msgId).append(", result=").append(result).append(", sequenceId=")
				.append(getHeader().getSequenceId()).append("]");
		return sb.toString();
	}

}
