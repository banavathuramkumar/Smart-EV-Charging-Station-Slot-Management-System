package com.evchargehub.collections;

/**
 * Generic class with Two Types of Parameter (K, V).
 * - Generic Class with Two Types of Parameter
 * - General Form of a Generic Class
 * - Type parameters K (Key) and V (Value)
 */
public class KeyValuePair<K, V> {
    private K key;
    private V value;

    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + " = " + value;
    }
}
