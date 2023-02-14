/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.SequenceNumber;

/**
 * @author huzorro(huzorro@gmail.com)
 * 
 */
public class SgipReportRequestMessage extends SgipDefaultMessage {
	private static final long serialVersionUID = 4460557848888343195L;
	private SequenceNumber sequenceId ;
	private short reporttype = 0;
	private String usernumber = GlobalConstance.emptyString;
	private short state = 0;
	private short errorcode = 0;
	private String reserve = GlobalConstance.emptyString;

	public SgipReportRequestMessage() {
		super(SgipPacketType.REPORTREQUEST);
	}
	
	public SgipReportRequestMessage(Header header) {
		super(SgipPacketType.REPORTREQUEST,header);
	}


	/**
	 * @return the reporttype
	 */
	public short getReporttype() {
		return reporttype;
	}

	/**
	 * @param reporttype the reporttype to set
	 */
	public void setReporttype(short reporttype) {
		this.reporttype = reporttype;
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
	 * @return the state
	 */
	public short getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(short state) {
		this.state = state;
	}

	/**
	 * @return the errorcode
	 */
	public short getErrorcode() {
		return errorcode;
	}

	/**
	 * @param errorcode the errorcode to set
	 */
	public void setErrorcode(short errorcode) {
		this.errorcode = errorcode;
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
				.format("SgipReportRequestMessage [ sequenceId=%s, reporttype=%s, usernumber=%s, state=%s, errorcode=%s, reserve=%s, seq=%s, header=%s]",
						sequenceId,reporttype, usernumber, state,
						errorcode, reserve, getSequenceNumber(),getHeader());
	}
	
}
