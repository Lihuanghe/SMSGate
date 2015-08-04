/**
 * 
 */
package com.zx.sms.common.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class SequenceNumber implements Serializable{
	private static final long serialVersionUID = 650229326111998772L;
	private long nodeIds;
	private int month;
	private int day;
	private int hour;
	private int minutes;
	private int seconds;
	private long sequenceId;
	private final static AtomicLong atomicLong = new AtomicLong();
	public SequenceNumber() {
		this(CachedMillisecondClock.INS.now());
	}
	
	/**
	 * 
	 * @param nodeIds
	 * @param timeMillis
	 */
	public SequenceNumber(long nodeIds, long timeMillis) {
		this(timeMillis, nodeIds, (atomicLong.compareAndSet(Integer.MAX_VALUE, 0)
				? atomicLong.getAndIncrement()
				: atomicLong.getAndIncrement()));
	}
	/**
	 * 
	 * @param timeMillis
	 */
	public SequenceNumber(long timeMillis) {
		this(timeMillis, 1010, (atomicLong.compareAndSet(Short.MAX_VALUE, 0)
				? atomicLong.getAndIncrement()
				: atomicLong.getAndIncrement()));
	}
	/**
	 * 
	 * @param msgIds
	 */
	public SequenceNumber(String msgIds) {
		setNodeIds(Long.parseLong(msgIds.substring(0, 10)));
		setMonth(Integer.parseInt(msgIds.substring(10, 12)));
		setDay(Integer.parseInt(msgIds.substring(12, 14)));
		setHour(Integer.parseInt(msgIds.substring(14, 16)));
		setMinutes(Integer.parseInt(msgIds.substring(16, 18)));
		setSeconds(Integer.parseInt(msgIds.substring(18, 20)));		
		setSequenceId(Long.parseLong(msgIds.substring(20, 30)));
	}	
	/**
	 * 
	 * @param timeMillis
	 * @param gateId
	 * @param sequenceId
	 */
	public SequenceNumber(long timeMillis, long nodeIds, long sequenceId) {
		setMonth(Integer.parseInt(String.format("%tm", timeMillis)));
		setDay(Integer.parseInt(String.format("%td", timeMillis)));
		setHour(Integer.parseInt(String.format("%tH", timeMillis)));
		setMinutes(Integer.parseInt(String.format("%tM", timeMillis)));
		setSeconds(Integer.parseInt(String.format("%tS", timeMillis)));
		setNodeIds(nodeIds);
		setSequenceId(sequenceId);
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
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}
	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}
	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}
	/**
	 * @param day the day to set
	 */
	public void setDay(int day) {
		this.day = day;
	}
	/**
	 * @return the hour
	 */
	public int getHour() {
		return hour;
	}
	/**
	 * @param hour the hour to set
	 */
	public void setHour(int hour) {
		this.hour = hour;
	}
	/**
	 * @return the minutes
	 */
	public int getMinutes() {
		return minutes;
	}
	/**
	 * @param minutes the minutes to set
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	/**
	 * @return the seconds
	 */
	public int getSeconds() {
		return seconds;
	}
	/**
	 * @param seconds the seconds to set
	 */
	public void setSeconds(int seconds) {
		this.seconds = seconds;
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("%1$010d%2$02d%3$02d%4$02d%5$02d%6$02d%7$010d",
						nodeIds, month, day, hour, minutes, seconds, sequenceId);
	}	
	
}
