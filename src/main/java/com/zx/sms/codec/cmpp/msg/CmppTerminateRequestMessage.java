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
public class CmppTerminateRequestMessage extends DefaultMessage{
	private static final long serialVersionUID = 814288661389104951L;
	
	public CmppTerminateRequestMessage(Header header) {
		super(CmppPacketType.CMPPTERMINATEREQUEST, header);
	}
	public CmppTerminateRequestMessage() {
		super(CmppPacketType.CMPPTERMINATEREQUEST);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("CmppTerminateRequestMessage [toString()=%s]",
				super.toString());
	}
	
}
