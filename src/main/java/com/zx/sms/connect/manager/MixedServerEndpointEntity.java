package com.zx.sms.connect.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chinamobile.cmos.sms.AbstractSmsDcs;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerChildEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPServerEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerEndpointEntity;

public class MixedServerEndpointEntity extends EndpointEntity implements ServerEndpoint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4623975340709986175L;
	private Map<String,EndpointEntity> childrenEndpoint = new ConcurrentHashMap<String,EndpointEntity>() ;
	
	private CMPPServerEndpointEntity cmppServerEndpointEntity = new CMPPServerEndpointEntity();
	private SgipServerEndpointEntity sgipServerEndpointEntity = new SgipServerEndpointEntity();
	private SMGPServerEndpointEntity smgpServerEndpointEntity = new SMGPServerEndpointEntity();
	private SMPPServerEndpointEntity smppServerEndpointEntity = new SMPPServerEndpointEntity();

	@Override
	public void addchild(EndpointEntity entity) {
		String userName = null ;
		if(entity instanceof CMPPServerChildEndpointEntity) {
			cmppServerEndpointEntity.addchild(entity);
			userName = ((CMPPServerChildEndpointEntity)entity).getUserName().trim();
		}else if(entity instanceof SgipServerChildEndpointEntity) {
			sgipServerEndpointEntity.addchild(entity);
			userName = ((SgipServerChildEndpointEntity)entity).getLoginName().trim();
		}else if(entity instanceof SMGPServerChildEndpointEntity) {
			smgpServerEndpointEntity.addchild(entity);
			userName = ((SMGPServerChildEndpointEntity)entity).getClientID().trim();
		}else if(entity instanceof SMPPServerChildEndpointEntity) {
			smppServerEndpointEntity.addchild(entity);
			userName = ((SMPPServerChildEndpointEntity)entity).getSystemId().trim();
		}
		
		if(userName != null)
			childrenEndpoint.put(userName, entity);
	}

	@Override
	public void removechild(EndpointEntity entity) {
		String userName = null ;
		if(entity instanceof CMPPServerChildEndpointEntity) {
			cmppServerEndpointEntity.removechild(entity);
			userName = ((CMPPServerChildEndpointEntity)entity).getUserName().trim();
		}else if(entity instanceof SgipServerChildEndpointEntity) {
			sgipServerEndpointEntity.removechild(entity);
			userName = ((SgipServerChildEndpointEntity)entity).getLoginName().trim();
		}else if(entity instanceof SMGPServerChildEndpointEntity) {
			smgpServerEndpointEntity.removechild(entity);
			userName = ((SMGPServerChildEndpointEntity)entity).getClientID().trim();
		}else if(entity instanceof SMPPServerChildEndpointEntity) {
			smppServerEndpointEntity.removechild(entity);
			userName = ((SMPPServerChildEndpointEntity)entity).getSystemId().trim();
		}
		if(userName != null)
			childrenEndpoint.remove(userName);
	}

	@Override
	public EndpointEntity getChild(String userName) {
		EndpointEntity entity = childrenEndpoint.get(userName);
		if(entity instanceof CMPPServerChildEndpointEntity) {
			return cmppServerEndpointEntity.getChild(userName);
		}else if(entity instanceof SgipServerChildEndpointEntity) {
			return sgipServerEndpointEntity.getChild(userName);
		}else if(entity instanceof SMGPServerChildEndpointEntity) {
			return smgpServerEndpointEntity.getChild(userName);
		}else if(entity instanceof SMPPServerChildEndpointEntity) {
			return smppServerEndpointEntity.getChild(userName);
		}
		return null;
	}

	@Override
	public EndpointEntity getChild(String userName, ChannelType chType) {
		EndpointEntity entity = childrenEndpoint.get(userName);
		if(entity instanceof CMPPServerChildEndpointEntity) {
			return cmppServerEndpointEntity.getChild(userName,chType);
		}else if(entity instanceof SgipServerChildEndpointEntity) {
			return sgipServerEndpointEntity.getChild(userName,chType);
		}else if(entity instanceof SMGPServerChildEndpointEntity) {
			return smgpServerEndpointEntity.getChild(userName,chType);
		}else if(entity instanceof SMPPServerChildEndpointEntity) {
			return smppServerEndpointEntity.getChild(userName,chType);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MixedServerEndpointConnector buildConnector() {
		return new MixedServerEndpointConnector(this);
	}

	@Override
	protected AbstractSmsDcs buildSmsDcs(byte dcs) {
		return null;
	}
}
