package com.zx.sms.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.session.cmpp.SessionState;



/**
 * 处理客户端或者服务端登陆，密码校验。协议协商 建立连接前，不会启动消息重试和消息可靠性保证
 */
public abstract class AbstractSessionLoginManager extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSessionLoginManager.class);

	protected EndpointEntity entity;

	/**
	 * 连接状态
	 **/
	protected SessionState state = SessionState.DisConnect;

	public AbstractSessionLoginManager(EndpointEntity entity) {
		this.entity = entity;
	}
	
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	if(state == SessionState.DisConnect){
    		logger.error("connection error until login.",cause);
    		ctx.close();
    	}else{
    		ctx.fireExceptionCaught(cause);
    	}
    }
    
    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 如果是服务端，收到的第一个消息必须是Connect消息
		if (state == SessionState.DisConnect) {
			if (entity instanceof ClientEndpoint) {
				// 客户端收到的第一个消息应该是ConnectResp消息
				receiveConnectResponseMessage(ctx, msg);
			} else {
				receiveConnectMessage(ctx, msg);
			}
		}
		
		ctx.fireChannelRead(msg);
	}


    @Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {	
		if(entity!=null){
			logger.warn("connection closed . {}" ,entity);
			EndpointConnector conn = EndpointManager.INS.getEndpointConnector(entity);
			if(conn!=null)conn.removeChannel(ctx.channel());
		}else{
			logger.debug("session is not created. the entity is {}.channel remote is {}" ,entity ,ctx.channel().remoteAddress());
		}
		ctx.fireChannelInactive();
	}
    
    protected abstract void doLogin(Channel ch);
    @Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (state == SessionState.DisConnect) {
			// 客户端必须先发起Connect消息
			if (entity instanceof ClientEndpoint) {
				
				doLogin(ctx.channel());

			}
		}
		ctx.fireChannelActive();
	}

    protected abstract EndpointEntity queryEndpointEntityByMsg(Object msg);
    protected abstract boolean validAddressHost(String remotehost);
    protected abstract int validClientMsg(EndpointEntity entity,Object message);
    protected abstract int validServermsg(Object message);
    protected abstract void changeProtoVersion(ChannelHandlerContext ctx,EndpointEntity entity,Object message) throws Exception;
    protected abstract void doLoginSuccess(ChannelHandlerContext ctx,EndpointEntity entity,Object message);
    protected abstract void failedLogin(ChannelHandlerContext ctx, Object message, long status) ;
    
    protected void receiveConnectMessage(ChannelHandlerContext ctx, Object message) throws Exception {
		
		// 通过用户名获取端口信息
		EndpointEntity childentity = queryEndpointEntityByMsg( message);
		if (childentity == null) {
			failedLogin(ctx, message, 3);
			return;
		}

		if (!validAddressHost(childentity.getHost())) {
			failedLogin(ctx, message, 2);
			return;
		}

		
		// 服务端收到Request，校验用户名密码成功
		int status = validClientMsg(childentity,message);
		// 认证成功
		if (status == 0) {
			
			//修改协议版本，使用客户端对应协议的协议解析器
			changeProtoVersion(ctx,childentity,message);

			state = SessionState.Connect;
			
			// 打开连接，并把连接加入管理 器
			EndpointManager.INS.openEndpoint(childentity);
			// 端口已打开，获取连接器
			EndpointConnector conn = EndpointManager.INS.getEndpointConnector(childentity);
			
			if(conn==null){
				logger.warn("entity may closed. {}" ,childentity);
				failedLogin(ctx, message, 5);
				return;
			}
			
			//检查是否超过最大连接数
			if(validMaxChannel(childentity,conn)){
				//把连接加入连接管理 器，该方法是同步方法
				conn.addChannel(ctx.channel());
				
				//channelHandler已绑定完成，给客户端发resp.
				doLoginSuccess(ctx,childentity,message);
				
				//通知业务handler连接已建立完成
				notifyChannelConnected(ctx);
				
			}else{
				//超过最大连接数了
				failedLogin(ctx, message, 5);
			}
		} else {
			failedLogin(ctx, message, status);
		}
	}

	
	/**
	 * 状态 0：正确 1：消息结构错 2：非法源地址 3：认证错 4：版本太高 5~ ：其他错误
	 */
	

	private void receiveConnectResponseMessage(ChannelHandlerContext ctx, Object message) throws Exception {
		int status = validServermsg(message);
		if (status == 0) {
			state = SessionState.Connect;
			EndpointConnector conn = EndpointManager.INS.getEndpointConnector(entity);
			if(conn==null){
				logger.warn("entity may closed. {}" ,entity);
				ctx.close();
				return;
			}
			
			if(!validMaxChannel(entity,conn)){
				ctx.close();
				return;
			}
			
			//如果没有超过最大连接数配置，建立连接
			conn.addChannel(ctx.channel());
			notifyChannelConnected(ctx);
		}else{
			ctx.close();
			return;
		}

	}
	
	private boolean validMaxChannel(EndpointEntity entity,EndpointConnector conn)
	{
		int maxChannels = entity.getMaxChannels();

		if (maxChannels != 0 && maxChannels <= conn.getConnectionNum()) {
			logger.warn("MaxChannels config is {}. no more channel will be created . ", maxChannels);
			
			return false;
		}
		return true;
	}
	
	private void notifyChannelConnected(ChannelHandlerContext ctx ){
		//通知业务handler连接已建立完成
		ctx.channel().pipeline().fireUserEventTriggered(SessionState.Connect);
	}

}
