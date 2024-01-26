package com.zx.sms.session;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.common.util.IPRange;
import com.zx.sms.connect.manager.ClientEndpoint;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.session.cmpp.SessionState;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.proxy.ProxyConnectionEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 处理客户端或者服务端登陆，密码校验。协议协商 建立连接前，不会启动消息重试和消息可靠性保证
 */
public abstract class AbstractSessionLoginManager extends ChannelDuplexHandler {
	private static final Logger logger = LoggerFactory.getLogger(AbstractSessionLoginManager.class);
	private static final Pattern p = Pattern.compile("[0-9]+\\s*:\\s*([0-9]+)");
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
			String exceptionMsg = cause.getMessage();
			if(cause instanceof TooLongFrameException && exceptionMsg != null) {
				Matcher  matcher = p.matcher(exceptionMsg);
				if(matcher.find()) {
					String length = matcher.group(1);
					byte[] chars = ByteArrayUtil.toByteArray(Long.parseLong(length));
					logger.warn("login error. this request maybe HTTP. receive first 4 byte is :\""+ (new String(chars)).trim()+"\" .{}", exceptionMsg);
					ByteBuf buf = ctx.alloc().buffer(40);
					buf.writeBytes(("HTTP/1.0 502\r\nServer: SMS-Gate\r\nDate: "+ (new Date())+"\r\nContent-Length: 0\r\n\r\n").getBytes("UTF-8"));
					ctx.writeAndFlush(buf).addListener(new GenericFutureListener() {
						@Override
						public void operationComplete(Future future) throws Exception {
							// 如果发送消息失败，记录失败日志
							if (!future.isSuccess()) {
								logger.error("", future.cause());
							}
						}
					});
				}else {
					logger.warn("login error entityId : [" + entity.getId()+"] {} : {}", cause.getClass(),cause.getMessage());
				}
			}else {
				logger.warn("login error entityID : [" + entity.getId()+"] {} : {}", cause.getClass(),cause.getMessage());
			}
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
		logger.warn("session is not created. the entityId is [{}];  remote is {}", entity.getId(), ctx.channel().remoteAddress());
		ctx.fireChannelInactive();
	}

	protected abstract BaseMessage  createLoginRequest();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (state == SessionState.DisConnect) {
			// 客户端必须先发起Connect消息
			if (entity instanceof ClientEndpoint) {

				BaseMessage loginRequest = createLoginRequest();
				ctx.channel().writeAndFlush(loginRequest);
				logger.info("session Start : Send to {} Login Message : {}",entity,loginRequest);
			}
		}
		ctx.fireChannelActive();
	}

	protected abstract EndpointEntity queryEndpointEntityByMsg(Object msg);

	protected abstract boolean validAddressHost(EndpointEntity childentity, Channel channel,InetAddress remoteAddr);

	protected abstract int validClientMsg(EndpointEntity entity, Object message);

	protected abstract int validServermsg(Object message);

	protected abstract void changeProtoVersion(ChannelHandlerContext ctx, EndpointEntity entity, Object message) throws Exception;

	protected abstract void doLoginSuccess(ChannelHandlerContext ctx, EndpointEntity entity, Object message);

	protected abstract void failedLogin(ChannelHandlerContext ctx, Object message, long status);
	
	
	private InetAddress getRemoteAddress(Channel channel) {
		InetAddress remoteAddr = ((InetSocketAddress) channel.remoteAddress()).getAddress();
		//因为支持proxy protocol , 优先使用 attr里获取的原始IP
		if(channel.hasAttr(GlobalConstance.proxyProtocolKey)) {
			HAProxyMessage proxyMessage = channel.attr(GlobalConstance.proxyProtocolKey).get();
			if(proxyMessage != null) {
				String sourceAddr = proxyMessage.sourceAddress();
				int port = proxyMessage.sourcePort();
				try {
					remoteAddr = InetAddress.getByName(sourceAddr);
		        } catch(UnknownHostException e) {
		        	remoteAddr = null;
		        }
			}
		}
		return remoteAddr;
	}

	private boolean validRemoteAddress(EndpointEntity childentity, Channel channel) {

		InetAddress remoteAddr = getRemoteAddress(channel);
		
		List<String> allowed = childentity.getAllowedAddr();
		// 如果配置的IP白名单，则必须先满足白名单要求
		if (allowed != null && !allowed.isEmpty()) {
			boolean isallow = false;
			for (String strIp : allowed) {
				if (StringUtils.isNotBlank(strIp)) {
					try {
						IPRange r = new IPRange(strIp.trim());
						if (r.isInRange(remoteAddr)) {
							isallow = true;
							break;
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
			// 检查所有白名单都不满足
			if (!isallow) {
				logger.warn("{} address not allowed. childentity {} ," ,remoteAddr,childentity);
				return false;
			}
		}

		return validAddressHost(childentity, channel,remoteAddr);
	}

	protected void receiveConnectMessage(ChannelHandlerContext ctx, Object message) throws Exception {

		// 通过用户名获取端口信息
		EndpointEntity childentity = null;
		try {
			childentity = queryEndpointEntityByMsg(message);
			// 修改协议版本，使用客户端对应协议的协议解析器
			changeProtoVersion(ctx, childentity, message);
		}catch(Exception e) {
			logger.warn("login failed , {}", message);
			childentity = null;
		}
		
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

			//如果该连接不是此账号的第一个连接，要获取早先第一个生成的entity对象
			EndpointEntity oldEntity = EndpointManager.INS.getEndpointEntity(childentity.getId());
			
			//判断账号实体对象属性是否有变化，如最大连接数，账号密码，发送速度等信息
			if(oldEntity!=null && (!oldEntity.equals(childentity))) {
				//这里简单处理，如果EndpointEntity实体属性有变化，直接关掉旧实体及旧连接，等待客户端重连
				EndpointManager.INS.remove(oldEntity.getId());
			}
			// 打开连接，并把连接加入管理器
			EndpointManager.INS.openEndpoint(childentity);
			
			oldEntity = EndpointManager.INS.getEndpointEntity(childentity.getId());
			// 绑定端口为对应账号的端口
			entity = oldEntity;

			// 端口已打开，获取连接器
			EndpointConnector conn = oldEntity.getSingletonConnector();

			// 检查是否超过最大连接数
			if (conn.addChannel(ctx.channel())) {
				IdleStateHandler idlehandler = (IdleStateHandler) ctx.pipeline().get(GlobalConstance.IdleCheckerHandlerName);
				ctx.pipeline().replace(idlehandler, GlobalConstance.IdleCheckerHandlerName,
						new IdleStateHandler(0, 0, oldEntity.getIdleTimeSec(), TimeUnit.SECONDS));
				state = SessionState.Connect;

				// channelHandler已绑定完成，给客户端发resp.
				doLoginSuccess(ctx, oldEntity, message);

				// 通知业务handler连接已建立完成
				notifyChannelConnected(ctx);
				logger.info("{} login success on channel {}", oldEntity.getId(), ctx.channel());
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
		ctx.pipeline().fireUserEventTriggered(SessionState.Connect);
		ctx.pipeline().remove(this);
	}
	
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		if(evt instanceof ProxyConnectionEvent) {
			ProxyConnectionEvent pe = (ProxyConnectionEvent)evt;
			logger.info("proxy connection : {} ", pe);
		}
		ctx.fireUserEventTriggered(evt);
	}

}
