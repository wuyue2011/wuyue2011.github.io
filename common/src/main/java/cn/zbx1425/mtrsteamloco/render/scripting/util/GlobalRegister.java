package cn.zbx1425.mtrsteamloco.render.scripting.util;

import java.util.HashMap;
import java.util.Map;

public class GlobalRegister {

    private static final Map<String, Object> register = new HashMap<>();

    public static void put(String key, Object value) {
        synchronized (register) {
            register.put(key, value);
        }
    }

    public static Object get(String key) {
        synchronized (register) {
            return register.get(key);
        }
    }

    public static boolean contains(String key) {
        synchronized (register) {
            return register.containsKey(key);
        }
    }

}