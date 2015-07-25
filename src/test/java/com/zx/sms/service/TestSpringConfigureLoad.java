package com.zx.sms.service;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.zx.sms.common.util.SpringContextUtil;
import com.zx.sms.dao.TestDao;
import com.zx.sms.session.cmpp.SessionStateManager;

@ContextConfiguration(locations="classpath:applicationContext.xml")
public class TestSpringConfigureLoad extends AbstractJUnit4SpringContextTests {
	private static final Logger logger = LoggerFactory.getLogger(TestSpringConfigureLoad.class);

	
	@Test
	public void testA()
	{
		TestDao dao = (TestDao)SpringContextUtil.getBean(TestDao.class);
		logger.info("查询结果={}",dao.select());
		
	}
	
	@Test
	@Rollback(true)
	public void delete() 
	{
		TestDao dao = (TestDao)SpringContextUtil.getBean(TestDao.class);
		try {
			dao.deleteAll();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("查询结果={}",dao.select());
	}

}
