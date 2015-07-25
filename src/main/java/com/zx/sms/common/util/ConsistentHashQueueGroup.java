package com.zx.sms.common.util;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import com.google.common.hash.HashFunction;

/**
 * 使用{@code queue}作为一致性{@code hashing}实现的节点
 * <br>实现了数据在{@code queue group}之间分布式读取和写入
 * @author huzorro(huzorro@gmail.com)
 * @param <E>
 */
public class ConsistentHashQueueGroup<T extends BlockingQueue<E>, E> extends ConsistentHash<T> {

    /**
     * @param nodes
     */
    public ConsistentHashQueueGroup(Collection<T> nodes) {
        super(nodes);
    }

    /**
     * @param hashFunction
     * @param numberOfReplicas
     * @param nodes
     */
    public ConsistentHashQueueGroup(HashFunction hashFunction,
            int numberOfReplicas, Collection<T> nodes) {
        super(hashFunction, numberOfReplicas, nodes);
    }
    /**
     * put message to the queue group
     * @param message
     * @throws InterruptedException
     */
    public void put(E message) throws InterruptedException {
        this.get(message).put(message);
    }
    
    /**
     * take message from the queue group 
     * @return E
     * @throws InterruptedException
     */
    public E take() throws InterruptedException {
        return this.get(System.nanoTime()).take();
    }
    /**
     * remove message from the queue group
     * @param message
     * @return boolean
     */
    public boolean remove(E message) {
        return this.get(message).remove(message);
    }
}
