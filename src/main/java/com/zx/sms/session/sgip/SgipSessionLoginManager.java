package com.zx.sms.session.sgip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.codec.sgip12.msg.SgipBindRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipBindResponseMessage;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.sgip.SgipEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerEndpointEntity;
import com.zx.sms.session.AbstractSessionLoginManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class SgipSessionLoginManager extends AbstractSessionLoginManager {
	private static final Logger logger = LoggerFactory.getLogger(SgipSessionLoginManager.class);
	public SgipSessionLoginManager(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected void doLogin(Channel ch) {
		//发送bind请求
		SgipEndpointEntity sgipentity = (SgipEndpointEntity) entity;
		SgipBindRequestMessage bind = createBindRequest(sgipentity);
		ch.writeAndFlush(bind);
	}

	@Override
	protected EndpointEntity queryEndpointEntityByMsg(Object msg) {
		
		if(msg instanceof SgipBindRequestMessage){
			SgipBindRequestMessage message = (SgipBindRequestMessage)msg;
			String username = message.getLoginName();
			if (entity instanceof SgipServerEndpointEntity) {
				SgipServerEndpointEntity serverEntity = (SgipServerEndpointEntity) entity;
				return serverEntity.getChild(username.trim());
			}
		}
		
		return null;
	}

	@Override
	protected boolean validAddressHost(EndpointEntity childentity,Channel channel) {
		return true;
	}

	@Override
	protected int validClientMsg(EndpointEntity entity, Object msg) {
		SgipEndpointEntity sgipentity = (SgipEndpointEntity) entity;
		SgipBindRequestMessage message = (SgipBindRequestMessage)msg;
		if(sgipentity.getLoginName().equals(message.getLoginName()) &&
		   sgipentity.getLoginPassowrd().equals(message.getLoginPassowrd())
		   )
		{
			return 0;
		}else{
			return 1;
		}
		
	}

	@Override
	protected int validServermsg(Object message) {
		if(message instanceof SgipBindResponseMessage){
			SgipBindResponseMessage resp = (SgipBindResponseMessage)message;
			
			return resp.getResult();
		}else{
			logger.error("connect msg type error : {}" , message);
			return 9;
		}
	}

	@Override
	protected void changeProtoVersion(ChannelHandlerContext ctx, EndpointEntity entity, Object message) throws Exception {
		
	}

	@Override
	protected void doLoginSuccess(ChannelHandlerContext ctx, EndpointEntity entity, Object message) {
		//发送bind请求
		SgipEndpointEntity sgipentity = (SgipEndpointEntity) entity;
		
		SgipBindResponseMessage resp = new SgipBindResponseMessage(((Message)message).getHeader());
		resp.setResult((short)0);
		resp.setTimestamp(((Message)message).getTimestamp());
		ctx.channel().writeAndFlush(resp);

	}

	@Override
	protected void failedLogin(ChannelHandlerContext ctx, Object msg, long status) {
		if(msg instanceof SgipBindRequestMessage){
			logger.error("Connected error status :{},msg : {}" , status,msg);
			SgipBindRequestMessage message = (SgipBindRequestMessage)msg;
			// 认证失败
			SgipBindResponseMessage resp = new SgipBindResponseMessage(((Message)message).getHeader());
			resp.setResult((short)status);
			resp.setTimestamp(((Message)message).getTimestamp());
			
			ChannelFuture promise = ctx.writeAndFlush(resp);

			final ChannelHandlerContext finalctx = ctx;
			promise.addListener(new GenericFutureListener() {

				public void operationComplete(Future future) throws Exception {
					finalctx.close();
				}
			});
		}else{
			logger.error("connect msg type error : {}" , msg);
			ctx.close();
		}

	}
	
    private SgipBindRequestMessage createBindRequest(SgipEndpointEntity entity)  {
    	SgipBindRequestMessage req = new SgipBindRequestMessage();
    	req.setLoginName(entity.getLoginName());
    	req.setLoginPassowrd(entity.getLoginPassowrd());
    	req.getHeader().setNodeId(entity.getNodeId());
    	/**
    	 * 登录类型。  
    	 * 1：SP向SMG建立的连接，用于发送命令 
    	 * 2：SMG向SP建立的连接，用于发送命令 
    	 * 3：SMG之间建立的连接，用于转发命令  
    	 * 4：SMG向GNS建立的连接，用于路由表的检索和维护  
    	 * 5：GNS向SMG建立的连接，用于路由表的更新
     	 * 6：主备GNS之间建立的连接，用于主备路由表的一致性 
     	 * 11：SP与SMG以及SMG之间建立的测试连接，用于跟踪测试 
     	 * 其它：保留
    	 */
    	if(entity.getChannelType() == ChannelType.UP){
    		req.setLoginType((short)2);
    	}else if (entity.getChannelType() == ChannelType.DOWN){
    		req.setLoginType((short)1);
    	}
    	
        return req;
    }

}
