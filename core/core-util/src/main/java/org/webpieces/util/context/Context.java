package org.webpieces.util.context;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public static final String HEADERS = "__headers";
    public static final String REQUEST = "__request";

    private static ThreadLocal<Map<String,Object>> context = ThreadLocal.withInitial(() -> new HashMap<>());

    public static void set(String key, Object value) {
        context.get().put(key, value);
    }
    public static Object get(String key) {
        return context.get().get(key);
    }

    public void clear() {
        context.remove();
    }

    public static Map<String, Object> getContext() {
        return context.get();
    }

    public static void restoreContext(Map<String, Object> props) {
        context.set(props);
    }
}
