/**
 * 
 */
package com.zx.sms.common.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.math.RandomUtils;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public class DefaultSequenceNumberUtil {
	public static byte[] sequenceN2Bytes(SequenceNumber sn) {
		byte[] bytes = new byte[12];
		long t = Long.parseLong(String.format("%1$02d%2$02d%3$02d%4$02d%5$02d", sn.getMonth(), sn.getDay(), sn.getHour(), sn.getMinutes(), sn.getSeconds()));
		ByteBuffer.wrap(bytes).putInt((int) sn.getNodeIds()).putInt((int) t).putInt((int) sn.getSequenceId());
		return bytes;

	}

	public static SequenceNumber bytes2SequenceN(byte[] bytes) {
		long nodeIds = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0, 4)).getInt() & 0xFFFFFFFFL;
		String t = String.format("%1$010d", ByteBuffer.wrap(Arrays.copyOfRange(bytes, 4, 8)).getInt() & 0xFFFFFFFFL);
		long sequenceId = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 8, 12)).getInt() & 0xFFFFFFFFL;
		SequenceNumber sn = new SequenceNumber();
		sn.setNodeIds(nodeIds);
		sn.setMonth(Integer.parseInt(t.substring(0, 2)));
		sn.setDay(Integer.parseInt(t.substring(2, 4)));
		sn.setHour(Integer.parseInt(t.substring(4, 6)));
		sn.setMinutes(Integer.parseInt(t.substring(6, 8)));
		sn.setSeconds(Integer.parseInt(t.substring(8, 10)));
		sn.setSequenceId(sequenceId);
		return sn;
	}

	public static long getSequenceNo() {
		return getNextAtomicValue(sequenceId,Limited);
	}
	
	/**
	 *实现AtomicLong对象的线程安全循环自增
	 *@param  atomicObj
	 *需要自增的Atomic对象
	 *@param limited
	 *最大值
	 */
	public static long getNextAtomicValue(AtomicLong atomicObj,long limited){
		long ret = atomicObj.getAndIncrement();

		if (ret > limited) { // Long.MAX_VALUE - 0xfff
			synchronized (atomicObj) {
				//双重判断，只能有一个线程更新值
				if (atomicObj.get() > limited) {
					atomicObj.set(0);
					return 0;
				} else {
					return atomicObj.getAndIncrement();
				}
			}
		} else {
			return ret;
		}
	}

	private final static long Limited = 0x7fffffffffff0000L;
	private final static AtomicLong sequenceId = new AtomicLong(Math.abs(RandomUtils.nextInt()));
}
