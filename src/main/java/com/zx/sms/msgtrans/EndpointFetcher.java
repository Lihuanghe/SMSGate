package com.zx.sms.msgtrans;

import java.util.List;

import com.zx.sms.connect.manager.EndpointEntity;

/**
 *获取可用的端口
 **/
public interface EndpointFetcher<T extends EndpointEntity>{
	void fetch(TransParamater parameter ,List<T> out);
}
