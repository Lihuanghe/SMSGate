/**
 * 
 */
package com.zx.sms.codec.sgip12.msg;


import java.io.Serializable;

import com.zx.sms.common.GlobalConstance;

/**
 * @author 
 *
 */
public class SgipTraceInfo implements Serializable,Cloneable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4559834900229287275L;
	private int result = 0;
	private String nodeId = GlobalConstance.emptyString;
	private String receiveTime = GlobalConstance.emptyString;
	private String sendTime = GlobalConstance.emptyString;
	private String reserve = GlobalConstance.emptyString;
	

	
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public String getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
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
				.format("SgipTraceInfo [result=%s, nodeId=%s, receiveTime=%s, sendTime=%s, reserve=%s]",
						result, nodeId,receiveTime,sendTime, reserve);
	}
	
}
