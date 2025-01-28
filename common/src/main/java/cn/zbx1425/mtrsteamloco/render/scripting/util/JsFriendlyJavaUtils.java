package cn.zbx1425.mtrsteamloco.render.scripting.util;

import java.util.List;

public class JsFriendlyJavaUtils {
    public static <T> T[] asJavaArray(List<T> list) {
        if (list.isEmpty()) {
            // 如果列表为空，返回一个空的T[]数组
            return (T[]) new Object[0];
        }
        // 获取列表中第一个元素的类型
        Class<?> clazz = list.get(0).getClass();
        @SuppressWarnings("unchecked")
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }
}