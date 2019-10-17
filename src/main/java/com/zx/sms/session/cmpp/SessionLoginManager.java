package com.zx.sms.session.cmpp;

import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.zx.sms.codec.cmpp.msg.CmppConnectRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppConnectResponseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.session.AbstractSessionLoginManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 处理客户端或者服务端登陆，密码校验。协议协商 建立连接前，不会启动消息重试和消息可靠性保证
 */
public class SessionLoginManager extends AbstractSessionLoginManager {
	private static final Logger logger = LoggerFactory.getLogger(SessionLoginManager.class);

	/**
	 * 连接状态
	 **/

	public SessionLoginManager(EndpointEntity entity) {
		super(entity);
	}

	private int validClientMsg(CmppConnectRequestMessage message, CMPPServerChildEndpointEntity entity) throws Exception {
		byte[] userBytes = entity.getUserName().getBytes(entity.getChartset());
		byte[] passwdBytes = entity.getPassword().getBytes(entity.getChartset());

		byte[] timestampBytes = String.format("%010d", message.getTimestamp()).getBytes(entity.getChartset());
		byte[] authBytes = DigestUtils.md5(Bytes.concat(userBytes, new byte[9], passwdBytes, timestampBytes));

		if (Arrays.equals(authBytes, message.getAuthenticatorSource())) {
			return 0;
		} else {
			logger.error("AuthenticatorSource valided failed.s:{},c:{}",Hex.encodeHexString(authBytes),Hex.encodeHexString(message.getAuthenticatorSource()));
			return 3;
		}
	}


	@Override
	protected void doLogin(Channel ch) {
		CMPPEndpointEntity cliententity = (CMPPEndpointEntity) entity;
		// TODO 发送连接请求 ,创建密码
		CmppConnectRequestMessage req = new CmppConnectRequestMessage();
		req.setSourceAddr(cliententity.getUserName());
		String timestamp = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "MMddHHmmss");
		req.setTimestamp(Long.parseLong(timestamp));
		byte[] userBytes = cliententity.getUserName().getBytes(cliententity.getChartset());
		byte[] passwdBytes = cliententity.getPassword().getBytes(cliententity.getChartset());
		byte[] timestampBytes = timestamp.getBytes(cliententity.getChartset());
		req.setAuthenticatorSource(DigestUtils.md5(Bytes.concat(userBytes, new byte[9], passwdBytes, timestampBytes)));
		req.setVersion(cliententity.getVersion());
		ch.writeAndFlush(req);
		logger.info("session Start :Send CmppConnectRequestMessage seq :{}", req.getHeader().getSequenceId());
	}

	@Override
	protected EndpointEntity queryEndpointEntityByMsg(Object msg) {
		if(msg instanceof CmppConnectRequestMessage){
			CmppConnectRequestMessage  message = (CmppConnectRequestMessage)msg;
			String username = message.getSourceAddr();
			if (entity instanceof ServerEndpoint) {
				ServerEndpoint serverEntity = (ServerEndpoint) entity;
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
	protected int validClientMsg(EndpointEntity entity, Object message) {
		try {
			return validClientMsg((CmppConnectRequestMessage)message,(CMPPServerChildEndpointEntity)entity);
		} catch (Exception e) {
			logger.error("AuthenticatorSource valided failed",e);
			return 3;
		}
	}

	@Override
	protected int validServermsg(Object message) {
		if(message instanceof CmppConnectResponseMessage){
			CmppConnectResponseMessage resp = (CmppConnectResponseMessage) message;
			//不校验服务器验证码了。直接返回状态
			return (int) resp.getStatus();
		}else{
			logger.error("connect msg type error : {}" , message);
			return 9;
		}
	}

	@Override
	protected void changeProtoVersion(ChannelHandlerContext ctx, EndpointEntity entity,Object msg) throws Exception{
		
		CMPPServerChildEndpointEntity childentity = (CMPPServerChildEndpointEntity)entity;
		CmppConnectRequestMessage message = (CmppConnectRequestMessage)msg;
		short version = message.getVersion();
		//默认的是cmpp30的协议，如果不是cmpp30则要更换解析器版本
		if ((short)0x30 != childentity.getVersion()) {
			//发送ConnectRequest里的Version跟配置的不同
			if(childentity.getVersion() != version){
				logger.warn("receive version code {} ,expected version is {} .I would use version {}",version ,childentity.getVersion(),childentity.getVersion());
			}
			
			//以配置的协议版本为准
			//更换协议解析器
			logger.info("changeCodec to version:{}", childentity.getVersion());
			ctx.pipeline().replace(GlobalConstance.codecName, GlobalConstance.codecName,
					CMPPCodecChannelInitializer.getCodecHandler(childentity.getVersion()));
		}
	}

	@Override
	protected void doLoginSuccess(ChannelHandlerContext ctx, EndpointEntity entity,Object msg) {
		
		CMPPServerChildEndpointEntity childentity = (CMPPServerChildEndpointEntity)entity;
		CmppConnectRequestMessage message = (CmppConnectRequestMessage)msg;
		//channelHandler已绑定完成，给客户端发resp.
		CmppConnectResponseMessage resp = new CmppConnectResponseMessage(message.getHeader().getSequenceId());
		resp.setVersion(childentity.getVersion());
		resp.setStatus(0);
		resp.setAuthenticatorISMG(DigestUtils.md5(Bytes.concat(Ints.toByteArray((int)resp.getStatus()), message.getAuthenticatorSource(), childentity
				.getPassword().getBytes(childentity.getChartset()))));
		ctx.channel().writeAndFlush(resp);
	}

	@Override
	/**
	 * 状态 0：正确 1：消息结构错 2：非法源地址 3：认证错 4：版本太高 5~ ：其他错误
	 */
	protected void failedLogin(ChannelHandlerContext ctx, Object msg, long status) {
		if(msg instanceof CmppConnectRequestMessage){
			logger.error("Connected error status :{},msg : {}" , status,msg);
			CmppConnectRequestMessage message = (CmppConnectRequestMessage)msg;
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
		}else{
			logger.error("connect msg type error : {}" , msg);
			ctx.close();
		}

	}

}
