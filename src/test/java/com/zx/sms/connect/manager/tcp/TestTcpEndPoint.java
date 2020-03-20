package com.zx.sms.connect.manager.tcp;

import io.netty.buffer.Unpooled;

import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;

public class TestTcpEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestTcpEndPoint.class);
	@Test
	public void testEchoTcpEndpoint() throws Exception
	{
		int port = 7890;
		final EndpointManager manager = EndpointManager.INS;
		final EndpointEntity server = new TCPServerEndpointEntity(port);
		server.setId("svrID");
		EndpointEntity client = new TCPClientEndpointEntity("127.0.0.1",port);
		client.setId("tcpid");
		client.setMaxChannels((short)2);
		manager.addEndpointEntity(server);
		manager.addEndpointEntity(client);
//		manager.addEndpointEntity(new TCPClientEndpointEntity("221.176.67.103",port));
//		manager.addEndpointEntity(client);
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
//		manager.addEndpointEntity(new TCPClientEndpointEntity("192.168.1.103",port));
		manager.openAll();
//		EventLoopGroupFactory.INS.getWorker().scheduleAtFixedRate(new Runnable() {
//			@Override
//			public void run() {
//				logger.info("connections: {}",manager.getEndpointConnector(server).getConnectionNum());
//			}
//		}, 10, 10, TimeUnit.SECONDS);
		
		Thread.sleep(5000);
		client.getSingletonConnector().fetch().writeAndFlush(Unpooled.wrappedBuffer(new byte[]{1}));
		Thread.sleep(10000);
		 Set<EndpointEntity> entities =  CMPPEndpointManager.INS.allEndPointEntity();
		 for(EndpointEntity en : entities)
		 {
			 CMPPEndpointManager.INS.close(en);
		 }
	}
}
