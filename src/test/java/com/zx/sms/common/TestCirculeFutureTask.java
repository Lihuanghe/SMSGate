package com.zx.sms.common;

import io.netty.util.concurrent.Future;

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;

import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;

public class TestCirculeFutureTask {
	int cnt = 0;
	
	private void throwsexp(){
		System.out.println("==" + System.currentTimeMillis());
		int s = 3/1;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testexp(){
		EventLoopGroupFactory.INS.getBusiWork().scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				
				throwsexp();
				
			}
		}, 2,1, TimeUnit.SECONDS);
		
		EventLoopGroupFactory.INS.getBusiWork().scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				
				System.out.println(DateFormatUtils.format(Calendar.getInstance(), "MMdd"));
				
			}
		}, 2,2, TimeUnit.SECONDS);
		
	}
	
	@Test
	public void test() {
		final Thread th = Thread.currentThread();
		EventLoopGroupFactory.INS.submitUnlimitCircleTask( new Callable<Integer>() {
			private long lastime = 0;
			@Override
			public Integer call() throws Exception {
				cnt++;
				long now = System.nanoTime();
				System.out.println(now - lastime);
				lastime = now;
				return cnt;
			}
		}, new ExitUnlimitCirclePolicy() {

			@Override
			public boolean notOver(Future future) {
				try {
					boolean ret =  (Integer)future.get() < 100;
					if(!ret){
						LockSupport.unpark(th);
					}
					return ret;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LockSupport.unpark(th);
				return false;
			}
		},0);
		System.out.println(ManagementFactory.getRuntimeMXBean().getName());
		

	}

}
