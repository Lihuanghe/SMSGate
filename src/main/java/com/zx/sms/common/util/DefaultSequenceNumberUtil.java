/**
 * 
 */
package com.zx.sms.common.util;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class DefaultSequenceNumberUtil {
	
	public static byte[] sequenceN2Bytes(SequenceNumber sn) {
		byte[] bytes = new byte[12];
		long t = Long.parseLong(sn.getTimeString());
		ByteBuffer.wrap(bytes).putInt((int) sn.getNodeIds()).putInt((int) t).putInt((int) sn.getSequenceId());
		return bytes;

	}
	private static final String[] datePattern = new String[]{"yyyyMMddHHmmss"};
	
	public static SequenceNumber bytes2SequenceN(byte[] bytes) {
		long nodeIds = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0, 4)).getInt() & 0xFFFFFFFFL;
		
		//sgip协议里时间不带年份信息，这里判断下年份信息
		String year = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyyy");
		String t =year + StringUtils.leftPad(String.valueOf(ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4, 8)).getInt()), 10, '0');
		
		Date d ;
		try {
			d = DateUtils.parseDate(t, datePattern);
			//如果正好是年末，这个时间有可能差一年，则必须取上一年
			//这里判断取200天，防止因不同主机时间不同步造成误差
			if(d.getTime() - CachedMillisecondClock.INS.now() > 86400000L * 200){
				d = DateUtils.addYears(d, -1);
			}
		} catch (ParseException e) {
			d = new Date();
			e.printStackTrace();
		}
		
		int sequenceId = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 8, 12)).getInt();
		SequenceNumber sn = new SequenceNumber(d.getTime(),nodeIds,sequenceId);
		return sn;
	}

	public static int getSequenceNo() {
		return sequenceId.incrementAndGet();
	}
	
	private final static AtomicInteger sequenceId = new AtomicInteger(RandomUtils.nextInt());
}
