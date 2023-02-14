package com.zx.sms.common;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicBitSet {
	private final AtomicIntegerArray array;

	public AtomicBitSet(int length) {
		int intLength = (length + 31) >>> 5; // unsigned / 32
		array = new AtomicIntegerArray(intLength);
	}

	public void set(long n) {
		int bit = 1 << n;
		int idx = (int) (n >>> 5);
		while (true) {
			int num = array.get(idx);
			int num2 = num | bit;
			if (num == num2 || array.compareAndSet(idx, num, num2))
				return;
		}
	}

	public boolean get(long n) {
		int bit = 1 << n;
		int idx = (int) (n >>> 5);
		int num = array.get(idx);
		return (num & bit) != 0;
	}
	
	public synchronized int cardinality() {
		int sum = 0;
		for(int i = 0;i<array.length();i++) {
			sum += Integer.bitCount(array.get(i));
		}
		return sum;
	}
}
