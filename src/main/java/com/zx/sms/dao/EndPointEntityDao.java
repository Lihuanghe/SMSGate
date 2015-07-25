package com.zx.sms.dao;

import java.util.List;

import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

public interface EndPointEntityDao {//客户端访问
	 public List<CMPPEndpointEntity> queryAll();  
     public CMPPEndpointEntity getByName(String name);  
     public List<CMPPEndpointEntity> getById(String id);  
     public void insert(CMPPEndpointEntity cMPPEndpointEntity);  
     public void delete(String id);  
     public void update(CMPPEndpointEntity cMPPEndpointEntity); 
}
