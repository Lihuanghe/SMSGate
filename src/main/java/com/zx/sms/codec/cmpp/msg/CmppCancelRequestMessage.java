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
public class CmppCancelRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = -4633530203133110407L;
	private MsgId msgId = new MsgId();
	
	public CmppCancelRequestMessage(Header header) {
		super(CmppPacketType.CMPPCANCELREQUEST,header);
	}

	public CmppCancelRequestMessage() {
		super(CmppPacketType.CMPPCANCELREQUEST);
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
	
}
