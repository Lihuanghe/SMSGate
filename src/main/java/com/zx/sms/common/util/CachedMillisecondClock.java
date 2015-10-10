package com.zx.sms.common.util;

/**
 * 由于System.currentTimeMillis()性能问题，缓存当前时间，每1s更新一次
 */
public enum CachedMillisecondClock {
	INS;
	private volatile long now = 0;// 当前时间

	private CachedMillisecondClock() {
		this.now = System.currentTimeMillis();
		start();
	}

	private void start() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					now = System.currentTimeMillis();
				}
			}
		},"CachedMillisecondClockUpdater");
		t.setDaemon(true);
		t.start();
	}

	public long now() {
		return now;
	}

}
