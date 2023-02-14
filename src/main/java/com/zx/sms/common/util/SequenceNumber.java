/**
 * 
 */
package com.zx.sms.common.util;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SequenceNumber implements Serializable{
	private static final long serialVersionUID = 650229326111998772L;
	private static final String[] datePattern = new String[]{"yyyyMMddHHmmss"};
	private long nodeIds;
	private int sequenceId;
	private long timestamp;
	
	public SequenceNumber() {
		this(CachedMillisecondClock.INS.now());
	}
	
	/**
	 * 
	 * @param nodeIds
	 * @param timeMillis
	 */
	public SequenceNumber(long nodeIds, long timeMillis) {
		this(timeMillis, nodeIds, DefaultSequenceNumberUtil.getSequenceNo());
	}
	/**
	 * 
	 * @param timeMillis
	 */
	public SequenceNumber(long timeMillis) {
		this(timeMillis, 1010,  DefaultSequenceNumberUtil.getSequenceNo());
	}
	
	
	public SequenceNumber(String  str_sequenceId) {
		setNodeIds(Long.parseLong(str_sequenceId.substring(0, 10)));
		setTimestamp(DefaultSequenceNumberUtil.getTimestampFromMonthDayTime(str_sequenceId.substring(10, 20)));		
		setSequenceId(Integer.parseInt(str_sequenceId.substring(20, 30)));
	}
	/**
	 * 
	 * @param msgIds
	 */
	public SequenceNumber(MsgId msgIds) {
		String strmsgid = msgIds.toString();
		setNodeIds(msgIds.getGateId());
		setTimestamp(DefaultSequenceNumberUtil.getTimestampFromMonthDayTime(strmsgid.substring(0, 10)));		
		setSequenceId(msgIds.getSequenceId());
	}	
	/**
	 * 
	 * @param timeMillis
	 * @param nodeIds
	 * @param sequenceId
	 */
	public SequenceNumber(long timeMillis, long nodeIds, int sequenceId) {
		setNodeIds(nodeIds);
		setSequenceId(sequenceId);
		setTimestamp(timeMillis);
	}
	/**
	 * @return the nodeIds
	 */
	public long getNodeIds() {
		return nodeIds;
	}
	/**
	 * @param nodeIds the nodeIds to set
	 */
	public void setNodeIds(long nodeIds) {
		this.nodeIds = nodeIds;
	}

	/**
	 * @return the sequenceId
	 */
	public int getSequenceId() {
		return sequenceId;
	}
	/**
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public String getTimeString() {
		
		return DateFormatUtils.format(timestamp, "MMddHHmmss");
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.leftPad(String.valueOf(nodeIds), 10,'0'))
		.append(getTimeString())
		.append(StringUtils.leftPad(String.valueOf(sequenceId), 10,'0'));
		return sb.toString();
	}	
	
}
