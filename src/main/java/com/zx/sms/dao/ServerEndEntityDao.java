package com.zx.sms.dao;

import java.util.List;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;

public interface ServerEndEntityDao {//服务端访问
     public List<CMPPServerEndpointEntity> queryAll();  
     public List<CMPPServerEndpointEntity> getByName(String name);  
     public List<CMPPServerEndpointEntity> getById(String id);  
     public void insert(CMPPServerEndpointEntity cMPPEndpointEntity);  
     public void delete(String id);  
     public void update(CMPPServerEndpointEntity cMPPEndpointEntity); 
     public int select ();
}
