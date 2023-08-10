/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;

import org.apache.commons.lang3.StringUtils;

import com.zx.sms.codec.cmpp.msg.Header;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.common.GlobalConstance;

/**
 * @author 
 *
 */
public class SgipTraceResponseMessage extends SgipDefaultMessage {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3444842126905019887L;
    private SgipTraceInfo[] traceInfos;
	
	public SgipTraceResponseMessage() {
		super(SgipPacketType.TRACERESPONSE);
	}
	public SgipTraceResponseMessage(Header header) {
		super(SgipPacketType.TRACERESPONSE,header);
	}

	
	public int getCount() {
		return traceInfos == null ? 0 : traceInfos.length;
	}

	public SgipTraceInfo[] getTraceInfos() {
		return traceInfos;
	}
	public void setTraceInfos(SgipTraceInfo[] traceInfos) {
		this.traceInfos = traceInfos;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("SgipTraceResponseMessage [count=%s, traceInfos=%s,  seq=%s, header=%s]",
						getCount(), StringUtils.join(traceInfos, "|") , getSequenceNumber(), getHeader());
	}
	
}
