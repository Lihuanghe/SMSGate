package com.zx.sms.connect.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.zx.sms.config.PropertiesUtils;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author Lihuanghe(18852780@qq.com)
 */
public enum EventLoopGroupFactory {
	INS;

	private static final Logger logger = LoggerFactory.getLogger(EventLoopGroupFactory.class);

	private static boolean useEpoll() {
		return isLinuxPlatform() && Epoll.isAvailable();
	}

	private static final String OS_NAME = System.getProperty("os.name");
	private static boolean isLinuxPlatform = false;
	private static boolean isWindowsPlatform = false;
	static {
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
			isLinuxPlatform = true;
		}
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
			isWindowsPlatform = true;
		}
	}

	private static boolean isWindowsPlatform() {
		return isWindowsPlatform;

	}

	private static boolean isLinuxPlatform() {
		return isLinuxPlatform;
	}

	private final static RejectedExecutionHandler rejected = new RejectedExecutionHandler() {
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

		}
	};

	private static EventLoopGroup buildEventLoopGroup(int nthread, String threadName) {
		if (useEpoll()) {
			String sThreadName = "Epoll" + threadName;
			logger.info("create EpollEventLoopGroup,Name : {}",sThreadName);
			return new EpollEventLoopGroup(nthread, newThreadFactory(sThreadName));
		} else {
			String sThreadName = "Nio" + threadName;
			logger.info("create NioEventLoopGroup,Name : {}",sThreadName);
			return new NioEventLoopGroup(nthread, newThreadFactory(sThreadName));
		}
	}

	@SuppressWarnings("unchecked")
	public static <C extends ServerChannel> Class<C> selectServerChannelClass() {
		if (useEpoll()) {
			return (Class<C>) EpollServerSocketChannel.class;
		} else {
			return (Class<C>) NioServerSocketChannel.class;
		}
	}

	@SuppressWarnings("unchecked")
	public static <C extends Channel> Class<C> selectChannelClass() {
		if (useEpoll()) {
			return (Class<C>) EpollSocketChannel.class;
		} else {
			return (Class<C>) NioSocketChannel.class;
		}
	}

	private final static EventLoopGroup bossGroup = buildEventLoopGroup(1, "bossGroup");
	private final static EventLoopGroup workgroup = buildEventLoopGroup(0, "workGroup");
	/**
	 * 解决Netty-EventLoopGroup无法submit阻塞任务的问题。 netty的特性：
	 * EventLoopGroup.submit(callable)方法不能提交阻塞任务。
	 * 如果callable阻塞，即使EventLoopGroup中有其它空闲的线程，也无法执行部分提交的任务。
	 * 
	 * 原因：EventLoopGroup的任务队列不是共享的， 每个EventLoop都有独立的任务队列， 如果队列中一个任务阻塞，其余的任务也无法执行。
	 */

	private final static ListeningScheduledExecutorService busiWork = MoreExecutors
			.listeningDecorator(new ScheduledThreadPoolExecutor(
					Integer.parseInt(PropertiesUtils.getProperties("GlobalBusiWorkThreadCount", "4")),
					newThreadFactory("busiWork-"), rejected));
	// private final static EventLoopGroup busiWork = new
	// ShareTaskQueueDefaultEventLoopGroup(Integer.valueOf(PropertiesUtils.getproperties("GlobalBusiWorkThreadCount","4")),new
	// DefaultExecutorServiceFactory("busiWork"));

	public EventLoopGroup getBoss() {
		return bossGroup;
	};

	public EventLoopGroup getWorker() {
		return workgroup;
	};

	public ListeningScheduledExecutorService getBusiWork() {
		return busiWork;
	};

	/**
	 * 使用线程池实现一个无限循环任务，
	 * 
	 * @param task          需要执行的任务
	 * @param exitCondition 任务的关闭条件
	 * @param delay         任务的执行间隔
	 */
	public <T> void submitUnlimitCircleTask(Callable<T> task, ExitUnlimitCirclePolicy<T> exitCondition, long delay) {
		addtask(busiWork, task, exitCondition, delay);
	}

	private <T> void addtask(final ListeningScheduledExecutorService executor, final Callable<T> task,
			final ExitUnlimitCirclePolicy<T> exitCondition, final long delay) {

		if (executor.isShutdown())
			return;
		final ListenableScheduledFuture<T> future = executor.schedule(task, delay, TimeUnit.MILLISECONDS);
		future.addListener(new Runnable() {

			@Override
			public void run() {

				DefaultPromise<T> nettyfuture = new DefaultPromise<T>(GlobalEventExecutor.INSTANCE);
				try {
					nettyfuture.setSuccess(future.get());
				} catch (InterruptedException e) {
					nettyfuture.tryFailure(e);
				} catch (ExecutionException e) {
					nettyfuture.tryFailure(e);
				} catch (Exception e) {
					nettyfuture.tryFailure(e);
				}
				try {
					if (exitCondition.notOver(nettyfuture))
						addtask(executor, task, exitCondition, delay);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}, executor);
	}

	/**
	 * close方法会阻塞， 如果有死循环任务，线程池会关闭不掉。
	 *
	 */
	public void closeAll() {
		// 先停业务线程池
		// getBusiWork().shutdownGracefully().syncUninterruptibly();
		getBusiWork().shutdown();
		getBoss().shutdownGracefully().syncUninterruptibly();

		// 最后停worker
		getWorker().shutdownGracefully().syncUninterruptibly();
	}

	private static ThreadFactory newThreadFactory(final String name) {

		return new ThreadFactory() {

			private final AtomicInteger threadNumber = new AtomicInteger(1);

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, name + threadNumber.getAndIncrement());

				t.setDaemon(true);
				if (t.getPriority() != Thread.NORM_PRIORITY)
					t.setPriority(Thread.NORM_PRIORITY);
				return t;
			}
		};

	}
}
