package com.zx.sms.msgtrans;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.NotSupportedException;
import com.zx.sms.connect.manager.AbstractEndpointConnector;
import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

/**
 *按端口组转发， 
 */
public class GroupEndpointFetcher implements EndpointFetcher<CMPPEndpointEntity> {
	private static final Logger logger = LoggerFactory.getLogger(GroupEndpointFetcher.class);
	@Override
	public void fetch(TransParamater parameter, List<CMPPEndpointEntity> out) {
		List<CMPPEndpointEntity>  groups = CMPPEndpointManager.INS.getEndPointEntityByGroup(parameter.getGroup());
		for(CMPPEndpointEntity entity : groups){
			try {
				if(channelmatch(entity,parameter.getMsgType())){
					out.add(entity);
				}
			} catch (NotSupportedException e) {
				logger.error("",e);
			}
		}
	}
	
	private boolean channelmatch(CMPPEndpointEntity entity , ChannelType msgType) throws NotSupportedException
	{
		if(msgType == ChannelType.UP){
			return entity instanceof ServerEndpoint && entity.getChannelType()!=ChannelType.DOWN;
		}else if(msgType == ChannelType.DOWN)
		{
			return entity instanceof ClientEndpoint && entity.getChannelType()!=ChannelType.UP;
		}else{
			throw new NotSupportedException("not support MsgType: " + msgType.name());
		}
	}

}
