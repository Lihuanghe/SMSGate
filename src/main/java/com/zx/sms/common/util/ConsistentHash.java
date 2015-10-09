package com.zx.sms.common.util;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * 一致性{@code hashing}算法实现
 * <br>该实现特点是可以任意添加、删除节点而不影响正常的节点存取
 * <br>缺省虚拟节点是{@code 100}
 * @author huzorro(huzorro@gmail.com)
 */
public class ConsistentHash<T> {

    private final Collection<T> nodes;
    private final HashFunction hashFunction;
    private final int numberOfReplicas;
    private final SortedMap<Long, T> circle = new TreeMap<Long, T>();
    /**
     * 
     * @param nodes 节点集
     */
    public ConsistentHash(Collection<T> nodes) {        
        this(Hashing.md5(), 100, nodes);
    }
    /**
     * 
     * @param hashFunction hash实现, 缺省是md5
     * @param numberOfReplicas 虚拟节点
     * @param nodes 节点集
     */
    public ConsistentHash(HashFunction hashFunction, int numberOfReplicas,
            Collection<T> nodes) {
        this.nodes = nodes;
        this.hashFunction = hashFunction;
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            add(node);
        }
    }
    
    /**
     * add node to the circle
     * @param node
     */
    public void add(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.put(hashFunction.hashString(node.toString() + i,Charset.defaultCharset()).asLong(),
                    node);
        }
    }
    /**
     * delete node from the circle
     * @param node
     */
    public void delete(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.remove(hashFunction.hashString(node.toString() + i,Charset.defaultCharset()).asLong());
        }
    }
    /**
     * pass key access node from the circle
     * @param key
     * @return
     */
    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        Long hash = hashFunction.hashString(key.toString(),Charset.defaultCharset()).asLong();
        if (!circle.containsKey(hash)) {
            SortedMap<Long, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }
    public Collection<T> getNodes() {
        return nodes;
    }
}
