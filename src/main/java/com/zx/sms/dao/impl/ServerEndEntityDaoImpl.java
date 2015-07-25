package com.zx.sms.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.dao.ServerEndEntityDao;
@Component
public class ServerEndEntityDaoImpl implements ServerEndEntityDao {

	@Autowired
	private SqlSession sqlSession;

	@Override
	public List<CMPPServerEndpointEntity> queryAll() {
		
		return null;
	}

	@Override
	public List<CMPPServerEndpointEntity> getByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CMPPServerEndpointEntity> getById(String id) {
		
		return sqlSession.selectOne(id);
	}

	@Override
	public void insert(CMPPServerEndpointEntity cMPPEndpointEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(CMPPServerEndpointEntity cMPPEndpointEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int select() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

	
}
