/**
 * 
 */
package com.zx.sms.common.util;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SequenceNumber implements Serializable{
	private static final long serialVersionUID = 650229326111998772L;
	private static final String[] datePattern = new String[]{"yyyyMMddHHmmss"};
	private long nodeIds;
	private long sequenceId;
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
	/**
	 * 
	 * @param msgIds
	 */
	public SequenceNumber(MsgId msgIds) {
		String strmsgid = msgIds.toString();
		setNodeIds(msgIds.getGateId());
		//sgip协议里时间不带年份信息，这里判断下年份信息
		String year = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyyy");
		String t = String.format("%1$s%2$s",year, strmsgid.substring(0, 10));
		
		Date d;
		try {
			d = DateUtils.parseDate(t, datePattern);
			//如果正好是年末，这个时间有可能差一年，则必须取上一年
			//这里判断取200天，防止因不同主机时间不同步造成误差
			if(d.getTime() - CachedMillisecondClock.INS.now() > 86400000L * 200){
				d = DateUtils.addYears(d, -1);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			d = new Date();
		}
		setTimestamp(d.getTime());		
		setSequenceId(msgIds.getSequenceId());
	}	
	/**
	 * 
	 * @param timeMillis
	 * @param gateId
	 * @param sequenceId
	 */
	public SequenceNumber(long timeMillis, long nodeIds, long sequenceId) {
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
	public long getSequenceId() {
		return sequenceId;
	}
	/**
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(long sequenceId) {
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
		return String
				.format("%1$010d%2$10s%3$010d",
						nodeIds, getTimeString(), sequenceId);
	}	
	
}
