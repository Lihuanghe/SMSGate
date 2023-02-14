package com.zx.sms.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

public class TestNettyEventLoopTaskCancel {

	private NioEventLoopGroup ins = new NioEventLoopGroup(1);
	@Test
	public void testcancel() throws Exception
	{
		System.out.println(ByteBufUtil.hexDump(new byte[]{10,11,12,13,14,15,127}));
		String str = "0a0b";
		
		byte[] b =	Hex.decodeHex(str.toCharArray());
		
		final AtomicInteger i = new AtomicInteger();
		Future future = ins.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				
					
					System.out.println("====="+ i.incrementAndGet());
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
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
			Assert.assertEquals(4, i.get());
			
			ins.shutdownGracefully().syncUninterruptibly();
			System.out.println("-----close------");
			System.out.println("-----finished------");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
