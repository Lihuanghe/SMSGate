package com.zx.sms.msgtrans;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;

public class TestJsEngineEndpointFetcher {
	private JsEngineEndpointFetcher fetcher = new JsEngineEndpointFetcher();
	
	@Test
	public void testeval()
	{
		TransParamater param= new TransParamater();
		param.setIp("127.0.0.1");
		List<CMPPEndpointEntity> out = new ArrayList<CMPPEndpointEntity>();
		fetcher.fetch(param, out);
		CMPPEndpointEntity entity = out.get(0);
		Assert.assertEquals("1234", entity.getId());
		
		out.clear();
		param.setIp("128.0.0.1");
		fetcher.fetch(param, out);
		entity = out.get(0);
		Assert.assertEquals("5678", entity.getId());
	}
	
	@Before
	public void init()
	{
		CMPPServerChildEndpointEntity server = new CMPPServerChildEndpointEntity();
		server.setPort(1234);
		server.setId("1234");
		server.setGroupName("23");
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setPort(1234);
		client.setGroupName("Test");
		client.setWindows((short)16);
		client.setId("5678");
		CMPPEndpointManager.INS.addEndpointEntity(server);
		CMPPEndpointManager.INS.addEndpointEntity(client);
		fetcher.addScript("default", "log.info(param.getIp());if(param.getIp()=='127.0.0.1'){gate = '1234'}if(param.getIp()=='128.0.0.1'){gate = '5678'};");
	}

}
