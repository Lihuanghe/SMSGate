package com.zx.sms.common;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class TestNettyEventLoopTaskCancel {

	private NioEventLoopGroup ins = new NioEventLoopGroup(1);
	@Test
	public void testcancel() throws Exception
	{
		System.out.println(ByteBufUtil.hexDump(new byte[]{10,11,12,13,14,15,127}));
		String str = "0a0b";
		
		byte[] b =	Hex.decodeHex(str.toCharArray());
		
		
		Future future = ins.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				int i=0;
					
					System.out.println("====="+i++);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
		},0,3,TimeUnit.SECONDS);
		System.out.println(future.getClass());
		try {
			Thread.sleep(10000);
			future.cancel(true);
			System.out.println("-----End------");
			Thread.sleep(10000);
			ins.shutdownGracefully().syncUninterruptibly();
//			ins.close();
			System.out.println("-----close------");
			LockSupport.park();
			System.out.println("-----finished------");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
