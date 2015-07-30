/**
 * 
 */
package com.zx.sms.handler.api;

import com.zx.sms.connect.manager.EndpointEntity;

import io.netty.channel.ChannelHandler;

/**
 * @author Lihuanghe(18852780@qq.com)
 * 业务处理接口，
 */
public interface BusinessHandlerInterface extends ChannelHandler  {
	/**
	 *业务处理名称
	 */
	String name();
	/**
	 *设置端口对象
	 */
	 void setEndpointEntity(EndpointEntity entity);
	 
	 EndpointEntity getEndpointEntity();
}
