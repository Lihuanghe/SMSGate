package com.zx.sms.handler;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.packet.CmppConnectRequest;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.sgip12.packet.SgipBindRequest;
import com.zx.sms.codec.sgip12.packet.SgipPacketType;
import com.zx.sms.codec.smgp.msg.SMGPConstants;
import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.ByteArrayUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPCodecChannelInitializer;
import com.zx.sms.connect.manager.sgip.SgipCodecChannelInitializer;
import com.zx.sms.connect.manager.smgp.SMGPCodecChannelInitializer;
import com.zx.sms.connect.manager.smpp.SMPPCodecChannelInitializer;
import com.zx.sms.session.cmpp.SessionLoginManager;
import com.zx.sms.session.sgip.SgipSessionLoginManager;
import com.zx.sms.session.smgp.SMGPSessionLoginManager;
import com.zx.sms.session.smpp.SMPPSessionLoginManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 *通过第一个登陆的请求判断是什么短信协议 
 */
public class SmsProtocolCheckHandler extends ByteToMessageDecoder {
	private static final Logger logger = LoggerFactory.getLogger(SmsProtocolCheckHandler.class);
	private static final Pattern p = Pattern.compile("[0-9]+\\s*:\\s*([0-9]+)");
	private EndpointEntity entity;
	public SmsProtocolCheckHandler(EndpointEntity e) {
		this.entity = e;
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		//读取登陆消息长度
		long packageLength = in.readUnsignedInt();
		int commandId = in.readInt();
		
		in.resetReaderIndex();
		
		ChannelPipeline pipeline = ctx.pipeline();
		//根据登陆请头部长度判断协议
		if(packageLength == CmppConnectRequest.AUTHENTICATORSOURCE.getBodyLength() + 12 && CmppPacketType.CMPPCONNECTREQUEST.getCommandId() == commandId) {
			//CMPP
			pipeline.replace(GlobalConstance.MixedServerIdleStateHandler, "CmppServerIdleStateHandler",  GlobalConstance.idleHandler);
			pipeline.addLast(CMPPCodecChannelInitializer.pipeName(), new CMPPCodecChannelInitializer());
			pipeline.addLast(GlobalConstance.sessionLoginManager, new SessionLoginManager(entity));
			
		}else if(packageLength == SgipBindRequest.LOGINNAME.getBodyLength()+ 20 && SgipPacketType.BINDREQUEST.getCommandId() == commandId) {
			//SGIP
			pipeline.addLast(SgipCodecChannelInitializer.pipeName(), new SgipCodecChannelInitializer());
			pipeline.addLast(GlobalConstance.sessionLoginManager, new SgipSessionLoginManager(entity));
		}else if(packageLength == 42 && SMGPConstants.SMGP_LOGIN == commandId) {
			//SMGP
			pipeline.replace(GlobalConstance.MixedServerIdleStateHandler, "SmgpServerIdleStateHandler",  GlobalConstance.smgpidleHandler);
			pipeline.addLast(SMGPCodecChannelInitializer.pipeName(), new SMGPCodecChannelInitializer(0x30));  //默认使用3.0协议，用户登陆后再更换为正确的协议
			pipeline.addLast(GlobalConstance.sessionLoginManager, new SMGPSessionLoginManager(entity));
		
		}else if(commandId == SmppConstants.CMD_ID_BIND_TRANSCEIVER ||
				commandId == SmppConstants.CMD_ID_BIND_TRANSMITTER ||
				commandId == SmppConstants.CMD_ID_BIND_RECEIVER){
			//SMPP
			pipeline.replace(GlobalConstance.MixedServerIdleStateHandler, "SmppServerIdleStateHandler",  GlobalConstance.smppidleHandler);
			pipeline.addLast(SMPPCodecChannelInitializer.pipeName(), new SMPPCodecChannelInitializer(entity));
			pipeline.addLast(GlobalConstance.sessionLoginManager, new SMPPSessionLoginManager(entity));
		}else {
			//非法包
			logger.warn("error Login Message. dump:{}",ByteBufUtil.hexDump(in));
			ctx.close();
			return;
		}
		
		//Handler挂载完成，删除自己
		pipeline.remove(this);
		//前导的分包器没用了，删除
		pipeline.remove(GlobalConstance.PreLengthFieldBasedFrameDecoder);
		out.add(in.retain());
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
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
					logger.warn("login error entityId : [" + entity.getId()+"] {} : {}", cause.getClass()  ,exceptionMsg);
				}
			}else {
				logger.warn("login error entityId : [" + entity.getId()+"] {} : {}", cause.getClass()  ,exceptionMsg);
			}
			ctx.close();
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel ch = ctx.channel();

		logger.warn("channel closed. the entity is {}.channel remote is {}", entity, ctx.channel().remoteAddress());
		
	}

}
