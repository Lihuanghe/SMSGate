package com.zx.sms.connect.manager.cmpp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.cmpp.CMPPMessageCodecAggregator;
import com.zx.sms.codec.cmpp.CmppHeaderCodec;
import com.zx.sms.codec.cmpp10.CMPP10MessageCodecAggregator;
import com.zx.sms.codec.cmpp20.CMPP20MessageCodecAggregator;
import com.zx.sms.common.NotSupportedException;
import com.zx.sms.handler.cmpp.CmppServerIdleStateHandler;

/**
 * @author Lihuanghe(18852780@qq.com)
 *         初始化pipeline，解码是在当前handler前插入，不是使用pipeLine.addLast方法。注意使用
 **/
public class CMPPCodecChannelInitializer extends ChannelInitializer<Channel> {
	private static final Logger logger = LoggerFactory.getLogger(CMPPCodecChannelInitializer.class);
	private int version;
	public final static String codecName = "CMPPMessageCodecAggregator";
	
	private final static int defaultVersion = 0x30;

	public CMPPCodecChannelInitializer(int version) {
		if (version == 0)
			this.version = defaultVersion;
		else
			this.version = version;
	}

	public static String pipeName() {
		return "cmppCodec";
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		/*
		 * 消息总长度(含消息头及消息体) 最大消消息长度要从配置里取 处理粘包，断包
		 */
		pipeline.addBefore(pipeName(), "FrameDecoder", new LengthFieldBasedFrameDecoder(4 * 1024 , 0, 4, -4, 0, true));

		pipeline.addBefore(pipeName(), "CmppHeaderCodec", new CmppHeaderCodec());

		pipeline.addBefore(pipeName(), codecName, getCodecHandler(version));

	}

	public static ChannelHandlerAdapter getCodecHandler(int version) throws Exception {
		if (version == 0x30L) {
			return CMPPMessageCodecAggregator.getInstance();
		} else if (version == 0x20L) {
			return CMPP20MessageCodecAggregator.getInstance();
		} else if (version == 0x10L) {
			return CMPP10MessageCodecAggregator.getInstance();
		}
		logger.error("not supported protocol version: {}", version);
		throw new NotSupportedException("not supported protocol version.");
	}

}
