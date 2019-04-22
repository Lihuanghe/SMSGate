package com.zx.sms.connect.manager;

import java.util.List;

import com.zx.sms.BaseMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Promise;

/**
 * @author Lihuanghe(18852780@qq.com)
 * 端口管理
 * 负责端口的打开，关闭，最大连接数据控制。，端口总速率设置，负载算法. 一个端口可以有多个tcp连接
 */
public interface EndpointConnector<T extends EndpointEntity> {

	/**
	 *获取端口配置
	 */
	public T getEndpointEntity();
	
	/**
	 *打开一个端口 
	 */
	public ChannelFuture open() throws Exception;
	
	/**
	 * 关闭一个连接
	 */
	public void close(Channel channel) throws Exception;
	
	/**
	 *关闭端口的所有连接 
	 */
	public void close()throws Exception;
	
	/**
	 *根据负载均衡算法获取一个连接
	 */
	Channel fetch();
	/**
	 *获取端口当前连接数
	 */
	int getConnectionNum();

	/**
	 *连接创建成功后，将channel加入连接器，并发送用户事件 
	 */
	public boolean addChannel(Channel ch);
	public void removeChannel(Channel ch);
	public Channel[] getallChannel();
	
	/**
	 *异步发送消息，消息发送至网卡（写入tcp协议栈即表示完成）
	 */
	ChannelFuture asynwrite(Object msg);
	
	/**
	 *同步发送消息，消息收到回复表示完成
	 */
	<K extends BaseMessage> Promise<K> synwrite(K msg);
	
	/**
	 *通过同一个连接同步发送一组消息
	 */
	<K extends BaseMessage> List<Promise<K>> synwrite(List<K> msgs);
}
