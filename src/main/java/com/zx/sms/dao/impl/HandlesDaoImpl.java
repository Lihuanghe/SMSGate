package com.zx.sms.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

import com.zx.sms.dao.HandlesDao;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;

public class HandlesDaoImpl implements HandlesDao {
	

	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public List<AbstractBusinessHandler> queryAll() {
		System.out.println("返回空");
		return null;
	}

	@Override
	public AbstractBusinessHandler getByName(String name) {
		
		return sqlSession.selectOne(name);
	}

	@Override
	public List<Class<BusinessHandlerInterface>> getById(String id) {
		
		return sqlSession.selectOne(id);
	}

	

	
}
