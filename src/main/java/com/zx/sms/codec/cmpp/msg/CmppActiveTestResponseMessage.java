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
public class CmppActiveTestResponseMessage extends DefaultMessage{
	private static final long serialVersionUID = 4300214238350805590L;
	private short reserved = 0;
	public CmppActiveTestResponseMessage(int sequenceId) {
		super(CmppPacketType.CMPPACTIVETESTRESPONSE,sequenceId);
	}
	public CmppActiveTestResponseMessage(Header header) {
		super(CmppPacketType.CMPPACTIVETESTRESPONSE,header);
	}
	public short getReserved() {
		return reserved;
	}
	public void setReserved(short reserved) {
		this.reserved = reserved;
	}

}
