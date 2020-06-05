package com.zx.sms.common;

import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.Assert;
import com.zx.sms.common.util.IPRange;



public class TestIPRange {

	@Test
	public void testIPv6() throws UnknownHostException {
		IPRange r = new IPRange("fe80::6815:2fc4:e63:c14e");
		System.out.println(r);
		Assert.assertTrue(r.isInRange("fe80::6815:2fc4:e63:c14e"));
		Assert.assertFalse(r.isInRange("fe80::6815:2fc4:e63:c14f"));
	}
	
	@Test
	public void testIPv4() throws UnknownHostException {
		IPRange r = new IPRange("192.168.1.1");
		System.out.println(r);
		Assert.assertTrue(r.isInRange("192.168.1.1"));
		Assert.assertFalse(r.isInRange("192.168.1.2"));
	}
	
	@Test
	public void testIPv6net() throws UnknownHostException {
		IPRange r = new IPRange("2409:8088:81a:131:2:105:10:704/29");
		System.out.println(r);
		Assert.assertTrue(r.isInRange("2409:8089:2020:8110:3000::7"));
		
		r = new IPRange("fe80:319:6815:992::2fc4:e63:c14e/57");
		System.out.println(r);
		Assert.assertTrue(r.isInRange("fe80:319:6815:992::2fc4:e63:c24e"));
		Assert.assertTrue(r.isInRange("fe80:319:6815:992::2fc4:e63:c14f"));
	}
	
	@Test
	public void testIPv4net() throws UnknownHostException {
		IPRange r = new IPRange("192.168.98.48/18");
		
		System.out.println(r);
		Assert.assertTrue(r.isInRange("192.168.93.48"));
	}
}
