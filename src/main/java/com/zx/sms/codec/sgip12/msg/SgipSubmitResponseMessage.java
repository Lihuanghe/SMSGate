/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SgipSubmitResponseMessage extends SgipDefaultMessage {
	private static final long serialVersionUID = -6490291019236883524L;

	private short result = 0;
	private String reserve = GlobalConstance.emptyString;

	public SgipSubmitResponseMessage() {
		super(SgipPacketType.SUBMITRESPONSE);
	}

	public SgipSubmitResponseMessage(Header header) {
		super(SgipPacketType.SUBMITRESPONSE, header);
	}

	/**
	 * @return the result
	 */
	public short getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(short result) {
		this.result = result;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("SgipSubmitResponseMessage [result=%s, reserve=%s, seq=%s, header=%s]", result, reserve, getSequenceNumber(),getHeader());
	}

}
