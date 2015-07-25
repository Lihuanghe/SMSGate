package com.zx.sms.connect.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zx.sms.config.ConfigFileUtil;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public enum EventLoopGroupFactory {
	INS;
	
	private  final static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private  final static EventLoopGroup workgroup = new NioEventLoopGroup();
	private  final static EventLoopGroup msgResend = new NioEventLoopGroup(4);
	private  final static EventLoopGroup waitWindow = new NioEventLoopGroup(4);
	private  final static EventLoopGroup busiWork = new NioEventLoopGroup();
	
	public EventLoopGroup getBoss(){return bossGroup;};
	public EventLoopGroup getWorker(){return workgroup;};
	public EventLoopGroup getMsgResend(){return msgResend;};
	public EventLoopGroup getWaitWindow(){return waitWindow;};
	public EventLoopGroup getBusiWork(){return busiWork;};
}
