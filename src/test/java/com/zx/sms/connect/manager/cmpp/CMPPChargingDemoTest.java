package com.zx.sms.connect.manager.cmpp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.BaseMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.handler.api.gate.SessionConnectedHandler;
import com.zx.sms.handler.api.smsbiz.MessageReceiveHandler;
/**
 * 如何不修改源码实现增加特殊的Handler处理逻辑
 * 有很多同学要实现按短短信条数计费的功能。但代码默认封装了长短信拆分合并的逻辑
 * 这里给一个例子
 */

public class CMPPChargingDemoTest {
	private static final Logger logger = LoggerFactory.getLogger(CMPPChargingDemoTest.class);

	/*
	 *首先创建自己扩展的Connector,在其中增加自己的Handler 
	 */
	private class MyCMPPClientEndpointConnector extends CMPPClientEndpointConnector{

		public MyCMPPClientEndpointConnector(CMPPClientEndpointEntity e) {
			super(e);
		}
		private final AtomicInteger sendcnt = new AtomicInteger(0);
		private final AtomicInteger readcnt = new AtomicInteger(0);
		@Override
		protected void doBindHandler(ChannelPipeline pipe, EndpointEntity cmppentity) { 
			//这个handler加在协议解析的后边
			pipe.addAfter(GlobalConstance.codecName,"Myhandler", new ChannelDuplexHandler(){
				  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				        ctx.fireChannelRead(msg);
				        if(msg instanceof BaseMessage){
				        	if(((BaseMessage) msg).isRequest()){
				        		 logger.info("read request {},seq={}",readcnt.getAndIncrement(),((BaseMessage) msg).getSequenceNo());
				        	}else{
				        		logger.info("read response.seq={}",((BaseMessage) msg).getSequenceNo());
				        	}
				        }
				       
				    }
				  
				  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
				        ctx.write(msg, promise);
				        if(msg instanceof BaseMessage){
				        	if(((BaseMessage) msg).isRequest()){
				        		 logger.info("send request {},seq={}",sendcnt.getAndIncrement(),((BaseMessage) msg).getSequenceNo());
				        	}else{
				        		logger.info("send response.seq={}",((BaseMessage) msg).getSequenceNo());
				        	}
				        }
				    }
			});
			
			//别忘了调用父类的方法
			super.doBindHandler(pipe,cmppentity);
		}
	}
	
	/*
	 *创建自己扩展的Entity 
	 */
	private class MyCMPPClientEndpointEntity extends CMPPClientEndpointEntity{
		@Override
		protected CMPPClientEndpointConnector buildConnector() {
			return new MyCMPPClientEndpointConnector(this);
		}
	}

	
	/*
	 *使用自己扩展的Entity 
	 */
	
	@Test
	public void testCharging() throws Exception {
	
		final EndpointManager manager = EndpointManager.INS;
		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7892);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);

		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("GSDT01");
		child.setPassword("1qaz2wsx");

		child.setValid(true);
		child.setVersion((short)0x20);

		child.setMaxChannels((short)20);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
//		child.setReSendFailMsg(true);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new CMPPMessageReceiveHandler());
		serverhandlers.add(new AbstractBusinessHandler() {

		    @Override
		    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		    	CMPPResponseSenderHandler handler = new CMPPResponseSenderHandler();
		    	handler.setEndpointEntity(getEndpointEntity());
		    	ctx.pipeline().addBefore("sessionStateManager", handler.name(), handler);
		    	ctx.pipeline().remove(this);
		    }
			
			@Override
			public String name() {
				return "AddCMPPResponseSenderHandler";
			}
			
		});
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		manager.addEndpointEntity(server);
		
	
	
		MyCMPPClientEndpointEntity client = new MyCMPPClientEndpointEntity();
		client.setId("client");
		client.setHost("127.0.0.1");
		client.setPort(7892);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("GSDT01");
		client.setPassword("1qaz2wsx");


		client.setMaxChannels((short)12);
		client.setVersion((short)0x20);
		client.setRetryWaitTimeSec((short)10);
		client.setUseSSL(false);
		client.setReSendFailMsg(false);

		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new CMPPSessionConnectedHandler(10));
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);
		
		manager.openAll();
		//LockSupport.park();

        System.out.println("start.....");
        
		Thread.sleep(300000);
		EndpointManager.INS.close();
	}
}
