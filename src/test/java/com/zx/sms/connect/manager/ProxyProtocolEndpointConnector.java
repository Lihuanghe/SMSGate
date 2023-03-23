package com.zx.sms.connect.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointConnector;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ProxyProtocolEndpointConnector extends CMPPClientEndpointConnector{
	private static final Logger logger = LoggerFactory.getLogger(ProxyProtocolEndpointConnector.class);
	private String srcAddress ;
	public ProxyProtocolEndpointConnector(CMPPClientEndpointEntity e,String srcAddress) {
		super(e);
		this.srcAddress = srcAddress;
	}
	
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		//handler加在最前端
		pipeline.addFirst(new ProxyProtocolClientHandler());
		pipeline.addFirst(HAProxyMessageEncoder.INSTANCE);
		
		super.doinitPipeLine(pipeline);
	}

	private class  ProxyProtocolClientHandler extends ChannelDuplexHandler{
		
		public void channelActive(final ChannelHandlerContext ctx) throws Exception {
			
			final HAProxyMessage message = new HAProxyMessage(
                    HAProxyProtocolVersion.V2, HAProxyCommand.PROXY, HAProxyProxiedProtocol.TCP4,
                    srcAddress, "127.0.0.2", 8000, 9000);
			
			//消息从当前hander开始发出，不能过其它的协议解析器
			 ChannelFuture future = ctx.writeAndFlush(message);
			  
			ctx.fireChannelActive();
		}
		
	    public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
	    	
	        ChannelFuture future1 = ctx.write(msg, promise);
	        if (msg instanceof HAProxyMessage) {
	        	logger.info("write {}",((HAProxyMessage)msg).toString());
	            future1.addListener(new GenericFutureListener() {
					@Override
					public void operationComplete(Future future) throws Exception {
						if (future.isSuccess()) {
		                    ctx.pipeline().remove(HAProxyMessageEncoder.INSTANCE);
		                    ctx.pipeline().remove(ProxyProtocolClientHandler.this);
		                } else {
		                    ctx.close();
		                }
					}
				});
	        }
	    }
	    
	}
}
