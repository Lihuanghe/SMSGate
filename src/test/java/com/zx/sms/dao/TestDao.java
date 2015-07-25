package com.zx.sms.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDao {
	@Autowired
	private SqlSession sqlSession;
	
	
	public int select(){
		return (int)sqlSession.selectOne("findById");
	}
	
	@Transactional(readOnly=false,isolation=Isolation.REPEATABLE_READ)
	public void deleteAll() throws Exception
	{
		sqlSession.delete("deleteAll");

		throw new RuntimeException();
	}
}
