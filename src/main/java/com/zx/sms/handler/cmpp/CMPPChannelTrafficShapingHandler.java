package com.zx.sms.handler.cmpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

import com.zx.sms.codec.cmpp.msg.Message;

/**
 * 重写了calculateSize方法，按消息条数计算流量
 *
 **/

public class CMPPChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {
	public CMPPChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
		super(writeLimit, readLimit, checkInterval);
		// 积压75条,或者延迟超过2.5s就不能再写了
		setMaxWriteSize(75);
		setMaxWriteDelay(2500);
	}

	private boolean isRequestMsg(Message msg) {
		long commandId = msg.getHeader().getCommandId();
		return (commandId & 0x80000000L) == 0L;
	}

	@Override
	protected long calculateSize(Object msg) {
		if (msg instanceof ByteBuf) {
			return ((ByteBuf) msg).readableBytes();
		}
		if (msg instanceof ByteBufHolder) {
			return ((ByteBufHolder) msg).content().readableBytes();
		}
		if (msg instanceof Message) {
			// 只计算Request信息
			if (isRequestMsg((Message) msg)) {
				return 1;
			}
		}
		return -1;
	}
}
