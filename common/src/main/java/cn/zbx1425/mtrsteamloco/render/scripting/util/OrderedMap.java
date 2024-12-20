package cn.zbx1425.mtrsteamloco.render.scripting.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderedMap<K, V> {
    private HashMap<K, V> valueMap;
    private List<K> orderList;
    private HashMap<K, PlacementOrder> orderMap;

    public OrderedMap() {
        valueMap = new HashMap<>();
        orderList = new ArrayList<>();
        orderMap = new HashMap<>();
    }

    public OrderedMap(OrderedMap<K, V> map) {
        valueMap = new HashMap<>(map.valueMap);
        orderList = new ArrayList<>(map.orderList);
        orderMap = new HashMap<>(map.orderMap);
    }

    public void put(K key, V value, PlacementOrder order) {
        valueMap.put(key, value);
        if (orderList.contains(key)) {
            int index = orderList.indexOf(key);
            orderList.set(index, key);
        } else {
            orderList.add(key);
        }
        orderMap.put(key, order);
    }

    public void remove(K key) {
        valueMap.remove(key);
        orderMap.remove(key);
    }

    public V get(K key) {
        return valueMap.get(key);
    }

    public void clear() {
        valueMap.clear();
        orderList.clear();
        orderMap.clear();
    }

    public List<Entry<K, V>> entryList() {
        ArrayList<Entry<K, V>> entryList = new ArrayList<>();
        for (K key : orderList) {
            PlacementOrder order = orderMap.get(key);
            V value = valueMap.get(key);
            switch (order) {
                case UPSIDE:
                    entryList.add(new Entry<>(key, value));
                    break;
                case NEUTRAL:
                    entryList.add(new Entry<>(key, value));
                    break;
                case DOWNSIDE:
                    entryList.add(new Entry<>(key, value));
                    break;
            }
        }
        return entryList;
    }

    public enum PlacementOrder {
        UPSIDE,
        NEUTRAL,
        DOWNSIDE
    }

    public class Entry<K, V> {
        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
