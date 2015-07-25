package com.zx.sms.connect.manager.cmpp;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.zx.sms.config.ConfigFileUtil;
import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.EndpointManager;
/**
 *经测试，35个连接，每个连接每200/s条消息
 *lenovoX250能承担7000/s消息编码解析无压力。
 *10000/s的消息服务不稳定，开个网页，或者打开其它程序导致系统抖动，会有大量消息延迟 (超过500ms)
 *
 *低负载时消息编码解码可控制在10ms以内。
 *
 */

@ContextConfiguration(locations="classpath:applicationContext.xml")
public class TestCMPPEndPoint extends AbstractJUnit4SpringContextTests{
	private static final Logger logger = LoggerFactory.getLogger(TestCMPPEndPoint.class);

	@Test
	public void testCMPPEndpoint() throws Exception {
	
		final CMPPEndpointManager manager = CMPPEndpointManager.INS;

		List list = ConfigFileUtil.loadServerEndpointEntity();
		manager.addAllEndpointEntity(list);
		list.clear();
		List listclient = ConfigFileUtil.loadClientEndpointEntity();
		manager.addAllEndpointEntity(listclient);

		manager.openAll();
		LockSupport.park();
	}
}
