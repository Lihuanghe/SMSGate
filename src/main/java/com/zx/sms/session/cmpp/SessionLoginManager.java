package com.zx.sms.session.cmpp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.zx.sms.codec.cmpp.msg.CmppConnectRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppConnectResponseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;

/**
 * 处理客户端或者服务端登陆，密码校验。协议协商 建立连接前，不会启动消息重试和消息可靠性保证
 */
public class SessionLoginManager extends ChannelHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SessionLoginManager.class);

	private EndpointEntity entity;

	// 如果是服务端，记录服务端口信息
	private CMPPServerChildEndpointEntity serverchildentity;

	/**
	 * 连接状态
	 **/
	private SessionState state = SessionState.DisConnect;

	public SessionLoginManager(EndpointEntity entity) {
		this.entity = entity;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 如果是服务端，收到的第一个消息必须是Connect消息
		if (state == SessionState.DisConnect) {
			if (entity instanceof ClientEndpoint) {
				// 客户端收到的第一个消息应该是ConnectResp消息
				if (!(msg instanceof CmppConnectResponseMessage)) {

					logger.error("channel Not Connnected, Request must be CmppConnectResponseMessage,but is {}",msg.getClass().getName());
					ctx.close();
					return;
				} else {

					CmppConnectResponseMessage message = (CmppConnectResponseMessage) msg;
					receiveConnectResponseMessage(ctx, message);

				}
				
			} else {
				// 服务端收到的第一个消息应该是ConnectRequest
				if (!(msg instanceof CmppConnectRequestMessage)) {

					logger.error("channel Not Connnected, Request must be CmppConnectRequestMessage,but is {}",msg.getClass().getName());
					ctx.close();
					return;
				} else {
					CmppConnectRequestMessage message = (CmppConnectRequestMessage) msg;

					receiveConnectMessage(ctx, message);
				}
			}
		}
		ctx.fireChannelRead(msg);

	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {	
		if(serverchildentity!=null){
			logger.warn("connection closed . {}" ,serverchildentity);
			EndpointConnector conn = CMPPEndpointManager.INS.getEndpointConnector(serverchildentity);
			if(conn!=null)conn.removeChannel(ctx.channel());
		}else if(entity instanceof CMPPEndpointEntity){
			logger.warn("connection closed . {}" ,entity);
			EndpointConnector conn = CMPPEndpointManager.INS.getEndpointConnector((CMPPEndpointEntity)entity);
			if(conn!=null)conn.removeChannel(ctx.channel());
		}else{
			//TODO 如果连接未建立完成。
			logger.debug("shoud not be here. the entity is {}.channel remote is {}" ,entity ,ctx.channel().remoteAddress());
		}
		ctx.fireChannelInactive();
	}
	
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (state == SessionState.DisConnect) {
			// 客户端必须先发起Connect消息
			if (entity instanceof ClientEndpoint) {
				CMPPEndpointEntity cliententity = (CMPPEndpointEntity) entity;
				// TODO 发送连接请求 ,创建密码
				CmppConnectRequestMessage req = new CmppConnectRequestMessage();
				req.setSourceAddr(cliententity.getUserName());
				String timestamp = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "MMddHHmmss");
				req.setTimestamp(Long.parseLong(timestamp));
				byte[] userBytes = cliententity.getUserName().getBytes(GlobalConstance.defaultTransportCharset);
				byte[] passwdBytes = cliententity.getPassword().getBytes(GlobalConstance.defaultTransportCharset);
				byte[] timestampBytes = timestamp.getBytes(GlobalConstance.defaultTransportCharset);
				req.setAuthenticatorSource(DigestUtils.md5(Bytes.concat(userBytes, new byte[9], passwdBytes, timestampBytes)));
				req.setVersion(cliententity.getVersion());
				ctx.channel().writeAndFlush(req);
				logger.info("session Start :Send CmppConnectRequestMessage seq :{}", req.getHeader().getSequenceId());
			}
		}
		ctx.fireChannelActive();
	}

	private int validClientMsg(CmppConnectRequestMessage message, CMPPServerChildEndpointEntity entity)throws Exception {
		byte[] userBytes = entity.getUserName().getBytes(entity.getChartset());
		byte[] passwdBytes = entity.getPassword().getBytes(entity.getChartset());

		byte[] timestampBytes = String.format("%010d", message.getTimestamp()).getBytes(entity.getChartset());
		byte[] authBytes = DigestUtils.md5(Bytes.concat(userBytes, new byte[9], passwdBytes, timestampBytes));

		if (Arrays.equals(authBytes, message.getAuthenticatorSource())) {
			return 0;
		} else {
			logger.error("AuthenticatorSource valided failed");
			return 3;
		}
	}

	private int validServermsg(CmppConnectResponseMessage resp) {
		
		//不校验服务器验证码了。直接返回状态
		return (int) resp.getStatus();
	}

	private boolean validAddressHost(ChannelHandlerContext ctx, String expectedhostIp) {
		//没有配置客户端IP,则不校验
		if(StringUtils.isBlank(expectedhostIp)){
			return true;
		}
		
		InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();

		if (expectedhostIp.equals(socketAddress.getAddress().getHostAddress())) {
			return true;
		}
		return false;
	}

	private void changeCodecHandler(ChannelHandlerContext ctx, int version) throws Exception {
		// TODO 更换协议解析器
		logger.info("changeCodec to version:{}", version);
		ctx.pipeline().replace(CMPPCodecChannelInitializer.codecName, CMPPCodecChannelInitializer.codecName,
				CMPPCodecChannelInitializer.getCodecHandler(version));
	}

	private CMPPServerChildEndpointEntity queryCMPPEndpointEntityInfoByUsername(String username) {

		if (entity instanceof CMPPServerEndpointEntity) {
			CMPPServerEndpointEntity serverEntity = (CMPPServerEndpointEntity) entity;
			return serverEntity.getChild(username.trim());
		}
		return null;
	}

	private void receiveConnectMessage(ChannelHandlerContext ctx, CmppConnectRequestMessage message) throws Exception {
		// 服务端收到Request，校验用户名密码成功
		// TODO 用户名密码校验

		String userName = message.getSourceAddr();
		// 通过用户名获取端口信息
		CMPPServerChildEndpointEntity childentity = queryCMPPEndpointEntityInfoByUsername(userName);
		if (childentity == null) {
			failedLogin(ctx, message, 3);
			return;
		}

		if (!validAddressHost(ctx, childentity.getHost())) {
			failedLogin(ctx, message, 2);
			return;
		}

		// TODO 协议适配
		short version = message.getVersion();
		int status = validClientMsg(message, childentity);
		// 认证成功
		if (status == 0) {
			//默认的是cmpp30的协议，如果不是cmpp30则要更换解析器版本
			if ((short)0x30 != childentity.getVersion()) {
				//发送ConnectRequest里的Version跟配置的不同
				if(childentity.getVersion() != version){
					logger.warn("receive version code {} ,expected version is {} .I would use version {}",version ,childentity.getVersion(),childentity.getVersion());
				}
				
				//以配置的协议版本为准
				changeCodecHandler(ctx, childentity.getVersion());
			}
			this.serverchildentity = childentity;

			state = SessionState.Connect;
			
			// 打开连接，并把连接加入管理 器
			CMPPEndpointManager.INS.openEndpoint(childentity);
			// 端口已打开，获取连接器
			EndpointConnector conn = CMPPEndpointManager.INS.getEndpointConnector(childentity);
			
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
				CmppConnectResponseMessage resp = new CmppConnectResponseMessage(message.getHeader().getSequenceId());
				resp.setStatus(status);
				resp.setAuthenticatorISMG(DigestUtils.md5(Bytes.concat(Ints.toByteArray((int)resp.getStatus()), message.getAuthenticatorSource(), childentity
						.getPassword().getBytes(childentity.getChartset()))));
				ctx.writeAndFlush(resp);
				
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

	private CmppConnectResponseMessage buildCmppConnectResponseMessage(long seq,long status,byte[] ismg)
	{
		CmppConnectResponseMessage resp = new CmppConnectResponseMessage(seq);
		resp.setStatus(status);
		resp.setAuthenticatorISMG(ismg);
		return resp;
	}
	
	/**
	 * 状态 0：正确 1：消息结构错 2：非法源地址 3：认证错 4：版本太高 5~ ：其他错误
	 */
	private void failedLogin(ChannelHandlerContext ctx, CmppConnectRequestMessage message, long status) {
		logger.error("Connected error status :{}" , status);
		// 认证失败
		CmppConnectResponseMessage resp = new CmppConnectResponseMessage(message.getHeader().getSequenceId());
		resp.setAuthenticatorISMG(new byte[16]);
		resp.setStatus(status);
		ChannelFuture promise = ctx.writeAndFlush(resp);

		final ChannelHandlerContext finalctx = ctx;
		promise.addListener(new GenericFutureListener() {

			public void operationComplete(Future future) throws Exception {
				finalctx.close();
			}
		});
	}

	private void receiveConnectResponseMessage(ChannelHandlerContext ctx, CmppConnectResponseMessage message) throws Exception {
		final CMPPEndpointEntity cliententity = (CMPPEndpointEntity) this.entity;
		final short version = message.getVersion();
		int status = validServermsg(message);
		if (status == 0) {
			state = SessionState.Connect;
			EndpointConnector conn = CMPPEndpointManager.INS.getEndpointConnector(cliententity);
			if(conn==null || (!validMaxChannel(cliententity,conn))){
				logger.warn("entity may closed. {}" ,cliententity);
				ctx.close();
				return;
			}
			//如果没有超过最大连接数配置，建立连接
			conn.addChannel(ctx.channel());
			notifyChannelConnected(ctx);
		} else {
			// 认证失败。关闭连接,关闭完成后
			// 修改协议版本，等待下次重连
			if (status == 4) {
				logger.error("protocol version error :{}" , version);
				ChannelFuture future = ctx.close();
				future.addListener(new GenericFutureListener() {

					public void operationComplete(Future future) throws Exception {
						cliententity.setVersion(version);
					}
				});
			}

		}

	}
	
	private boolean validMaxChannel(CMPPEndpointEntity entity,EndpointConnector conn)
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
