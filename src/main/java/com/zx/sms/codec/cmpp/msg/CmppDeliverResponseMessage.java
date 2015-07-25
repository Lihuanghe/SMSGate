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
public class CmppDeliverResponseMessage extends DefaultMessage {
	private static final long serialVersionUID = -8362723084094916290L;
	private MsgId msgId = new MsgId();
	private long result = 0;
	
	public  CmppDeliverResponseMessage(long sequenceId) {
		super(CmppPacketType.CMPPDELIVERRESPONSE,sequenceId);
	}
	public  CmppDeliverResponseMessage(Header header) {
		super(CmppPacketType.CMPPDELIVERRESPONSE,header);
	}
	/**
	 * @return the msgId
	 */
	public MsgId getMsgId() {
		return msgId;
	}

	/**
	 * @param msgId the msgId to set
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
	 * @param result the result to set
	 */
	public void setResult(long result) {
		this.result = result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("CmppDeliverResponseMessage [msgId=%s, result=%s, getPacketType()=%s, getTimestamp()=%s, getChannelIds()=%s, getChildChannelIds()=%s, getHeader()=%s]",
						msgId, result, getPacketType(), getTimestamp(),
						getChannelIds(), getChildChannelIds(), getHeader());
	}


	
}
