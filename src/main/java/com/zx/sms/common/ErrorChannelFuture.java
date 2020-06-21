package com.zx.sms.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.*;

/**
 * A skeletal {@link ChannelFuture} implementation which represents a
 * {@link ChannelFuture} which channel is error,such as : not connected,channel writable is false,and so on.
 */
public class ErrorChannelFuture extends CompleteFuture<Void> implements ChannelFuture {

    /**
     * 错误信息描述
     */
    private String errorMsg;

    /**
     * Creates a new instance.
     */
    public ErrorChannelFuture(EventExecutor executor) {
        super(executor);
    }

    /**
     * Creates a new instance.
     */
    public ErrorChannelFuture(String errorMsg) {
        super(GlobalEventExecutor.INSTANCE);
        this.errorMsg = errorMsg;
    }

    @Override
    protected EventExecutor executor() {
        EventExecutor e = super.executor();
        if (e == null) {
            return GlobalEventExecutor.INSTANCE;
        } else {
            return e;
        }
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Throwable cause() {
        return new SendFailException(errorMsg);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public Channel channel() {
        return null;
    }

    @Override
    public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.addListener(listener);
        return this;
    }

    @Override
    public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.addListeners(listeners);
        return this;
    }

    @Override
    public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        super.removeListener(listener);
        return this;
    }

    @Override
    public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        super.removeListeners(listeners);
        return this;
    }

    @Override
    public ChannelFuture syncUninterruptibly() {
        return this;
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        return this;
    }

    @Override
    public ChannelFuture await() throws InterruptedException {
        return this;
    }

    @Override
    public ChannelFuture awaitUninterruptibly() {
        return this;
    }

    @Override
    public Void getNow() {
        return null;
    }

    @Override
    public boolean isVoid() {
        return false;
    }
}
