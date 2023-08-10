/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.SequenceNumber;

/**
 * @author 
 * 
 */
public class SgipTraceRequestMessage extends SgipDefaultMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1162299484825646326L;
	private SequenceNumber sequenceId ;
	private String usernumber = GlobalConstance.emptyString;
	private String reserve = GlobalConstance.emptyString;

	public SgipTraceRequestMessage() {
		super(SgipPacketType.TRACEREQUEST);
	}
	
	public SgipTraceRequestMessage(Header header) {
		super(SgipPacketType.TRACEREQUEST,header);
	}

	/**
	 * @return the usernumber
	 */
	public String getUsernumber() {
		return usernumber;
	}

	/**
	 * @param usernumber the usernumber to set
	 */
	public void setUsernumber(String usernumber) {
		this.usernumber = usernumber;
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
	
	public SequenceNumber getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(SequenceNumber sequenceId) {
		this.sequenceId = sequenceId;
	}

	@Override
	public String toString() {
		return String
				.format("SgipTraceRequestMessage [ sequenceId=%s, usernumber=%s,reserve=%s, seq=%s, header=%s]",
						sequenceId,usernumber, reserve, getSequenceNumber(),getHeader());
	}
	
}
