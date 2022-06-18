package org.webpieces.util.context;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public static final String HEADERS = "__headers";
    public static final String REQUEST = "__request";

    private static ThreadLocal<Map<String,Object>> context = ThreadLocal.withInitial(() -> new HashMap<>());

    public static <T> T get(String key) {
        return (T)context.get().get(key);
    }

    public static <T> T get(ContextKey key) {
        return (T)get(key.name());
    }

    public static void put(String key, Object value) {
        context.get().put(key, value);
    }

    public static void put(ContextKey key, Object value) {
        put(key.name(), value);
    }

    public static Object remove(String key) {
        return context.get().remove(key);
    }

    public static Object remove(ContextKey key) {
        return remove(key.name());
    }

    public static void clear() {
        context.remove();
    }

    public static Map<String, Object> getContext() {
        return context.get();
    }

    public static void restoreContext(Map<String, Object> props) {
        context.set(props);
    }

    public static Map<String, Object> copyContext() {
        Map<String, Object> stringObjectMap = context.get();
        return new HashMap<>(stringObjectMap);
    }

}
