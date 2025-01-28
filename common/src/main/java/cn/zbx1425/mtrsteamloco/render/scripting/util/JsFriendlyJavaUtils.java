package cn.zbx1425.mtrsteamloco.render.scripting.util;

import java.util.List;

public class JsFriendlyJavaUtils {
    public static <T> T[] asJavaArray(List<T> list) {
        return (T[]) list.toArray();
    }
}