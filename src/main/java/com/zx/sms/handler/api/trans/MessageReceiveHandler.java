package com.zx.sms.handler.api.trans;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppDeliverResponseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitResponseMessage;
import com.zx.sms.codec.cmpp.msg.Message;
import com.zx.sms.common.storedMap.BDBStoredMapFactoryImpl;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EventLoopGroupFactory;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.handler.api.AbstractBusinessHandler;
import com.zx.sms.msgtrans.EndpointFetcher;
import com.zx.sms.msgtrans.GroupEndpointFetcher;
import com.zx.sms.msgtrans.TransParamater;
import com.zx.sms.session.cmpp.SessionState;

@Component("TranMessageReceiveHandler")
public class MessageReceiveHandler extends AbstractBusinessHandler {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiveHandler.class);
	private static AtomicLong cnt = new AtomicLong();
	private static Future future = null;
	private static AtomicInteger connCnt = new AtomicInteger();

	@Override
	public String name() {

		return "MessageReceiveHandler-Trans";
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		connCnt.decrementAndGet();
		if (future != null && connCnt.get() == 0) {
			logger.info("cancel future,{}", ctx.channel().id());
			future.cancel(true);
			future = null;
		}
		logger.info("channelID,{},total receive {}", ctx.channel().id(), cnt.get());
		ctx.fireChannelInactive();
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	
		if (evt == SessionState.Connect) {
			connCnt.incrementAndGet();
			if (future == null) {
				future = EventLoopGroupFactory.INS.getBusiWork().scheduleAtFixedRate(new Runnable() {
					private long lastNum = 0;

					@Override
					public void run() {
						long nowcnt = cnt.get();
						logger.info("Totle Receive Msg Num:{},   speed : {}/s", nowcnt, nowcnt - lastNum);
						lastNum = nowcnt;
					}

				}, 0, 1, TimeUnit.SECONDS);
			}
		}
		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setMsgId(e.getMsgId());
			responseMessage.setResult(0);

			ctx.writeAndFlush(responseMessage);
			transMsg(e);
			cnt.incrementAndGet();

		} else if (msg instanceof CmppDeliverResponseMessage) {
			CmppDeliverResponseMessage e = (CmppDeliverResponseMessage) msg;

		} else if (msg instanceof CmppSubmitRequestMessage) {
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setMsgId(e.getMsgid());
			resp.setResult(0);
			ctx.writeAndFlush(resp);
			transMsg(e);
			cnt.incrementAndGet();
		} else if (msg instanceof CmppSubmitResponseMessage) {
			CmppSubmitResponseMessage e = (CmppSubmitResponseMessage) msg;
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	protected void transMsg(Message msg) {
		TransParamater p = createP(msg);
		if (p == null) {
			logger.warn("");
		}

		EndpointFetcher fetcher = new GroupEndpointFetcher();
		List<CMPPEndpointEntity> out = new ArrayList<CMPPEndpointEntity>();
		// 根据消息获取一个转发目的地端口
		fetcher.fetch(p, out);
		if (out.size() > 0) {
			CMPPEndpointEntity outentity = out.get(0);
			// logger.debug("transferTo {}" ,outentity.getId());
			Queue queue = BDBStoredMapFactoryImpl.INS.getQueue(outentity.getId(),outentity.getId());
			queue.offer(msg);
			// EndpointConnector conn =
			// EndpointManager.INS.getEndpointConnector(outentity);
			// //获取端口上一个连接，fetch实现了负载均衡算法
			// Channel ch = conn.fetch();
			//
			// if(ch!=null){
			// ch.writeAndFlush(msg);
			// }
		} else {
			logger.error("Message Lost .", msg);
		}
	}

	private TransParamater createP(Message msg) {

		TransParamater out = new TransParamater();
		out.setGroup(((CMPPEndpointEntity) getEndpointEntity()).getGroupName());

		if (msg instanceof CmppSubmitRequestMessage) {
			out.setMsgType(EndpointEntity.ChannelType.DOWN);
		} else if (msg instanceof CmppDeliverRequestMessage) {
			out.setMsgType(EndpointEntity.ChannelType.UP);
		} else {
			return null;
		}
		return out;
	}
}
