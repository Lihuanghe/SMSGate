/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppActiveTestRequestMessage extends DefaultMessage {
	private static final long serialVersionUID = 4496674961657465872L;
	
	public CmppActiveTestRequestMessage() {
		super(CmppPacketType.CMPPACTIVETESTREQUEST);
	}
	public CmppActiveTestRequestMessage(Header header) {
		super(CmppPacketType.CMPPACTIVETESTREQUEST,header);
	}
}
