package com.zx.sms.handler.api;

import io.netty.channel.ChannelHandlerAdapter;

import com.zx.sms.connect.manager.EndpointEntity;

public abstract class AbstractBusinessHandler extends ChannelHandlerAdapter implements BusinessHandlerInterface, Cloneable {

	private EndpointEntity entity;

	public void setEndpointEntity(EndpointEntity entity) {
		this.entity = entity;
	}

	public EndpointEntity getEndpointEntity() {
		return entity;
	}

	public abstract String name();

	public AbstractBusinessHandler clone() throws CloneNotSupportedException {
		return (AbstractBusinessHandler) super.clone();
	}
}
