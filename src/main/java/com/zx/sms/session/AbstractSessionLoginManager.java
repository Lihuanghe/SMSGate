package com.zx.sms.session;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.IPRange;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateHandler;

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
		if (state == SessionState.DisConnect) {
			logger.error("login error entity : " + entity.toString(), cause);
			ctx.close();
		} else {
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
		Channel ch = ctx.channel();

		if (state == SessionState.Connect) {
			EndpointConnector conn = entity.getSingletonConnector();
			if (conn != null)
				conn.removeChannel(ch);
			logger.warn("Connection closed . {} , connect count : {}", entity, conn == null ? 0 : conn.getConnectionNum());
		} else {
			logger.warn("session is not created. the entity is {}.channel remote is {}", entity, ctx.channel().remoteAddress());
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

	protected abstract boolean validAddressHost(EndpointEntity childentity, Channel channel);

	protected abstract int validClientMsg(EndpointEntity entity, Object message);

	protected abstract int validServermsg(Object message);

	protected abstract void changeProtoVersion(ChannelHandlerContext ctx, EndpointEntity entity, Object message) throws Exception;

	protected abstract void doLoginSuccess(ChannelHandlerContext ctx, EndpointEntity entity, Object message);

	protected abstract void failedLogin(ChannelHandlerContext ctx, Object message, long status);

	private boolean validRemoteAddress(EndpointEntity childentity, Channel channel) {
		InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();

		List<String> allowed = childentity.getAllowedAddr();
		// 如果配置的IP白名单，则必须先满足白名单要求
		if (allowed != null && !allowed.isEmpty()) {
			boolean isallow = false;
			for (String strIp : allowed) {
				if (StringUtils.isNotBlank(strIp)) {
					try {
						IPRange r = new IPRange(strIp.trim());
						if (r.isInRange(remoteAddr.getAddress())) {
							isallow = true;
							break;
						}
					} catch (UnknownHostException e) {

					}
				}
			}
			// 检查所有白名单都不满足
			if (!isallow)
				return false;
		}

		return validAddressHost(childentity, channel);
	}

	protected void receiveConnectMessage(ChannelHandlerContext ctx, Object message) throws Exception {

		// 通过用户名获取端口信息
		EndpointEntity childentity = queryEndpointEntityByMsg(message);
		// 修改协议版本，使用客户端对应协议的协议解析器
		changeProtoVersion(ctx, childentity, message);
		
		if (childentity == null) {
			failedLogin(ctx, message, 3);
			return;
		}
				
		if (!validRemoteAddress(childentity, ctx.channel())) {
			failedLogin(ctx, message, 2);
			return;
		}

		// 服务端收到Request，校验用户名密码成功
		int status = validClientMsg(childentity, message);
		// 认证成功
		if (status == 0) {
			// 绑定端口为对应账号的端口
			entity = childentity;

			// 打开连接，并把连接加入管理 器
			EndpointManager.INS.openEndpoint(entity);
			// 端口已打开，获取连接器
			EndpointConnector conn = childentity.getSingletonConnector();

			// 检查是否超过最大连接数
			if (conn.addChannel(ctx.channel())) {
				IdleStateHandler idlehandler = (IdleStateHandler) ctx.pipeline().get(GlobalConstance.IdleCheckerHandlerName);
				ctx.pipeline().replace(idlehandler, GlobalConstance.IdleCheckerHandlerName,
						new IdleStateHandler(0, 0, childentity.getIdleTimeSec(), TimeUnit.SECONDS));
				state = SessionState.Connect;

				// channelHandler已绑定完成，给客户端发resp.
				doLoginSuccess(ctx, childentity, message);

				// 通知业务handler连接已建立完成
				notifyChannelConnected(ctx);
				logger.info("{} login success on channel {}", childentity.getId(), ctx.channel());
			} else {
				// 超过最大连接数了
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
			EndpointConnector conn = entity.getSingletonConnector();

			if (conn.addChannel(ctx.channel())) {
				state = SessionState.Connect;
				// 如果没有超过最大连接数配置，建立连接
				notifyChannelConnected(ctx);
				logger.info("{} login success on channel {}", entity.getId(), ctx.channel());
			} else {
				ctx.close();
				return;
			}

		} else {
			logger.info("{} login failed (status = {}) on channel {}", entity.getId(), status, ctx.channel());
			ctx.close();
			return;
		}
	}

	private void notifyChannelConnected(ChannelHandlerContext ctx) {
		// 通知业务handler连接已建立完成
		ctx.channel().pipeline().fireUserEventTriggered(SessionState.Connect);
	}

}
