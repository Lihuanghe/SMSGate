package com.zx.sms.dao;

import java.util.List;

import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;

public interface HandlesDao {//客户端访问
	 public List<AbstractBusinessHandler> queryAll();  
     public AbstractBusinessHandler getByName(String name);  
     public List<Class<BusinessHandlerInterface>> getById(String id);  
    
}
