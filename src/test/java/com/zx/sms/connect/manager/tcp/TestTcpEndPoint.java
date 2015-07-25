package com.zx.sms.connect.manager.tcp;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.EventLoopGroupFactory;

public class TestTcpEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestTcpEndPoint.class);
	@Test
	public void testEchoTcpEndpoint() throws Exception
	{
		int port = 4567;
		final EndpointManager manager = EndpointManager.INS;
		final EndpointEntity server = new TCPServerEndpointEntity(port);
		EndpointEntity client = new TCPClientEndpointEntity("127.0.0.1",port);
		manager.addEndpointEntity(server);
//		manager.addEndpointEntity(new TCPClientEndpointEntity("221.176.67.103",port));
//		manager.addEndpointEntity(client);
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
		manager.openAll();
		EventLoopGroupFactory.INS.getWorker().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				logger.info("connections: {}",manager.getEndpointConnector(server).getConnectionNum());
			}
		}, 10, 10, TimeUnit.SECONDS);
		LockSupport.park();
	}
}
