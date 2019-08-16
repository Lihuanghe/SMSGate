/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SgipUnbindResponseMessage extends SgipDefaultMessage {
	private static final long serialVersionUID = 4638514500085975L;
	public SgipUnbindResponseMessage() {
		super(SgipPacketType.UNBINDRESPONSE);
	}
	public SgipUnbindResponseMessage(Header header) {
		super(SgipPacketType.UNBINDRESPONSE,header);
	}

}
