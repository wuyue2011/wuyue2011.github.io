package cn.zbx1425.mtrsteamloco.render.scripting.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;

public class OrderedMap<K, V> implements Map<K, V> {
    private HashMap<K, V> valueMap;

    private List<K> upsideList;
    private List<K> neutralList;
    private List<K> downsideList;

    public OrderedMap() {
        valueMap = new LinkedHashMap<>();
        upsideList = new ArrayList<>();
        neutralList = new ArrayList<>();
        downsideList = new ArrayList<>();
    }

    public OrderedMap(Map<? extends K, ? extends V> m) {
        this();
        putAll(m);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAll(m, PlacementOrder.NEUTRAL);
    }

    public void putAll(Map<? extends K, ? extends V> m, PlacementOrder order) {
        if (m instanceof OrderedMap) {
            OrderedMap<K, V> other = (OrderedMap<K, V>) m;
            for (K key : other.upsideList) {
                put(key, other.valueMap.get(key), PlacementOrder.UPSIDE);
            }
            for (K key : other.neutralList) {
                put(key, other.valueMap.get(key), PlacementOrder.NEUTRAL);
            }
            for (K key : other.downsideList) {
                put(key, other.valueMap.get(key), PlacementOrder.DOWNSIDE);
            }
        } else {
            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue(), order);
            }
        }
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, PlacementOrder.NEUTRAL);
    }

    public V put(K key, V value, PlacementOrder order) {
        switch (order) {
            case UPSIDE:
                if (!upsideList.contains(key)) upsideList.add(key);
                break;
            case NEUTRAL:
                if (!neutralList.contains(key)) neutralList.add(key);
                break;
            case DOWNSIDE:
                if (!downsideList.contains(key)) downsideList.add(key);
                break;
        }
        V oldValue = valueMap.get(key);
        valueMap.put(key, value);
        return oldValue;
    }

    @Override
    public int size() {
        return valueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return valueMap.isEmpty();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return valueMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return valueMap.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        if (upsideList.contains(key)) {
            int index = upsideList.indexOf(key);
            upsideList.remove(index);
        }else if(neutralList.contains(key)) {
            int index = neutralList.indexOf(key);
            neutralList.remove(index);
        }else if(downsideList.contains(key)) {
            int index = downsideList.indexOf(key);
            downsideList.remove(index);
        }
        return valueMap.remove(key);
    }

    @Override
    public V get(Object key) {
        return valueMap.get(key);
    }

    @Override
    public void clear() {
        valueMap.clear();
        upsideList.clear();
        neutralList.clear();
        downsideList.clear();
    }

    public List<Map.Entry<K, V>> entryList() {
        List<Map.Entry<K, V>> entryList = new ArrayList<>();
        for (K key : upsideList) {
            entryList.add(new Entry<>(key, valueMap.get(key)));
        }
        for (K key : neutralList) {
            entryList.add(new Entry<>(key, valueMap.get(key)));
        }
        for (K key : downsideList) {
            entryList.add(new Entry<>(key, valueMap.get(key)));
        }
        return entryList;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        List<Map.Entry<K, V>> entryList = entryList();
        return new LinkedHashSet<>(entryList);
    }

    @Override
    public Set<K> keySet() {
        List<K> keyList = new ArrayList<>();
        keyList.addAll(upsideList);
        keyList.addAll(neutralList);
        keyList.addAll(downsideList);
        return new LinkedHashSet<>(keyList);
    }

    @Override
    public Collection<V> values() {
        return new ArrayList<>(valueMap.values());
    }

    public enum PlacementOrder {
        UPSIDE,
        NEUTRAL,
        DOWNSIDE
    }

    public class Entry<K, V> implements Map.Entry<K, V> {
        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }
}
