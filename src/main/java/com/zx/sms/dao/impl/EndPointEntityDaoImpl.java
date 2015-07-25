package com.zx.sms.dao.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.dao.EndPointEntityDao;
@Component
public class EndPointEntityDaoImpl  implements EndPointEntityDao{
	
	@Autowired
	private SqlSession sqlSession;
	
	@Override
	public List<CMPPEndpointEntity> queryAll() {
		
		return sqlSession.selectList(null);
	}

	@Override
	public CMPPEndpointEntity getByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CMPPEndpointEntity> getById(String id) {
		
		return sqlSession.selectOne(id);
	}

	@Override
	public void insert(CMPPEndpointEntity cMPPEndpointEntity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(CMPPEndpointEntity cMPPEndpointEntity) {
		// TODO Auto-generated method stub
		
	}
}
