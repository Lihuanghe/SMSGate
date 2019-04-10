package com.zx.sms.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class TestGuavaCache {

	//这里注意修改刷新时间
	static LoadingCache<String, ConcurrentMap<String, String>> staticcache = CacheBuilder.newBuilder().refreshAfterWrite(5, TimeUnit.SECONDS).build(
			new CacheLoader<String, ConcurrentMap<String, String>>() {
				
				private final ListeningExecutorService executorService =
			            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
					
				   //异步加载，防止缓存加载时阻塞调用线程
				   @Override
				    public ListenableFuture<ConcurrentMap<String, String>> reload(final String key, final ConcurrentMap<String, String> oldValue) throws Exception {
				        ListenableFuture<ConcurrentMap<String, String>> listenableFuture = executorService.submit(new Callable<ConcurrentMap<String, String>>() {
				            @Override
				            public ConcurrentMap<String, String> call() throws Exception {
				                try {

									//模拟从http加载所有数据
									System.out.println("reload : "+key);
									
									//模拟长时间调用
									Thread.sleep(5000);
									ConcurrentMap<String, String> map =  new ConcurrentHashMap<String, String>();
									
									//只加载这个key对应的缓存
									if("Cache".equals(key)) {
										map.put("A", String.valueOf(System.currentTimeMillis()));
										map.put("B", String.valueOf(System.currentTimeMillis()));
										map.put("C", String.valueOf(System.currentTimeMillis()));
										return map;
									}else {
										return map;
									}

								
				                } catch (Exception ex) {
				                    return oldValue;
				                } finally {
				                }
				            }
				        });
				        return listenableFuture;
				    }
				
				@Override
				public ConcurrentMap<String, String> load(String key) throws Exception {
					//返回空map,因为使用缓存永不过期，因此这里只会在预热时调用一次
					System.out.println("load : "+key);
					return  new ConcurrentHashMap<String, String>();
				}
				
			});
	
	private String get(String a) {
		ConcurrentMap<String, String> concurmap = staticcache.getUnchecked("Cache");
		if(concurmap == null) return null;
		return concurmap.get(a);
	}
	
	@Test
	public void test() {
		//程序启动时缓存预热
		System.out.println(String.valueOf(System.currentTimeMillis()));
		staticcache.refresh("Cache");
		System.out.println(String.valueOf(System.currentTimeMillis()));
		
		while(true) {
			long start = System.currentTimeMillis();
			System.out.println(get("A") +"-" +String.valueOf(System.currentTimeMillis()-start));
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
