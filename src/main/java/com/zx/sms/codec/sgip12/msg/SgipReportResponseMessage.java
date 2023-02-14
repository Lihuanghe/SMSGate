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
public class SgipReportResponseMessage extends SgipDefaultMessage {
	private static final long serialVersionUID = 1530100000061666385L;
	
	private short result = 0;
	private String reserve = GlobalConstance.emptyString;
	
	public SgipReportResponseMessage() {
		super(SgipPacketType.REPORTRESPONSE);
	}
	public SgipReportResponseMessage(Header header) {
		super(SgipPacketType.REPORTRESPONSE,header);
	}
	/**
	 * @return the result
	 */
	public short getResult() {
		return result;
	}
	/**
	 * @param result the result to set
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
	 * @param reserve the reserve to set
	 */
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("SgipReportResponseMessage [result=%s, reserve=%s,  seq=%s, header=%s]",
						result, reserve, getSequenceNumber(), getHeader());
	}
	
}
