package com.zx.sms.handler.api.gate;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.marre.wap.push.SmsMmsNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppTerminateRequestMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.ExitUnlimitCirclePolicy;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.session.cmpp.SessionState;

/**
 * 
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class SessionConnectedHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(SessionConnectedHandler.class);
	private int totleCnt = 200000;

	private int failcnt = 0;
	
	public int getTotleCnt() {
		return totleCnt;
	}
	public void setTotleCnt(int totleCnt) {
		this.totleCnt = totleCnt;
	}
	
	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {

		if (evt == SessionState.Connect) {
			logger.info("{}dddd",ctx.channel().isActive());
			final CMPPEndpointEntity finalentity = (CMPPEndpointEntity) getEndpointEntity();
			final Channel ch = ctx.channel();
			EventLoopGroupFactory.INS.submitUnlimitCircleTask(new Callable<Boolean>() {
				private Message createTestReq() {
					int contentLength = RandomUtils.nextInt() & 0x0f;
					StringBuilder sb = new StringBuilder();
					if (contentLength % 2 == 0) {
						while (contentLength-- > 0) {
							sb.append('中');
						}
					} else {
						while (contentLength-- > 0) {
							sb.append('a');
						}
					}
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("a", 1);
					map.put("b", "adf");
					
					if (finalentity instanceof ServerEndpoint) {
						CmppDeliverRequestMessage msg = new CmppDeliverRequestMessage();
						msg.setDestId("13800138000");
						msg.setLinkid("0000");
						msg.setMsgContent(sb.toString());

						msg.setMsgId(new MsgId());
						msg.setRegisteredDelivery((short) 0);
						if (msg.getRegisteredDelivery() == 1) {
							msg.setReportRequestMessage(new CmppReportRequestMessage());
						}
						msg.setServiceid("10086");
						msg.setSrcterminalId(String.valueOf(System.nanoTime()));
						msg.setSrcterminalType((short) 1);
						msg.setAttachment((Serializable)map);
						msg.setMsgContent(new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd",50*1024));
						
						return msg;
					} else {
						CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
						msg.setDestterminalId(String.valueOf(System.nanoTime()));
						msg.setLinkID("0000");
						msg.setMsgContent(sb.toString());
						msg.setMsgid(new MsgId());
						msg.setServiceId("10086");
						msg.setSrcId("10086");
						msg.setAttachment((Serializable)map);
						msg.setMsgContent(new SmsMmsNotificationMessage("http://www.baidu.com/abc/sfd",50*1024));
						return msg;
					}
				}

				@Override
				public Boolean call() throws Exception{
					int cnt = RandomUtils.nextInt() & 0x1ff;
					while(cnt-->0 && totleCnt>0) {
						ChannelFuture future =ChannelUtil.asyncWriteToEntity(getEndpointEntity(), createTestReq(),new GenericFutureListener<ChannelFuture>() {
							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								
							}
						});
						
						try{
							future.sync();
							totleCnt--;
						}catch(Exception e){
							break;
						}
					}
					return true;
				}
			}, new ExitUnlimitCirclePolicy() {
				@Override
				public boolean notOver(Future future) {
					boolean ret = ch.isActive() && totleCnt > 0;
					if(!ret){
						
						ChannelFuture promise =	ch.writeAndFlush(new CmppTerminateRequestMessage());
						promise.addListener(new GenericFutureListener<ChannelFuture>(){
							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								ch.close();
							}
						});
					}
					return ret;
				}
			},0);
		}
		ctx.fireUserEventTriggered(evt);

	}
	@Override
	public String name() {
		return "SessionConnectedHandler-Gate";
	}

}
