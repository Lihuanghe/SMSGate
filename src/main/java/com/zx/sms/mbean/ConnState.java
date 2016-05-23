package com.zx.sms.mbean;

import io.netty.channel.Channel;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.session.cmpp.SessionStateManager;

public class ConnState implements ConnStateMBean {
	@Override
	public String print(String entityId) {
		 StringBuilder sb = new StringBuilder();
		EndpointManager em = EndpointManager.INS;
		if(StringUtils.isEmpty(entityId)){
			List<EndpointEntity> enlist = em.allEndPointEntity();
			for(EndpointEntity e : enlist){
				sb.append(e.getId()+":\n");
				sb.append(printOne(e));
			}
		}else{
			EndpointEntity e = em.getEndpointEntity(entityId);
			if(e!=null){
				sb.append(e.getId()+":\n");
				sb.append(printOne(e));
			}
		}
		return sb.toString();
	}

	private String printOne(EndpointEntity e){
		 StringBuilder sb = new StringBuilder();
		EndpointConnector econn = EndpointManager.INS.getEndpointConnector(e);
		 Channel[] carr = econn.getallChannel();
		 if(carr!=null && carr.length>0){
			 
			 for(int i= 0;i<carr.length;i++){
				 Channel ch = carr[i];
				 SessionStateManager ssm = (SessionStateManager)ch.pipeline().get("sessionStateManager");
				 sb.append("\tch[");
				 sb.append(ch.localAddress().toString());
				 
				 if(e instanceof ServerEndpoint){
					 sb.append("<-");
				 }else{
					 sb.append("->");
				 }
				 sb.append(ch.remoteAddress().toString() +"]");
				 sb.append("\twaitting-resp=").append(ssm.getWaittingResp());
				 sb.append("\n");
			 }
		 }
		 return sb.toString();
	}
}
