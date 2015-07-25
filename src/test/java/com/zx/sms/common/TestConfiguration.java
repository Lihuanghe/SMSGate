package com.zx.sms.common;


import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Test;

import com.zx.sms.config.ConfigFileUtil;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;

public class TestConfiguration {


	@Test
	public void testLoadClient(){

	
		Assert.assertEquals(ConfigFileUtil.getGlobalPropertiesByKey("defaultTransportCharset",null),"GBK");
		
		List<CMPPClientEndpointEntity>  list = ConfigFileUtil.loadClientEndpointEntity();

		Assert.assertEquals(2, list.size());
		Assert.assertEquals("c1", list.get(0).getId());
		Assert.assertEquals(10, list.get(0).getMaxChannels());
		ConfigFileUtil.getJeproperties();
	}
	
	@Test
	public void testLoadSever(){

		Assert.assertEquals(ConfigFileUtil.getGlobalPropertiesByKey("defaultTransportCharset",null),"GBK");
		
		List<CMPPServerEndpointEntity>  list = ConfigFileUtil.loadServerEndpointEntity();
		Assert.assertEquals(1, list.size());
		ConfigFileUtil.getJeproperties();
	}
	
	static{
		ConfigFileUtil.loadconfiguration("configuration.xml");
	}
}
