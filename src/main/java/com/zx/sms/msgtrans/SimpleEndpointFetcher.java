package com.zx.sms.msgtrans;

import java.util.List;

import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;

public class SimpleEndpointFetcher implements EndpointFetcher<CMPPEndpointEntity> {

	@Override
	public void fetch(TransParamater parameter, List<CMPPEndpointEntity> out) {
		List<EndpointEntity> allEntity = EndpointManager.INS.allAllEndPointEntity();
		for(EndpointEntity entity : allEntity){
			
			if(parameter.getMsgType().equals("MO"))
			{
				if(entity instanceof CMPPServerChildEndpointEntity){
					
					if(parameter.getGroup().equals(((CMPPServerChildEndpointEntity)entity).getGroupName())){
						out.add((CMPPServerChildEndpointEntity)entity);
					}
				}
			}else if(parameter.getMsgType().equals("MT")){
				if(entity instanceof CMPPClientEndpointEntity){
					if(parameter.getGroup().equals(((CMPPClientEndpointEntity)entity).getGroupName())){
						out.add((CMPPClientEndpointEntity)entity);
					}
				}	
				
			}
		}
		
	}

}
