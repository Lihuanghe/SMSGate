/**
 * 
 */
package com.zx.sms.codec.smgp.msg;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import com.zx.sms.codec.smgp.util.SMGPMsgIdUtil;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.DefaultSequenceNumberUtil;

/**
 * 
 *
 */
public class MsgId implements Serializable {
	private static final long serialVersionUID = 945466149547731811L;
	private static int ProcessID = 1010;
	private int month;
	private int day;
	private int hour;
	private int minutes;
	private int gateId;
	private int sequenceId;
	private byte[] originarr;
	
	static{
		String vmName = ManagementFactory.getRuntimeMXBean().getName();
		if(vmName.contains("@")){
			try{
				ProcessID = Integer.parseInt(vmName.split("@")[0]);
			}catch(Exception e){
				
			}
		}
	}
	
	public MsgId() {
		this(CachedMillisecondClock.INS.now());
	}
	/**
	 * 
	 * @param gateId
	 */
	public MsgId(int gateId) {
		this(CachedMillisecondClock.INS.now(), gateId, (int)DefaultSequenceNumberUtil.getSequenceNo());
	}
	/**
	 * 
	 * @param timeMillis
	 */
	public MsgId(long timeMillis) {
		
		this(timeMillis, ProcessID, (int)DefaultSequenceNumberUtil.getSequenceNo());
	}
	
	public MsgId(byte[] arr) {
		originarr = new byte[10];
		System.arraycopy(arr, 0, originarr, 0, 10);
	}
	
	public MsgId(String msgIds) {
		setGateId(Integer.parseInt(msgIds.substring(0, 6)));
		setMonth(Integer.parseInt(msgIds.substring(6, 8)));
		setDay(Integer.parseInt(msgIds.substring(8, 10)));
		setHour(Integer.parseInt(msgIds.substring(10, 12)));
		setMinutes(Integer.parseInt(msgIds.substring(12, 14)));
		setSequenceId(Integer.parseInt(msgIds.substring(14, 20)));
	}
	/**
	 * 
	 * @param timeMillis
	 * @param gateId
	 * @param sequenceId
	 */
	public MsgId(long timeMillis, int gateId, int sequenceId) {
		setMonth(Integer.parseInt(String.format("%tm", timeMillis)));
		setDay(Integer.parseInt(String.format("%td", timeMillis)));
		setHour(Integer.parseInt(String.format("%tH", timeMillis)));
		setMinutes(Integer.parseInt(String.format("%tM", timeMillis)));
		setGateId(gateId);
		setSequenceId(sequenceId);
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
	 * @return the gateId
	 */
	public int getGateId() {
		return gateId;
	}
	/**
	 * @param gateId the gateId to set
	 */
	public void setGateId(int gateId) {
		this.gateId = gateId;
	}
	/**
	 * @return the sequenceId
	 */
	public int getSequenceId() {
		return sequenceId & 0xffff;
	}
	/**
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(int sequenceId) {
		this.sequenceId = (sequenceId & 0xffff)%1000000;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	
	public String toString() {
		if(originarr!=null && originarr.length>0) {
			return String.valueOf(Hex.encodeHex(originarr));
		}else {
			return String
					.format("%1$06d%2$02d%3$02d%4$02d%5$02d%6$06d",
							gateId,month, day, hour, minutes, sequenceId);
		}

	}
	
	public String toHexString(boolean toLowerCase) {
		return Hex.encodeHexString(SMGPMsgIdUtil.msgId2Bytes(this), toLowerCase);
	}
	
	@Override
	public int hashCode() {
	
		
		final int prime = 31;
		int result = 1;
		if(originarr!=null && originarr.length>0) {
			return Arrays.hashCode(originarr);
		}
		result = prime * result + day;
		result = prime * result + gateId;
		result = prime * result + hour;
		result = prime * result + minutes;
		result = prime * result + month;
		result = prime * result + sequenceId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MsgId other = (MsgId) obj;
		
		if(originarr!=null && originarr.length>0) {
			return Arrays.equals(originarr, other.originarr);
		}
		
		if (day != other.day)
			return false;
		if (gateId != other.gateId)
			return false;
		if (hour != other.hour)
			return false;
		if (minutes != other.minutes)
			return false;
		if (month != other.month)
			return false;
		if (sequenceId != other.sequenceId)
			return false;
		return true;
	}
	
	
	
}
