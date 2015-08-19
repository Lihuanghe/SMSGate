package com.zx.sms.connect.manager;

import com.zx.sms.config.PropertiesUtils;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
/**
 *@author Lihuanghe(18852780@qq.com)
 */
public enum EventLoopGroupFactory {
	INS;
	
	private  final static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private  final static EventLoopGroup workgroup = new NioEventLoopGroup();
	private  final static EventLoopGroup msgResend = new NioEventLoopGroup(Integer.valueOf(PropertiesUtils.getproperties("GlobalMsgResendThreadCount","4")));
	private  final static EventLoopGroup waitWindow = new NioEventLoopGroup(Integer.valueOf(PropertiesUtils.getproperties("GlobalWaitWindowThreadCount","4")));
	private  final static EventLoopGroup busiWork = new NioEventLoopGroup(Integer.valueOf(PropertiesUtils.getproperties("GlobalBusiWorkThreadCount","4")));
	
	public EventLoopGroup getBoss(){return bossGroup;};
	public EventLoopGroup getWorker(){return workgroup;};
	public EventLoopGroup getMsgResend(){return msgResend;};
	public EventLoopGroup getWaitWindow(){return waitWindow;};
	public EventLoopGroup getBusiWork(){return busiWork;};
	
	/**
	 *close方法会阻塞，
	 *如果有死循环任务，线程池会关闭不掉。
	 *
	 */
	public void closeAll(){
		//先停业务线程池
		 getBusiWork().shutdownGracefully().syncUninterruptibly();

		 getMsgResend().shutdownGracefully().syncUninterruptibly();
		 getWaitWindow().shutdownGracefully().syncUninterruptibly();
		 getBoss().shutdownGracefully().syncUninterruptibly();
		 
		 //最后停worker
		 getWorker().shutdownGracefully().syncUninterruptibly();
	}
}
