package cn.zbx1425.mtrsteamloco.render.scripting.util;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class OrderedMap<K, V> {
    private HashMap<K, V> valueMap;
    private List<K> orderList;
    private HashMap<K, PlacementOrder> orderMap;

    public OrderedMap() {
        map = new LinkedHashMap<>();
        orderList = new HashMap<>();
        orderMap = new HashMap<>();
    }

    public void put(K key, V value, PlacementOrder order) {
        map.put(key, value);
        if (orderList.containsKey(key)) {
            int index = orderList.indexOf(key);
            orderList.set(index, key);
        } else {
            orderList.add(key);
        }
        orderMap.put(key, order);
    }

    public void remove(K key) {
        map.remove(key);
        order.remove(key);
    }

    public V get(K key) {
        map.get(key);
    }

    public List<Entry<K, V>> entryList() {
        ArrayList<K> upsides = new ArrayList<>();
        ArrayList<K> neutrals = new ArrayList<>();
        ArrayList<K> downsides = new ArrayList<>();

        for (k key : orderList) {
            PlacementOrder order = orderMap.get(key);
            V value = map.get(key);
            switch (order) {
                case UPSIDE:
                    upsides.add(new Entry(key, value));
                    break;
                case NEUTRAL:
                    neutrals.add(new Entry(key, value));
                    break;
                case DOWNSIDE:
                    downsides.add(new Entry(key, value));
                    break;
            }
        }

        ArrayList<K> combined = new ArrayList<>();
        combined.addAll(upsides);
        combined.addAll(neutrals);
        combined.addAll(downsides);

        ArrayList<Entry<K, V>> entryList = new ArrayList<>();
        for (K key : combined) {
            V value = map.get(key);
            entryList.add(new Entry(key, value));
        }

        return entryList;
    }

    public enum PlacementOrder{
        UPSIDE,
        NEUTRAL,
        DOWNSIDE
    }

    public class Entry<K, V> {
        public final K key;
        public final V value;
        
        public Entry(K key, V value) {
            key = key;
            value = value;
        }
    }
}