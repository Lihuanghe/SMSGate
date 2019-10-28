/**
 * 
 */
package com.zx.sms.common.util;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import com.zx.sms.codec.smgp.util.SMGPMsgIdUtil;

/**
 * 
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class MsgId implements Serializable {
	private static final long serialVersionUID = 945466149547731811L;
	private static int ProcessID = 1010;
	private int month;
	private int day;
	private int hour;
	private int minutes;
	private int seconds;
	private int gateId;
	private int sequenceId;
	
	static{
		final String propertiesName = "smsgate.id";
		String value = null;
		//解决在docker中运行，进程号都一样的问题
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(propertiesName);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(propertiesName);
                    }
                });
            }
        } catch (SecurityException e) {
        }
        //没有配置gateid就取程序进程号
		if(StringUtils.isBlank(value)) {
			String vmName = ManagementFactory.getRuntimeMXBean().getName();
			if(vmName.contains("@")){
				value =vmName.split("@")[0];
			}
		}
		
		try{
			ProcessID = Integer.valueOf(value);
		}catch(Exception e){
			
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
	/**
	 * 
	 * @param msgIds
	 */
	public MsgId(String msgIds) {
		setMonth(Integer.parseInt(msgIds.substring(0, 2)));
		setDay(Integer.parseInt(msgIds.substring(2, 4)));
		setHour(Integer.parseInt(msgIds.substring(4, 6)));
		setMinutes(Integer.parseInt(msgIds.substring(6, 8)));
		setSeconds(Integer.parseInt(msgIds.substring(8, 10)));
		setGateId(Integer.parseInt(msgIds.substring(10, 17)));
		setSequenceId(Integer.parseInt(msgIds.substring(17, 22)));
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
		setSeconds(Integer.parseInt(String.format("%tS", timeMillis)));
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
		this.sequenceId = sequenceId & 0xffff;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("%1$02d%2$02d%3$02d%4$02d%5$02d%6$07d%7$05d",
						month, day, hour, minutes, seconds, gateId, sequenceId);
	}
	
	public String toHexString(boolean toLowerCase) {
		return Hex.encodeHexString(DefaultMsgIdUtil.msgId2Bytes(this), toLowerCase);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + day;
		result = prime * result + gateId;
		result = prime * result + hour;
		result = prime * result + minutes;
		result = prime * result + month;
		result = prime * result + seconds;
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
		if (seconds != other.seconds)
			return false;
		if (sequenceId != other.sequenceId)
			return false;
		return true;
	}
	
	
	
}
