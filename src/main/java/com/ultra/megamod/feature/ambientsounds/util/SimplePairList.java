package com.ultra.megamod.feature.ambientsounds.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SimplePairList<K, V> implements Iterable<SimplePair<K, V>> {

    protected final List<SimplePair<K, V>> list = new ArrayList<>();

    public void add(K key, V value) {
        list.add(new SimplePair<>(key, value));
    }

    public V getValue(K key) {
        for (SimplePair<K, V> pair : list) {
            if (pair.key().equals(key)) {
                return pair.value();
            }
        }
        return null;
    }

    public SimplePair<K, V> getPair(K key) {
        for (SimplePair<K, V> pair : list) {
            if (pair.key().equals(key)) {
                return pair;
            }
        }
        return null;
    }

    public void sort(Comparator<SimplePair<K, V>> comparator) {
        list.sort(comparator);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public SimplePair<K, V> get(int index) {
        return list.get(index);
    }

    public void clear() {
        list.clear();
    }

    @Override
    public Iterator<SimplePair<K, V>> iterator() {
        return list.iterator();
    }
}
