package com.im.lac.service;


/**
 *
 * @author timbo
 */
public interface ObjectCache<K,V> {
    
    V get(K key);
    
    void delete(K key);
    
    void update(K key, V item);
    
}
