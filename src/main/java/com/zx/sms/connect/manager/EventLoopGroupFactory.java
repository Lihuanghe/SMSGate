package com.zx.sms.connect.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.zx.sms.config.PropertiesUtils;
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
	 *使用netty线程池实现一个无限循环任务，
	 *@param task
	 *需要执行的任务
	 *@param exitCondition
	 *任务的关闭条件
	 *@param delay
	 *任务的执行间隔
	 */
	public void submitUnlimitCircleTask(Callable<?> task,ExitUnlimitCirclePolicy exitCondition,long delay){
		addtask(busiWork,task,exitCondition,delay);
	}
	
	private void addtask(final EventLoopGroup executor ,final Callable<?> task ,final ExitUnlimitCirclePolicy exitCondition,final long delay) {
	
		Future<?> future = executor.schedule(task, delay, TimeUnit.MILLISECONDS);
		//
		future.addListener(new GenericFutureListener<Future<Object>>() {
		
			public void operationComplete(Future<Object> future) throws Exception {
				if(future.isSuccess()){
					if(exitCondition.notOver(future))			
						addtask(executor,task ,exitCondition,delay);
				}
			}
		});
	}
	
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
